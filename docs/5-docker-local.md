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

Edit `.env` isi password production (atau pakai default untuk development):

```env
POSTGRES_PASSWORD=dev_password_change_me
JWT_SECRET=dev_jwt_secret_change_me_in_production_use_openssl_rand_base64_48
```

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
medika-postgres    Up (healthy)        0.0.0.0:5432->5432/tcp
medika-redis       Up (healthy)        0.0.0.0:6379->6379/tcp
medika-backend     Up (healthy)        0.0.0.0:8080->8080/tcp
medika-ml-service  Up                  0.0.0.0:8001->8001/tcp
medika-chatbot     Up                  0.0.0.0:8002->8002/tcp
medika-frontend    Up                  0.0.0.0:5173->80/tcp
medika-prometheus  Up                  0.0.0.0:9090->9090/tcp
medika-grafana     Up                  0.0.0.0:3000->3000/tcp
```

### 5. Akses Aplikasi

| Service | URL | Keterangan |
|---|---|---|
| **Frontend** | http://localhost:5173 | Aplikasi utama |
| **Backend API** | http://localhost:8080 | REST API |
| **Swagger UI** | http://localhost:8080/swagger-ui/index.html | API docs |
| **ML Service** | http://localhost:8001/health | Osteoporosis AI |
| **Chatbot** | http://localhost:8002/chat/health | Senta AI |
| **Prometheus** | http://localhost:9090 | Metrics |
| **Grafana** | http://localhost:3000 | Dashboard (admin/admin) |

### 6. Login Default

| Role | Email | Password |
|---|---|---|
| Admin | admin@medikasentosa.id | arie12345 |
| (Seed data lain otomatis dibuat oleh DataSeeder) | | |

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
