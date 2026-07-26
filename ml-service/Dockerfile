# ============================================
# ML SERVICE — FastAPI + TensorFlow 2.21
# ============================================
# OFFLINE: FROM medika-ml-deps (deps udah di-image)
# ============================================

FROM medika-ml-deps:latest
WORKDIR /app
# Python + system deps + pip packages SUDAH TERINSTALL

COPY . .

# Buat user non-root untuk keamanan
RUN adduser --disabled-password --gecos "" mluser && \
    chown -R mluser:mluser /app
USER mluser

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget -qO- http://localhost:8001/health || exit 1

EXPOSE 8001
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001"]
