"""Entry point FastAPI untuk ML Service skrining osteoporosis RS Medika Sentosa.

Modul ini mendefinisikan aplikasi FastAPI, middleware CORS, siklus hidup
warm-up model, serta endpoint health check dan prediksi osteoporosis
(EfficientNetV2 3 kelas + Grad-CAM).
"""

import time
import numpy as np
from contextlib import asynccontextmanager
from typing import AsyncIterator
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from app.preprocessing import preprocess
from app.predictor import predict, model
from app.gradcam import generate_gradcam
from app.schema import PredictionResponse, HealthResponse

GRADCAM_LAYER = "top_activation"
# GRADCAM_LAYER = "layer_normalization"

# membuat data dummy di awal inisiasi karen tensorflow menyebalkan untuk pertama kali test perlu dipancing
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
    # ── WARM-UP: panggil model sekali dengan gambar dummy ──
    print("Warming up model...")
    dummy = np.zeros((1, 224, 224, 3), dtype=np.float32)
    model.predict(dummy, verbose=0)
    print("Model ready!")
    yield
    # (kode cleanup saat shutdown bisa di sini, kosong untuk sekarang)


app = FastAPI(
    title="RS Medika Sentosa - ML Service",
    description=(
        "Layanan machine learning untuk skrining osteoporosis dari citra "
        "X-ray. Menggunakan model EfficientNetV2 (3 kelas: Normal, Osteopenia, "
        "Osteoporosis) dengan visualisasi Grad-CAM untuk interpretabilitas."
    ),
    version="1.0.0",
    lifespan=lifespan,
)

# CORS
app.add_middleware(CORSMiddleware,
                   allow_origins=[
                       "http://localhost:8080",     # Spring Boot
                        "http://localhost:5173",    # React
                   ],
                   allow_methods=["*"],
                   allow_headers=["*"],
                   )


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    """Health check layanan.

    Endpoint ringan untuk memastikan service hidup dan dapat diakses
    (mis. oleh load balancer atau backend Spring Boot).

    Returns:
        HealthResponse: Status layanan dan identitasnya.
    """
    return {"status": "UP", "service": "ML Service - Osteoporosis"}


@app.post("/predict/osteoporosis", response_model=PredictionResponse)
async def predict_osteoporosis(file: UploadFile = File(...)) -> PredictionResponse:
    """Prediksi osteoporosis dari citra X-ray.

    Melakukan preprocessing citra, inferensi model EfficientNetV2 (3 kelas),
    dan menghasilkan visualisasi Grad-CAM. Latensi pemrosesan sisi server
    diukur dan disertakan dalam respons.

    Args:
        file: Citra X-ray yang diunggah (JPG/PNG).

    Returns:
        PredictionResponse: Label prediksi, tingkat keyakinan, probabilitas
            3 kelas, pesan risiko klinis, gambar Grad-CAM (base64), dan latensi.

    Raises:
        HTTPException: Status 400 jika format file bukan JPG/PNG, atau jika
            citra tidak valid/tidak dapat diproses.
    """
    if file.content_type not in ["image/jpeg", "image/png"]:
        raise HTTPException(status_code=400, detail="Format harus JPG atau PNG")

    img_bytes = await file.read()

    start = time.time()
    try:
        img = preprocess(img_bytes)
        result = predict(img)

        class_index = result.pop("_class_index")
        gradcam_image = generate_gradcam(model, img, GRADCAM_LAYER, class_index)
        result["gradcamImage"] = gradcam_image

    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

    result["latencyMs"] = round((time.time() - start) * 1000, 2)
    return result