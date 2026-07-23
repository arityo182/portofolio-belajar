# ============================================
# ML SERVICE — FastAPI + TensorFlow 2.21
# Python 3.10 slim
# ============================================

FROM python:3.10-slim

WORKDIR /app

# Install system dependencies yang diperlukan TensorFlow
RUN apt-get update -qq && \
    apt-get install -y -qq --no-install-recommends \
    libgomp1 \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Salin requirements & install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Salin seluruh source code + model
COPY . .

# Buat user non-root untuk keamanan
RUN adduser --disabled-password --gecos "" mluser && \
    chown -R mluser:mluser /app
USER mluser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget -qO- http://localhost:8001/health || exit 1

EXPOSE 8001

# Jalankan dengan uvicorn
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001"]
