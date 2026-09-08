"""Entry point FastAPI untuk Chatbot Service Senta RS Medika Sentosa."""
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.core.config import APP_PORT
from app.features.chat.router import router as chat_router
from app.features.chat.rag_service import RAGService


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Startup: index knowledge base ke ChromaDB."""
    print("[Senta] Indexing knowledge base...")
    try:
        rag = RAGService()
        rag.index_knowledge_base()
        print(f"[Senta] RAG ready ({rag.collection.count()} docs)")
    except Exception as e:
        print(f"[Senta] RAG init warning: {e}")
    yield


app = FastAPI(
    title="RS Medika Sentosa — Chatbot Senta",
    description="Asisten virtual berbasis AI dengan RAG untuk informasi RS Medika Sentosa.",
    version="1.0.0",
    lifespan=lifespan,
)

# CORS — allow frontend, backend, dan GPU server
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:5173",      # React (dev)
        "http://localhost:8080",      # Spring Boot (dev)
        "http://localhost:3000",      # Grafana
        "http://localhost:80",        # Nginx (Docker)
        "https://mamedika.com",       # Production domain
        "https://www.mamedika.com",   # Production domain (www)
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(chat_router, prefix="/chat")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=APP_PORT, reload=True)
