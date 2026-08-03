"""HTTP client async ke vLLM OpenAI-compatible API.

vLLM menjalankan model inference di GPU server dengan throughput tinggi
untuk melayani banyak user/pasien secara bersamaan.

Endpoint yang digunakan:
  POST /v1/chat/completions  — non-streaming & streaming (SSE)
  GET  /v1/models            — health check (model loaded?)

Contoh部署:
  GPU server (Vast.ai RTX 3090):
    vllm serve Qwen/Qwen2.5-14B-Instruct --port 8100 --api-key $VLLM_API_KEY

  Chatbot service (VPS/Docker):
    VLLM_BASE_URL=http://<gpu-ip>:8100
    VLLM_MODEL=Qwen/Qwen2.5-14B-Instruct
    VLLM_API_KEY=your-api-key
"""
import json
import httpx
from typing import AsyncGenerator
from app.core.config import VLLM_BASE_URL, VLLM_MODEL, VLLM_API_KEY


# ── Timeout & headers ─────────────────────────────────────────────────────
_TIMEOUT = httpx.Timeout(connect=10.0, read=120.0, write=10.0, pool=10.0)
_HEADERS = {
    "Content-Type": "application/json",
    "Authorization": f"Bearer {VLLM_API_KEY}",
}


# ── Non-streaming ─────────────────────────────────────────────────────────
async def chat_complete(messages: list[dict], model: str = None) -> str:
    """Non-streaming chat — return full response string."""
    model = model or VLLM_MODEL
    payload = {
        "model": model,
        "messages": messages,
        "stream": False,
        "temperature": 0.7,
        "max_tokens": 2048,
    }
    async with httpx.AsyncClient(timeout=_TIMEOUT, headers=_HEADERS) as client:
        response = await client.post(f"{VLLM_BASE_URL}/v1/chat/completions", json=payload)
        data = response.json()
        # OpenAI-compatible response: choices[0].message.content
        return data["choices"][0]["message"]["content"]


# ── Streaming (SSE) ──────────────────────────────────────────────────────
async def chat_stream(messages: list[dict], model: str = None) -> AsyncGenerator[str, None]:
    """Streaming chat — yield tiap token via Server-Sent Events.

    vLLM streaming response format (OpenAI-compatible):
        data: {"id":"...","choices":[{"delta":{"content":"token"},"finish_reason":null}]}

    Kita convert ke frontend-compatible SSE:
        data: {"token": "token", "done": false}
    """
    model = model or VLLM_MODEL
    payload = {
        "model": model,
        "messages": messages,
        "stream": True,
        "temperature": 0.7,
        "max_tokens": 2048,
    }
    async with httpx.AsyncClient(timeout=_TIMEOUT, headers=_HEADERS) as client:
        async with client.stream("POST", f"{VLLM_BASE_URL}/v1/chat/completions", json=payload) as response:
            async for line in response.aiter_lines():
                if not line.strip():
                    continue
                # vLLM SSE format: "data: {...}"
                if line.startswith("data: "):
                    line = line[6:]  # strip "data: " prefix
                if line.strip() == "[DONE]":
                    yield 'data: {"token": "", "done": true}\n\n'
                    break
                try:
                    chunk = json.loads(line)
                    # OpenAI-compatible: choices[0].delta.content
                    delta = chunk.get("choices", [{}])[0].get("delta", {})
                    token = delta.get("content", "")
                    finish = chunk.get("choices", [{}])[0].get("finish_reason")
                    if token:
                        yield f'data: {json.dumps({"token": token, "done": False})}\n\n'
                    if finish:
                        yield 'data: {"token": "", "done": true}\n\n'
                        break
                except (json.JSONDecodeError, IndexError, KeyError):
                    continue


# ── Health check ──────────────────────────────────────────────────────────
async def check_vllm_health() -> bool:
    """Cek apakah vLLM server reachable & model loaded."""
    try:
        async with httpx.AsyncClient(timeout=5.0, headers=_HEADERS) as client:
            r = await client.get(f"{VLLM_BASE_URL}/v1/models")
            data = r.json()
            # vLLM /v1/models returns {"data": [{"id": "model-name", ...}]}
            models = data.get("data", [])
            return len(models) > 0
    except Exception:
        return False
