"""Konfigurasi environment untuk Chatbot Service (vLLM)."""
import logging
import os

from dotenv import load_dotenv

load_dotenv()

logger = logging.getLogger("chatbot-service")

APP_ENV = os.getenv("APP_ENV", "production").strip().lower()
IS_PRODUCTION = APP_ENV in {"production", "prod"}
ENABLE_DOCS = os.getenv("ENABLE_DOCS", "false").strip().lower() == "true"
ALLOW_UNAUTHENTICATED = (
    not IS_PRODUCTION
    and os.getenv("CHATBOT_ALLOW_UNAUTHENTICATED", "false").strip().lower() == "true"
)

# Autentikasi antar-service: hanya backend Spring Boot yang boleh memanggil.
INTERNAL_API_KEY = os.getenv("INTERNAL_API_KEY", "")
if not INTERNAL_API_KEY and not ALLOW_UNAUTHENTICATED:
    raise RuntimeError(
        "INTERNAL_API_KEY wajib diisi. Set CHATBOT_ALLOW_UNAUTHENTICATED=true "
        "hanya untuk development lokal tanpa backend."
    )
if ALLOW_UNAUTHENTICATED:
    logger.warning(
        "CHATBOT_ALLOW_UNAUTHENTICATED aktif — endpoint chat TIDAK "
        "terautentikasi. Jangan gunakan di production."
    )

# vLLM (OpenAI-compatible API)
VLLM_BASE_URL = os.getenv("VLLM_BASE_URL", "http://localhost:8100")
VLLM_MODEL = os.getenv("VLLM_MODEL", "qwen2.5-14b-instruct")
VLLM_API_KEY = os.getenv("VLLM_API_KEY", "").strip()
WEAK_VLLM_KEYS = {"", "EMPTY", "no-key", "changeme", "CHANGE_ME"}

if VLLM_API_KEY in WEAK_VLLM_KEYS:
    if IS_PRODUCTION and os.getenv("VLLM_ALLOW_NO_KEY", "false").lower() != "true":
        raise RuntimeError(
            "VLLM_API_KEY wajib diisi dengan nilai acak di production "
            "(atau set VLLM_ALLOW_NO_KEY=true bila vLLM memang tanpa auth)."
        )
    logger.warning("VLLM_API_KEY kosong/lemah — hanya aman untuk vLLM lokal.")

# RAG
CHROMA_PATH = os.getenv("CHROMA_PATH", "./chroma_db")
EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "paraphrase-multilingual-MiniLM-L12-v2")

# App
APP_PORT = int(os.getenv("APP_PORT", "8002"))

# Batas input / beban
MAX_MESSAGE_CHARS = int(os.getenv("MAX_MESSAGE_CHARS", "2000"))
MAX_HISTORY_ITEMS = int(os.getenv("MAX_HISTORY_ITEMS", "20"))
MAX_CONTEXT_ITEMS = int(os.getenv("MAX_CONTEXT_ITEMS", "100"))
MAX_CONCURRENT_REQUESTS = max(1, int(os.getenv("MAX_CONCURRENT_REQUESTS", "8")))
RATE_LIMIT_PER_MINUTE = int(os.getenv("RATE_LIMIT_PER_MINUTE", "30"))
