"""HTTP client async ke Ollama API."""
import httpx
from typing import AsyncGenerator
from app.core.config import OLLAMA_BASE_URL, OLLAMA_MODEL


async def chat_stream(messages: list[dict], model: str = None) -> AsyncGenerator[str, None]:
    """Streaming chat — yield tiap token via Server-Sent Events."""
    model = model or OLLAMA_MODEL
    async with httpx.AsyncClient(timeout=120.0) as client:
        async with client.stream("POST", f"{OLLAMA_BASE_URL}/api/chat", json={
            "model": model, "messages": messages, "stream": True
        }) as response:
            async for line in response.aiter_lines():
                if line.strip():
                    import json
                    try:
                        chunk = json.loads(line)
                        if chunk.get("done"):
                            yield "data: {\"token\": \"\", \"done\": true}\n\n"
                            break
                        token = chunk.get("message", {}).get("content", "")
                        if token:
                            yield f"data: {{\"token\": {json.dumps(token)}, \"done\": false}}\n\n"
                    except json.JSONDecodeError:
                        continue


async def chat_complete(messages: list[dict], model: str = None) -> str:
    """Non-streaming chat — return full response string."""
    model = model or OLLAMA_MODEL
    async with httpx.AsyncClient(timeout=120.0) as client:
        response = await client.post(f"{OLLAMA_BASE_URL}/api/chat", json={
            "model": model, "messages": messages, "stream": False
        })
        data = response.json()
        return data.get("message", {}).get("content", "")


async def check_ollama_health() -> bool:
    """Cek apakah Ollama server reachable."""
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            r = await client.get(f"{OLLAMA_BASE_URL}/api/tags")
            return r.status_code == 200
    except Exception:
        return False
