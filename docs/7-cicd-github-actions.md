# 🔄 Cara CI/CD (GitHub Actions)

> Otomasi build, test, dan deploy setiap kali push ke GitHub.

---

## Overview

```
git push → GitHub Actions → [Test] → [Build] → [Deploy (opsional)]
                                  │
                                  ├── Backend: Java 21 + Gradle + PostgreSQL
                                  ├── Frontend: Node 20 + JavaScript + Vite
                                  ├── ML Service: Python 3.11 import check
                                  └── Docker: Build semua images
```

---

## Workflow yang Aktif

### `ci.yml` — Build & Test

**Trigger:** Push ke `main`/`develop`, PR ke `main`

| Job | Apa yang dilakukan | Runtime |
|---|---|---|
| `backend-test` | Build Java + run JUnit tests dengan PostgreSQL container | ubuntu-latest |
| `ml-service-check` | Install Python deps + verify imports | ubuntu-latest |
| `frontend-check` | JavaScript test + Vite build | ubuntu-latest |
| `docker-build` | Build semua Docker images (depends on 3 jobs di atas) | ubuntu-latest |

---

## Setup GitHub Secrets

Buka repo → **Settings** → **Secrets and variables** → **Actions**

### Wajib (untuk test)

| Secret | Value | Keterangan |
|---|---|---|
| `POSTGRES_PASSWORD` | _(secret GitHub, nilai acak)_ | Password PostgreSQL di CI |

### Untuk Deploy (opsional)

| Secret | Value |
|---|---|
| `EC2_HOST` | Public IP EC2 instance |
| `EC2_SSH_KEY` | Isi file `.pem` (private key) |
| `AWS_ACCESS_KEY_ID` | Dari IAM user |
| `AWS_SECRET_ACCESS_KEY` | Dari IAM user |
| `AWS_REGION` | `ap-southeast-1` |

---

## Cara Kerja

### 1. Push ke GitHub

```bash
git add .
git commit -m "feat: tambah fitur baru"
git push origin main
```

### 2. GitHub Actions Trigger

Buka https://github.com/arityo182/portofolio-belajar/actions

Akan ada workflow baru yang running.

### 3. Tunggu Selesai

Klik workflow → klik job → lihat logs real-time.

**Expected result:**
```
✅ Backend — Java 21 Tests     (PASS)
✅ ML Service — Import Check   (PASS)
✅ Frontend — JavaScript + Build (PASS)
✅ Docker — Build Images        (PASS)
```

### 4. Deploy (opsional)

Jika `deploy-aws.yml` sudah di-uncomment, deploy otomatis jalan setelah CI pass.

---

## Detail Setiap Job

### Backend Test

```yaml
services:
  postgres:  # PostgreSQL 16 container
    image: postgres:16-alpine
    env:
      POSTGRES_DB: medika_sentosa
      POSTGRES_USER: core
      POSTGRES_PASSWORD: ${{ secrets.POSTGRES_PASSWORD }}

steps:
  - Setup JDK 21
  - Setup Gradle (dengan caching)
  - ./gradlew test
  - Upload test results sebagai artifact
```

**Yang di-test:**
- 22 unit test files (service layer)
- Semua feature modules: auth, appointment, billing, chat, dll
- Build success = semua test pass

### Frontend Check

```yaml
steps:
  - Setup Node.js 20 (dengan npm cache)
  - npm ci
  - npm test                # Vitest unit tests
  - npm run build           # Vite production build
  - Upload dist/ sebagai artifact
```

**Yang di-check:**
- Unit test frontend (JavaScript) lolos
- Build success = semua komponen compile

### ML Service Check

```yaml
steps:
  - Setup Python 3.11
  - pip install dependencies
  - python -c "from app.main import app"  # Import check
  - Check model file existence
```

**Yang di-check:**
- Semua import berhasil
- Model file `.keras` (skip jika tidak ada — too large for Git)

### Docker Build

```yaml
needs: [backend-test, ml-service-check, frontend-check]

steps:
  - Build backend image (multi-stage)
  - Build frontend deps image (medika-frontend-deps)
  - Build frontend image (multi-stage + Nginx)
  - Verify images exist
```

**Hanya jalan jika 3 job di atas PASS.**

---

## Artifacts

Setiap job meng-upload artifacts yang bisa didownload:

| Artifact | Isi | Retention |
|---|---|---|
| `backend-test-results` | HTML test report | 7 hari |
| `frontend-dist` | Built frontend files | 7 hari |

Download: Klik workflow → klik job → scroll ke **Artifacts** section.

---

## Caching

GitHub Actions otomatis cache:

| What | Cache Key | Location |
|---|---|---|
| Gradle dependencies | `gradle-*` | `~/.gradle/caches` |
| npm packages | `npm-*` | `node_modules` |
| Docker layers | `type=gha` | GitHub Actions cache |

**Impact:** Build kedua ~50% lebih cepat.

---

## Workflow Deploy (AWS EC2)

### `deploy-aws.yml` — Deploy to EC2

**Status:** COMMENTED (uncomment saat deploy)

**Trigger:** Push ke `main` + CI passed

```yaml
steps:
  1. SSH ke EC2
  2. cd /opt/medika
  3. git pull origin main
  4. docker compose down
  5. docker compose build --no-cache
  6. docker compose up -d
  7. docker compose exec backend ./gradlew test
  8. Health check: curl http://localhost:8080/api/health
```

### Cara Aktifkan

1. Buka `.github/workflows/deploy-aws.yml`
2. Hapus semua `# ` di awal baris (uncomment)
3. Set GitHub secrets: `EC2_HOST`, `EC2_SSH_KEY`
4. Push ke `main`

---

## Menambah Job Baru

Contoh: tambah job untuk lint Python:

```yaml
  python-lint:
    name: Python — Lint Check
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with:
          python-version: '3.11'
      - run: pip install ruff
      - run: ruff check ml-service/ chatbot-service/
```

---

## Troubleshooting

### Workflow tidak trigger
```bash
# Cek branch name — harus main atau develop
git branch

# Cek .github/workflows/ ada file .yml
ls .github/workflows/
```

### Backend test gagal
```bash
# Cek logs di GitHub Actions
# Biasanya: PostgreSQL connection refused
# Pastikan service container jalan
```

### Frontend build gagal
```bash
# Cek error frontend locally
cd hospital-frontendjs
npm test
npm run build
```

### Docker build timeout
```bash
# GitHub Actions punya timeout 60 menit
# Jika terlalu lama, cek:
# - .dockerignore sudah benar
# - Tidak ada COPY yang tidak perlu
```

### Deploy gagal
```bash
# Cek GitHub Secrets sudah benar
# Cek EC2 security group terbuka
# Cek SSH key valid
ssh -i key.pem ubuntu@EC2_IP
```

---

## Monitoring

### Badge di README

Tambahkan badge ke `README.md`:

```markdown
![CI](https://github.com/arityo182/portofolio-belajar/actions/workflows/ci.yml/badge.svg)
```

### Notifikasi

- **Email:** Otomatis jika workflow fail (diaktifkan di GitHub Settings)
- **Slack:** Bisa integrasi via Slack GitHub Action
- **Status Badge:** Tampilkan di README atau website

---

## Best Practices

1. **Jangan commit langsung ke main** — pakai PR + branch protection
2. **Test locally dulu** — sebelum push
3. **Gunakan secrets** — jangan hardcode passwords
4. **Cache dependencies** — sudah otomatis diaktifkan
5. **Upload artifacts** — untuk debugging jika build gagal
6. **Branch protection** — require PR review + CI pass sebelum merge
