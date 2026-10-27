# 🐳 Cara Run Docker (Local Development)

> Jalankan seluruh stack RS Medika Sentosa di mesin lokal dengan satu perintah.

---

## Prasyarat

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) / Docker Engine + Docker Compose v2
- Minimal **8 GB RAM** (8 service jalan bersamaan)
- Port kosong: `5432`, `6379`, `8080`, `8001`, `8002`, `5173`, `9090`, `3000`

---

## Langkah-langkah

### 1. Clone Repository

```bash
git clone https://github.com/arityo182/portofolio-belajar.git
cd portofolio-belajar
```

### 2. Setup Environment Variables

```bash
cp .env.example .env
```

Edit `.env` dan isi seluruh nilai `CHANGE_ME` dengan nilai acak. Jangan memakai
password default. Contoh membuat secret:

```bash
openssl rand -base64 36
openssl rand -hex 32
```

Compose akan menolak start bila `POSTGRES_PASSWORD`, `REDIS_PASSWORD`,
`JWT_SECRET`, `ML_INTERNAL_API_KEY`, `CHATBOT_INTERNAL_API_KEY`, atau
`GRAFANA_PASSWORD` masih kosong.

### 3. Build & Jalankan

```bash
# Build semua image (pertama kali, ~5-10 menit)
docker compose build

# Jalankan semua service
docker compose up -d
```

**Atau pakai `setup.sh` (recommended — test dulu sebelum build):**

```bash
chmod +x setup.sh
./setup.sh
```

`setup.sh` akan:
1. Test semua Dockerfile (syntax check)
2. Pull base images (postgres, redis, prometheus, grafana)
3. Build dependency images (layer caching)
4. Build semua service
5. Audit secrets (cek hardcoded passwords)

### 4. Cek Status

```bash
docker compose ps
```

Harusnya semua `Up`:

```
NAME               STATUS              PORTS
medika-postgres    Up (healthy)        127.0.0.1:5432->5432/tcp
medika-redis       Up (healthy)        127.0.0.1:6379->6379/tcp
medika-backend     Up (healthy)        127.0.0.1:8080->8080/tcp
medika-ml-service  Up                  127.0.0.1:8001->8001/tcp
medika-chatbot     Up                  127.0.0.1:8002->8002/tcp
medika-frontend    Up                  0.0.0.0:5173->80/tcp
medika-prometheus  Up                  127.0.0.1:9090->9090/tcp
medika-grafana     Up                  127.0.0.1:3000->3000/tcp
```

Port database, Redis, backend, ML, chatbot, dan monitoring hanya di-bind ke
loopback agar tidak dapat diakses langsung dari jaringan luar.

### 5. Akses Aplikasi

| Service | URL | Keterangan |
|---|---|---|
| **Frontend** | http://localhost:5173 | Aplikasi utama |
| **Backend API** | http://localhost:8080 | REST API (loopback) |
| **Swagger UI** | nonaktif default | Nyalakan dengan `SWAGGER_ENABLED=true` (development saja) |
| **ML Service** | http://localhost:8001/health | Internal, wajib `X-Internal-API-Key` |
| **Chatbot** | http://localhost:8002/chat/health | Internal, wajib `X-Internal-API-Key` |
| **Prometheus** | http://localhost:9090 | Loopback |
| **Grafana** | http://localhost:3000 | Loopback; password dari `GRAFANA_PASSWORD` |

### 6. Login Demo

Password akun demo dibuat acak saat seeding dan hanya tercatat di
`CREDENTIALS.md` (di-gitignore). Tidak ada password default yang dibagikan di
repo. Untuk production, buat akun admin melalui `ADMIN_PASSWORD` dan jangan
menjalankan `seed-data.sql`.

---

## Useful Commands

```bash
# Lihat logs semua service
docker compose logs -f

# Lihat logs 1 service
docker compose logs -f backend

# Stop semua service
docker compose down

# Stop + hapus volumes (fresh start)
docker compose down -v

# Rebuild 1 service
docker compose build backend --no-cache
docker compose up -d backend

# Masuk ke container
docker compose exec backend bash
docker compose exec postgres psql -U core -d medika_sentosa
```

---

## Troubleshooting

### Port sudah dipakai
```bash
# Cari process yang pakai port
lsof -i :5432
# Kill process
kill -9 <PID>
```

### PostgreSQL local conflict
```bash
# Stop PostgreSQL local (Ubuntu)
sudo systemctl stop postgresql
```

### Out of memory
```bash
# Cek usage
docker stats

# Kurangi resource limits di docker-compose.yml:
# deploy.resources.limits.memory: 512m
```

### Backend timeout saat startup
```bash
# Tunggu lebih lama (Spring Boot cold start ~30-60 detik)
docker compose logs -f backend
# Cari: "Started MedikasentosaApplication"
```

---

## Arsitektur

```
┌──────────────────────────────────────────────────────────┐
│                    Docker Compose (8 service)             │
│                                                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
│  │ Frontend │  │ Backend  │  │ Chatbot  │  │ ML Svc   │ │
│  │ Nginx:80 │──│ :8080    │──│ :8002    │  │ :8001    │ │
│  └──────────┘  └────┬─────┘  └────┬─────┘  └──────────┘ │
│                     │             │                       │
│               ┌─────┼─────────────┼──────┐               │
│          ┌────┴──┐ ┌┴────┐   ┌────────┐  │               │
│          │Postgre│ │Redis│   │ vLLM   │  │ (external)    │
│          │:5432  │ │:6379│   │ :8100  │  │               │
│          └───────┘ └─────┘   └────────┘  │               │
│          ┌─────────┐          ┌─────────┐ │               │
│          │Prometheu│          │ Grafana │ │               │
│          │s :9090  │          │ :3000   │ │               │
│          └─────────┘          └─────────┘ │               │
└──────────────────────────────────────────────────────────┘
```
