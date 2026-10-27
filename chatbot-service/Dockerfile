# ============================================
# CHATBOT SERVICE — FastAPI + vLLM + RAG
# ============================================
# OFFLINE: FROM medika-chatbot-deps (deps udah di-image)
# vLLM berjalan di GPU server (Vast.ai), chatbot service
# menghubunginya via HTTP OpenAI-compatible API.
# ============================================

FROM medika-chatbot-deps:latest
WORKDIR /app
# Python + pip packages SUDAH TERINSTALL

COPY . .

# Non-root user. Buat direktori Chroma lebih dulu agar named volume yang
# di-mount mewarisi ownership botuser (bukan root), sehingga SQLite bisa menulis.
RUN mkdir -p /app/chroma_db && \
    adduser --disabled-password --gecos "" botuser && \
    chown -R botuser:botuser /app
USER botuser

HEALTHCHECK --interval=30s --timeout=10s --retries=3 CMD curl -f http://localhost:8002/chat/health || exit 1

EXPOSE 8002
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8002"]
