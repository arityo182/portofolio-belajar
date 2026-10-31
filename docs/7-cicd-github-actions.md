# 🔄 CI/CD — GitHub Actions

> Alur: PR → test → merge ke `develop` → staging. Merge ke `main` → production.

---

## Alur branch

```text
feature/* --PR--> develop --merge--> CI lulus --> build image --> deploy STAGING
develop   --PR--> main    --merge--> CI lulus --> build image --> deploy PRODUCTION
```

- **PR** hanya menjalankan test. Tidak ada build/deploy dari PR.
- **Push/merge ke `develop`** menjalankan test, build image `staging` + `sha-<commit>`, lalu deploy staging.
- **Push/merge ke `main`** menjalankan test, build image `latest` + `sha-<commit>`, lalu deploy production.
- `main` dan `develop` sebaiknya dilindungi: wajib PR + status check CI hijau.

---

## Workflow

### `ci.yml` — test & validasi

Trigger:

- `pull_request` ke `develop` dan `main`
- `workflow_call` (dipanggil oleh `deploy.yml`)

Job:

| Job | Isi |
|---|---|
| `backend-test` | `./gradlew test` (H2, tanpa PostgreSQL service) |
| `frontend-check` | `npm ci`, `npm run lint`, `npm test`, `npm run build` |
| `python-check` | `python -m compileall ml-service/app chatbot-service/app` |
| `compose-check` | `docker compose ... config -q` untuk compose utama, staging, production |

### `deploy.yml` — build & deploy

Trigger: `push` ke `develop` atau `main`, kecuali perubahan dokumentasi saja.

Urutan job:

```text
ci (reusable) → build-push → deploy-staging  (jika develop)
                           └→ deploy-production (jika main)
```

Fitur:

- Deploy **tidak akan jalan** kalau salah satu job CI gagal.
- Image ditandai dengan **`sha-<commit>`** agar versi yang di-deploy pasti.
- Compose staging/production memakai `DOCKERHUB_USERNAME` dan `IMAGE_TAG`.
- Deploy memakai `docker compose up -d --wait`, lalu health check dengan retry.
- `concurrency` mencegah dua deploy di environment yang sama saling menimpa.

---

## ML service & chatbot

Saat ini **tidak di-deploy**:

- Block `ml-service` dan `chatbot` di `deploy/docker-compose.staging.yml` dan `deploy/docker-compose.prod.yml` tetap dikomentari.
- Matrix build hanya berisi `backend` dan `frontend`.
- CI hanya menjalankan compile-check Python, bukan build/deploy image.

Untuk mengaktifkan kembali nanti:

1. Uncomment service di compose staging/production.
2. Tambahkan lagi matrix `ml-service` dan `chatbot` di `.github/workflows/deploy.yml`.
3. Isi secret `ML_INTERNAL_API_KEY`, `CHATBOT_INTERNAL_API_KEY`, dan `VLLM_API_KEY`.
4. Pastikan GPU/vLLM tersedia.

---

## Secrets

### Repository secrets

| Secret | Dipakai untuk |
|---|---|
| `DOCKERHUB_USERNAME` | Nama akun DockerHub dan prefix image |
| `DOCKERHUB_TOKEN` | Login DockerHub saat build & push |
| `EC2_HOST` | Host VPS deploy |
| `EC2_USER` | User SSH VPS |
| `EC2_SSH_KEY` | Private key SSH |

### GitHub Environment

Gunakan environment `staging` dan `production`:

- `staging` boleh auto-deploy.
- `production` sebaiknya memakai **required reviewer** agar merge ke `main` tidak langsung men-deploy tanpa persetujuan.
- Jika staging dan production berada di server berbeda, simpan `EC2_*` sebagai **environment secret**, bukan repository secret.

### File `.env` di server

Compose membaca `.env` dari direktori deploy:

- `~/medika-staging/.env`
- `~/medika/.env`

Isi minimal:

```dotenv
POSTGRES_DB=medika_sentosa
POSTGRES_USER=core
POSTGRES_PASSWORD=<password-kuat>
REDIS_PASSWORD=<password-kuat>
JWT_SECRET=<minimal-32-byte-acak>
ADMIN_PASSWORD=<password-admin-awal>
GRAFANA_PASSWORD=<password-grafana>
```

Catatan:

- `.env` **tidak boleh** di-commit.
- `DOCKERHUB_TOKEN` tidak perlu ada di `.env` server; token hanya dipakai GitHub Actions.
- `POSTGRES_PASSWORD` tidak lagi diperlukan untuk CI karena backend test memakai H2.

---

## Image tag

| Branch | Tag channel | Tag immutable |
|---|---|---|
| `develop` | `staging` | `sha-<commit>` |
| `main` | `latest` | `sha-<commit>` |

Deploy selalu memakai `sha-<commit>`, bukan `latest`, supaya build lama tidak menimpa build baru.

---

## Deploy manual / rollback

Deploy normal terjadi lewat merge. Untuk rollback:

1. Cari commit terakhir yang sehat.
2. Checkout commit tersebut di branch yang sesuai, atau jalankan ulang workflow pada commit itu.
3. Pastikan image `sha-<commit>` masih ada di DockerHub.

Rollback database belum otomatis. Lakukan backup sebelum migrasi schema production.

---

## Troubleshooting

### Deploy staging gagal di health check

```bash
ssh user@VPS
cd ~/medika-staging
docker compose -f docker-compose.staging.yml ps
docker compose -f docker-compose.staging.yml logs --tail=100 backend
```

### Image tidak ditemukan

Pastikan `DOCKERHUB_USERNAME` di secret sama dengan prefix image di compose, dan image `sha-<commit>` berhasil di-push.

### `.env` belum ada

Workflow akan berhenti dengan pesan:

```text
ERROR: ~/medika-staging/.env belum ada
```

Buat file tersebut di server sebelum deploy pertama.

### Production gagal start

Periksa:

- `SPRING_PROFILES_ACTIVE=production`
- `JWT_SECRET`, `POSTGRES_*`, `REDIS_PASSWORD`
- schema database sudah sesuai (`ddl-auto=validate`)
