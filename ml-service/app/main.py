"""Entry point FastAPI untuk ML Service skrining osteoporosis RS Medika Sentosa.

Modul ini mendefinisikan aplikasi FastAPI, siklus hidup warm-up model, serta
endpoint health check dan prediksi osteoporosis (EfficientNetV2 3 kelas +
Grad-CAM).

Layanan ini bersifat internal: seluruh endpoint prediksi mewajibkan header
``X-Internal-API-Key`` yang hanya diketahui backend Spring Boot. Dokumentasi
OpenAPI dimatikan kecuali ``ENABLE_DOCS=true``.
"""

import asyncio
import hmac
import logging
import os
import time
from contextlib import asynccontextmanager
from typing import AsyncIterator

import numpy as np
from fastapi import Depends, FastAPI, File, Header, HTTPException, UploadFile

from app.gradcam import generate_gradcam
from app.predictor import model, predict
from app.preprocessing import preprocess, validate_image_bytes
from app.schema import HealthResponse, PredictionResponse

logger = logging.getLogger("ml-service")

GRADCAM_LAYER = "top_activation"

APP_ENV = os.getenv("APP_ENV", "production").strip().lower()
IS_PRODUCTION = APP_ENV in {"production", "prod"}
ENABLE_DOCS = os.getenv("ENABLE_DOCS", "false").strip().lower() == "true"
ALLOW_UNAUTHENTICATED = (
    not IS_PRODUCTION
    and os.getenv("ML_ALLOW_UNAUTHENTICATED", "false").strip().lower() == "true"
)

INTERNAL_API_KEY = os.getenv("INTERNAL_API_KEY", "")
MAX_UPLOAD_BYTES = int(os.getenv("MAX_UPLOAD_BYTES", str(10 * 1024 * 1024)))
MAX_CONCURRENCY = max(1, int(os.getenv("MAX_CONCURRENCY", "2")))
INFERENCE_TIMEOUT_SECONDS = float(os.getenv("INFERENCE_TIMEOUT_SECONDS", "30"))

if not INTERNAL_API_KEY and not ALLOW_UNAUTHENTICATED:
    raise RuntimeError(
        "INTERNAL_API_KEY wajib diisi. Set ML_ALLOW_UNAUTHENTICATED=true hanya "
        "untuk development lokal tanpa backend."
    )

if ALLOW_UNAUTHENTICATED:
    logger.warning(
        "ML_ALLOW_UNAUTHENTICATED aktif — endpoint prediksi TIDAK terautentikasi. "
        "Jangan gunakan konfigurasi ini di production."
    )

# Membatasi inferensi bersamaan agar RAM/CPU/GPU tidak habis oleh request paralel.
_inference_semaphore = asyncio.Semaphore(MAX_CONCURRENCY)


async def require_internal_key(
    x_internal_api_key: str | None = Header(default=None, alias="X-Internal-API-Key"),
) -> None:
    """Dependency autentikasi antar-service.

    Raises:
        HTTPException: 401 bila header tidak ada atau tidak cocok.
    """
    if ALLOW_UNAUTHENTICATED and not INTERNAL_API_KEY:
        return
    if not x_internal_api_key or not hmac.compare_digest(
        x_internal_api_key, INTERNAL_API_KEY
    ):
        raise HTTPException(status_code=401, detail="Unauthorized")


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    """Siklus hidup aplikasi: warm-up model saat startup.

    Memanggil model sekali dengan gambar dummy agar graf TensorFlow
    terkompilasi lebih awal, sehingga request pertama tidak lambat.

    Args:
        app: Instance FastAPI yang sedang dijalankan.

    Yields:
        None: Kontrol diserahkan ke runtime selama aplikasi berjalan;
            kode setelah `yield` dijalankan saat shutdown.
    """
    logger.info("Warming up model...")
    dummy = np.zeros((1, 224, 224, 3), dtype=np.float32)
    model.predict(dummy, verbose=0)
    logger.info("Model ready")
    yield


app = FastAPI(
    title="RS Medika Sentosa - ML Service",
    description=(
        "Layanan machine learning internal untuk skrining osteoporosis dari "
        "citra X-ray (EfficientNetV2, 3 kelas, Grad-CAM)."
    ),
    version="1.0.0",
    lifespan=lifespan,
    docs_url="/docs" if ENABLE_DOCS else None,
    redoc_url="/redoc" if ENABLE_DOCS else None,
    openapi_url="/openapi.json" if ENABLE_DOCS else None,
)


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    """Health check layanan (tanpa informasi sensitif)."""
    return {"status": "UP", "service": "ML Service - Osteoporosis"}


def _run_inference(image_bytes: bytes) -> dict:
    """Preprocessing + inferensi + Grad-CAM (dijalankan di worker thread)."""
    image = preprocess(image_bytes)
    result = predict(image)
    class_index = result.pop("_class_index")
    result["gradcamImage"] = generate_gradcam(
        model, image, GRADCAM_LAYER, class_index
    )
    return result


@app.post(
    "/predict/osteoporosis",
    response_model=PredictionResponse,
    dependencies=[Depends(require_internal_key)],
)
async def predict_osteoporosis(file: UploadFile = File(...)) -> PredictionResponse:
    """Prediksi osteoporosis dari citra X-ray.

    Args:
        file: Citra X-ray yang diunggah (JPG/PNG, maksimum 10 MB).

    Returns:
        PredictionResponse: Label prediksi, keyakinan, probabilitas, pesan
            risiko, Grad-CAM (base64), dan latensi.

    Raises:
        HTTPException: 400 untuk input tidak valid, 413 terlalu besar,
            503 saat antrean penuh, 504 saat inferensi melebihi batas waktu.
    """
    # Batasi pembacaan agar file raksasa tidak mengisi RAM.
    image_bytes = await file.read(MAX_UPLOAD_BYTES + 1)
    if len(image_bytes) > MAX_UPLOAD_BYTES:
        raise HTTPException(status_code=413, detail="Ukuran file melebihi batas 10 MB")
    if not image_bytes:
        raise HTTPException(status_code=400, detail="File kosong")

    try:
        validate_image_bytes(image_bytes)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc

    start = time.time()
    try:
        # wait_for + to_thread: inferensi TensorFlow bersifat blocking.
        await asyncio.wait_for(
            _inference_semaphore.acquire(), timeout=5
        )
    except asyncio.TimeoutError as exc:
        raise HTTPException(
            status_code=503,
            detail="Layanan sedang sibuk. Coba lagi beberapa saat lagi.",
        ) from exc

    # Jalankan inferensi di thread terpisah. Bila timeout, thread TensorFlow
    # tetap berjalan; slot semaphore ditahan sampai future benar-benar selesai
    # agar request timeout tidak membuka jalan bagi puluhan thread paralel.
    loop = asyncio.get_running_loop()
    future = loop.run_in_executor(None, _run_inference, image_bytes)
    released = False
    try:
        result = await asyncio.wait_for(
            asyncio.shield(future),
            timeout=INFERENCE_TIMEOUT_SECONDS,
        )
    except asyncio.TimeoutError as exc:
        future.add_done_callback(lambda _future: _inference_semaphore.release())
        released = True
        logger.warning("Inference timeout setelah %.0f detik", INFERENCE_TIMEOUT_SECONDS)
        raise HTTPException(
            status_code=504, detail="Pemrosesan citra melebihi batas waktu"
        ) from exc
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception:
        logger.exception("Inference gagal")
        raise HTTPException(
            status_code=500, detail="Gagal memproses citra. Silakan coba lagi."
        ) from None
    finally:
        if not released:
            _inference_semaphore.release()

    result["latencyMs"] = round((time.time() - start) * 1000, 2)
    return result
