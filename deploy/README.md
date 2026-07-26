# Deploy RS Medika Sentosa — 4 Server

## Topologi

| Server | IP Contoh | Isi |
|--------|-----------|-----|
| SVR 1 | 10.0.1.10 | Backend :8080 + PostgreSQL :5432 + Redis :6379 |
| SVR 2 | 10.0.1.20 | Frontend Nginx :80 |
| SVR 3 | 10.0.1.30 | Chatbot :8002 + ML Service :8001 |
| SVR 4 | 10.0.1.40 | Ollama GPU :11434 |

## Alur Deploy

### 1. Siapkan GPU (SVR 4)
```bash
ssh user@10.0.1.40
curl -fsSL https://ollama.com/install.sh | sh
ollama pull qwen2.5:14b
ollama serve
```

### 2. Deploy Database (SVR 1)
```bash
ssh user@10.0.1.10
docker compose -f docker-compose.svr1.yml up -d
```

### 3. Build & Kirim Image dari LAPTOP
```bash
cd ~/Belajar/Portofolio
./setup.sh    # build semua (1x)
./deploy/push-to-servers.sh
```

### 4. Deploy Server 1-3
```bash
# SVR 1 (backend)
docker compose -f docker-compose.svr1.yml up -d

# SVR 2 (frontend)
docker compose -f docker-compose.svr2.yml up -d

# SVR 3 (chatbot + ml)
docker compose -f docker-compose.svr3.yml up -d
```
