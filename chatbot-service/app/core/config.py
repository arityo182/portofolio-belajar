"""Konfigurasi environment untuk Chatbot Service (vLLM)."""
import os
from dotenv import load_dotenv

load_dotenv()

# vLLM (OpenAI-compatible API)
VLLM_BASE_URL = os.getenv("VLLM_BASE_URL", "http://localhost:8100")
VLLM_MODEL = os.getenv("VLLM_MODEL", "qwen2.5-14b-instruct")
VLLM_API_KEY = os.getenv("VLLM_API_KEY", "EMPTY")

# RAG
CHROMA_PATH = os.getenv("CHROMA_PATH", "./chroma_db")
EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "paraphrase-multilingual-MiniLM-L12-v2")

# App
APP_PORT = int(os.getenv("APP_PORT", "8002"))
