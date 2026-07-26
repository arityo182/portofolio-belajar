"""Router endpoint Chatbot Senta."""
import uuid
from fastapi import APIRouter, HTTPException
from sse_starlette.sse import EventSourceResponse

from app.core.config import OLLAMA_MODEL
from app.core.ollama_client import chat_complete, chat_stream, check_ollama_health
from app.features.chat.schema import ChatRequest, ChatResponse
from app.features.chat.prompt_builder import build_system_prompt
from app.features.chat.rag_service import RAGService

router = APIRouter()
rag = RAGService()


@router.get("/health")
async def health():
    """Health check + status Ollama."""
    ollama_up = await check_ollama_health()
    return {
        "status": "UP",
        "service": "Senta Chatbot — RS Medika Sentosa",
        "model": OLLAMA_MODEL,
        "ollama_reachable": ollama_up,
        "rag_docs": rag.collection.count() if rag else 0,
    }


@router.post("/message", response_model=ChatResponse)
async def chat_message(req: ChatRequest):
    """Non-streaming chat — kasih full response."""
    if not req.message.strip():
        raise HTTPException(status_code=400, detail="Pesan tidak boleh kosong")

    # Build system prompt dari context DB
    system_prompt = build_system_prompt(req.context)

    # RAG retrieval (opsional)
    rag_context = rag.retrieve(req.message)
    if rag_context:
        system_prompt += f"\n\n=== INFORMASI TAMBAHAN DARI FAQ RS ===\n{rag_context}"

    # Build messages
    messages = [{"role": "system", "content": system_prompt}]
    for h in req.sessionHistory:
        messages.append({"role": h.get("role", "user"), "content": h.get("content", "")})
    messages.append({"role": "user", "content": req.message})

    # Chat
    try:
        response = await chat_complete(messages)
    except Exception as e:
        raise HTTPException(status_code=503, detail=f"Gagal terhubung ke Ollama: {str(e)}")

    return ChatResponse(
        response=response or "Maaf, Senta sedang tidak bisa merespon. Silakan coba lagi.",
        sessionId=str(uuid.uuid4())
    )


@router.post("/message/stream")
async def chat_message_stream(req: ChatRequest):
    """Streaming chat via Server-Sent Events."""
    if not req.message.strip():
        raise HTTPException(status_code=400, detail="Pesan tidak boleh kosong")

    # Build prompt
    system_prompt = build_system_prompt(req.context)
    rag_context = rag.retrieve(req.message)
    if rag_context:
        system_prompt += f"\n\n=== FAQ RS ===\n{rag_context}"

    # Build messages
    messages = [{"role": "system", "content": system_prompt}]
    for h in req.sessionHistory:
        messages.append({"role": h.get("role", "user"), "content": h.get("content", "")})
    messages.append({"role": "user", "content": req.message})

    return EventSourceResponse(chat_stream(messages))
