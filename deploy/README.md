# Deploy RS Medika Sentosa — 3 Server + GPU

## Topologi

| Server | IP Contoh | Isi |
|--------|-----------|-----|
| SVR 1 | 10.0.1.10 | Backend :8080 + PostgreSQL :5432 + Redis :6379 |
| SVR 2 | 10.0.1.20 | Frontend Nginx :80 |
| GPU | 10.0.1.40 | vLLM :8100 (bare-metal) + ML Service :8001 (Docker) |

## Alur Deploy

### 1. Setup GPU Server (vLLM + ML Service)

```bash
ssh user@10.0.1.40

# Install vLLM
pip install vllm

# Jalankan vLLM (model Qwen 2.5 14B, port 8100)
vllm serve Qwen/Qwen2.5-14B-Instruct \
  --port 8100 \
  --api-key YOUR_API_KEY \
  --max-model-len 8192 \
  --gpu-memory-utilization 0.9

# ML Service (Docker)
docker compose -f deploy/docker-compose.svr5-gpu.yml up -d ml-service
```

### 2. Deploy Backend (SVR 1)

```bash
ssh user@10.0.1.10
docker compose -f docker-compose.svr1.yml up -d
```

### 3. Build & Push Image dari LAPTOP

```bash
cd ~/Belajar/Portofolio
./setup.sh    # build semua (1x)
./deploy/push-to-servers.sh
```

### 4. Deploy Chatbot (SVR 3 atau GPU server)

```bash
# Chatbot menghubungi vLLM via HTTP
# Set VLLM_BASE_URL ke IP GPU server
export VLLM_BASE_URL=http://10.0.1.40:8100
docker compose up -d chatbot
```

## vLLM vs Ollama

| | Ollama | vLLM |
|---|--------|------|
| Multi-user | Buruk (1 request) | Baik (batching) |
| Throughput | ~5 tok/s | ~30-50 tok/s |
| Install | `curl ollama.com/install.sh` | `pip install vllm` |
| API | /api/chat (proprietary) | /v1/chat/completions (OpenAI) |

## AWS Deployment (Opsional)

### EC2 (Recommended)
1. Buat EC2 instance t3.medium (Ubuntu 22.04)
2. Install Docker: `curl -fsSL https://get.docker.com | sh`
3. Clone repo: `git clone https://github.com/arityo182/portofolio-belajar.git`
4. Set environment: `cp .env.example .env` (isi password production)
5. Build & run: `docker compose up -d --build`
6. Uncomment deploy-aws.yml di `.github/workflows/`

### ECS Fallback
- Pakai ECR untuk Docker images
- ECS Fargate untuk container orchestration
- Uncomment section ECS di `deploy-aws.yml`

### LocalStack (Development Only)
```bash
docker compose -f config/localstack/docker-compose.localstack.yml up -d
aws --endpoint-url=http://localhost:4566 s3 mb s3://medika-xray
```
