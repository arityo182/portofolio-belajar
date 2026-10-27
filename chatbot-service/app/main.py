"""Entry point FastAPI untuk Chatbot Service Senta RS Medika Sentosa.

Layanan ini bersifat internal: seluruh endpoint chat mewajibkan header
``X-Internal-API-Key`` yang hanya diketahui backend Spring Boot. Browser tidak
pernah memanggil layanan ini secara langsung.
"""
import asyncio
import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.core.config import APP_PORT, ENABLE_DOCS
from app.features.chat.router import router as chat_router, initialize_rag

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("chatbot-service")


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Startup: warm-up RAG di background agar health check tidak tertahan."""
    async def _warm_rag() -> None:
        logger.info("[Senta] Menyiapkan RAG di background...")
        try:
            rag = await asyncio.to_thread(initialize_rag)
            if rag is None:
                logger.warning("[Senta] RAG tidak tersedia; chat berjalan tanpa RAG")
                return
            await asyncio.to_thread(rag.index_knowledge_base)
            logger.info("[Senta] RAG ready (%s docs)", rag.collection.count())
        except Exception:
            logger.exception("[Senta] RAG init gagal; chat tetap berjalan tanpa RAG")

    task = asyncio.create_task(_warm_rag())
    yield
    task.cancel()


app = FastAPI(
    title="RS Medika Sentosa — Chatbot Senta",
    description="Asisten virtual berbasis AI dengan RAG untuk informasi RS Medika Sentosa.",
    version="1.0.0",
    lifespan=lifespan,
    docs_url="/docs" if ENABLE_DOCS else None,
    redoc_url="/redoc" if ENABLE_DOCS else None,
    openapi_url="/openapi.json" if ENABLE_DOCS else None,
)

# Tidak ada CORS: layanan ini hanya dipanggil server-to-server.


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    """Jangan bocorkan stack trace/pesan internal ke pemanggil."""
    logger.exception("Unhandled error pada %s %s", request.method, request.url.path)
    return JSONResponse(status_code=500, content={"detail": "Internal server error"})


app.include_router(chat_router, prefix="/chat")

if __name__ == "__main__":
    import uvicorn

    uvicorn.run("app.main:app", host="0.0.0.0", port=APP_PORT, reload=False)
