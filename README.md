# RS Medika Sentosa — Hospital Enterprise Web Application

[![CI](https://github.com/arityo182/portofolio-belajar/actions/workflows/ci.yml/badge.svg)](https://github.com/arityo182/portofolio-belajar/actions/workflows/ci.yml)
[![Tests](https://img.shields.io/badge/tests-22%20files%20passed-brightgreen)]()
[![Java](https://img.shields.io/badge/java-21-blue)]()
[![Spring Boot](https://img.shields.io/badge/spring--boot-4.1.0-green)]()
[![React](https://img.shields.io/badge/react-19-61dafb)]()
[![Docker](https://img.shields.io/badge/docker-ready-2496ED)]()
[![Prometheus](https://img.shields.io/badge/monitoring-active-orange)]()
[![K8s](https://img.shields.io/badge/k8s-ready-326CE5)]()

Sistem informasi rumah sakit full-stack — microservice dengan 7 container — mengintegrasikan layanan administratif, skrining osteoporosis berbasis AI, chatbot asisten virtual "Senta", monitoring Prometheus+Grafana, CI/CD pipeline, dan deployment Docker multi-server. Dibangun sebagai template belajar arsitektur enterprise sekaligus portofolio.

---

## Daftar Isi

- [Status Proyek](#status-proyek)
- [Poin Utama](#poin-utama)
- [Screenshot](#screenshot)
- [Evolusi Sistem](#evolusi-sistem)
- [Arsitektur Sistem](#arsitektur-sistem)
- [Layanan (Services Breakdown)](#layanan-services-breakdown)
- [Alur Skrining Osteoporosis](#alur-skrining-osteoporosis)
- [Dokumentasi API](#dokumentasi-api)
- [Pengujian](#pengujian)
- [Fitur Utama](#fitur-utama)
- [Keamanan](#keamanan)
- [Performa & Reliabilitas](#performa--reliabilitas)
- [Tech Stack](#tech-stack)
- [Struktur Project](#struktur-project)
- [Roadmap Pengembangan](#roadmap-pengembangan)
- [Menjalankan Secara Lokal](#menjalankan-secara-lokal)
- [Lisensi & Kredit](#lisensi--kredit)

---

## Status Proyek

**Template belajar — Docker-ready, production-ready.**

Semua service berjalan di Docker Compose (8 service: postgres, redis, backend, frontend, chatbot, ml-service, prometheus, grafana). Build offline didukung via `setup.sh` — download dependencies sekali, rebuild selanjutnya tanpa internet. CI/CD pipeline aktif di GitHub Actions. Monitoring Prometheus + Grafana dengan 6 alert rules.

- **Repository:** [github.com/arityo182/portofolio-belajar](https://github.com/arityo182/portofolio-belajar)
- **Live demo:** _(belum deploy ke cloud — rencana: 2 VPS + 1 GPU server)_

---

## Poin Utama

- **Pipeline AI end-to-end:** upload citra X-ray → preprocessing (median filter, CLAHE, resize, max-scaling) → inferensi model EfficientNetV2 → Grad-CAM + Otsu masking → hasil tersimpan ke PostgreSQL dengan visualisasi overlay
- **Arsitektur package-by-feature** diterapkan konsisten di ketiga layanan — setiap modul (auth, poli, dokter, appointment, rekam medis, dst.) memiliki entity, repository, DTO, service, dan controller sendiri, bukan dibagi per layer
- **Autentikasi JWT** dengan access token singkat (15 menit) di memori, refresh token HttpOnly, revokasi berbasis token version, dan role-based access control tiga peran: Pasien, Dokter, Admin
- **Validasi logika bisnis berlapis** di backend: cek kuota jadwal dokter, validasi hari appointment cocok dengan jadwal praktik, anti double-booking, anti duplikat poli/dokter/pasien, generate nomor antrian otomatis per jadwal+tanggal, generate nomor rekam medis unik format `RM-YYYY-XXXXXX`
- **Dokumentasi API otomatis:** Swagger UI (springdoc-openapi) di backend dan FastAPI docs di ML service — nonaktif secara default, diaktifkan hanya untuk development
- **Panel terpisah per peran:** admin panel (kelola poli/dokter/jadwal/pasien/antrian/pengguna), panel dokter (antrian pasien + buat rekam medis & resep), halaman pasien (booking, profil, riwayat rekam medis)

---

## Screenshot

> **Catatan:** Screenshot belum tersedia. Letakkan tangkapan layar di `./assets/screenshots/` dan perbarui section ini.
>
> Screenshot yang direkomendasikan untuk diambil:
> - Halaman beranda (hero + layanan + preview dokter)
> - Alur booking appointment (pilih poli → dokter → jadwal → submit → nomor antrian)
> - Hasil skrining osteoporosis (label + confidence bar + Grad-CAM overlay)
> - Admin panel (dasbor + manajemen poli/dokter)
> - Panel dokter (antrian pasien + form rekam medis + resep)

---

## Evolusi Sistem

Proyek ini dimulai dengan struktur per-layer (controller/service/repository dipisah berdasarkan jenis layer, bukan domain) — pendekatan umum yang terasa rapi saat jumlah modul masih sedikit. Namun seiring modul bertambah menjadi 10+ fitur (auth, screening, poli, dokter, jadwal, pasien, appointment, antrian, rekam medis, resep, admin), struktur per-layer mulai terasa tidak nyaman untuk maintenance: perubahan satu fitur menyentuh folder yang berbeda-beda, sulit melihat "apa saja yang dimiliki fitur ini", dan duplikasi pola sering terjadi tanpa disadari.

Refactor total ke **package-by-feature** dilakukan di ketiga layanan. Setiap modul sekarang mandiri — semua layer (entity, repository, DTO, service, controller) berada dalam satu folder fitur. Pola ini membuat onboarding fitur baru lebih cepat dan dampak perubahan lebih terprediksi. Trade-off: ada sedikit duplikasi struktur antar fitur, tapi kemudahan navigasi jauh menggantinya.

Frontend juga mengikuti pola yang sama: `features/<nama>/` berisi service, pages, dan components untuk fitur tersebut, dengan `core/` menyimpan infrastruktur bersama (API client, auth context, theme).

---

## Arsitektur Sistem

```
┌──────────────────────────────────────────────────────────────┐
│                      DOCKER COMPOSE                           │
│                                                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │ Frontend │  │ Backend  │  │ Chatbot  │  │ ML Svc   │    │
│  │ Nginx:80 │──│ :8080    │──│ :8002    │  │ :8001    │    │
│  └──────────┘  └────┬─────┘  └────┬─────┘  └──────────┘    │
│                     │             │                           │
│               ┌─────┼─────────────┼──────┐                   │
│               │     │             │      │                   │
│          ┌────┴──┐ ┌┴────┐   ┌────────┐ │                   │
│          │Postgre│ │Redis│   │ vLLM   │ │  (GPU external)   │
│          │:5432  │ │:6379│   │ :8100  │ │                   │
│          └───────┘ └─────┘   └────────┘ │                   │
│               │                         │                   │
│          ┌────┴────┐               ┌────┴────┐              │
│          │Prometheu│               │ Grafana │              │
│          │s :9090  │               │ :3000   │              │
│          └─────────┘               └─────────┘              │
└──────────────────────────────────────────────────────────────┘
```

8 service, 7 container, 1 arsitektur microservice dengan monitoring terintegrasi.

---

## Layanan (Services Breakdown)

### 1. Backend Utama (Spring Boot)

**Tech:** Java 21, Spring Boot 4.1.0, Spring Security, Spring Data JPA, PostgreSQL, springdoc-openapi 2.7.0

**Tanggung Jawab:**

- REST API untuk seluruh modul bisnis: autentikasi, poli, dokter, jadwal praktik, pasien, appointment, antrian, rekam medis, resep, dan admin
- Autentikasi JWT stateless dengan filter custom (`JwtFilter`) dan role-based access control via `@PreAuthorize`
- Validasi logika bisnis berlapis di service layer (7 validasi untuk appointment, generate nomor antrian per jadwal+tanggal, generate nomor rekam medis unik)
- Forward request skrining ke ML service via multipart form-data, menerima hasil AI, dan menyimpan ke database
- Manajemen admin: CRUD poli/dokter/jadwal/pasien, papan antrian, manajemen pengguna (aktif/nonaktifkan akun, reset password)
- Soft delete pattern untuk poli, dokter, dan jadwal (field `isActive`, bukan hapus permanen)
- Global exception handler yang menerjemahkan error bisnis ke respons JSON terstruktur (404, 409, 400)

**Endpoint yang terverifikasi (dari controller scan):**

| Method | Path | Fungsi |
|--------|------|--------|
| POST | `/api/auth/register` | Registrasi pasien baru (201) |
| POST | `/api/auth/login` | Login + token JWT (200) |
| POST | `/api/screening/upload` | Upload X-ray untuk skrining AI |
| GET | `/api/screening/history` | Riwayat skrining pasien |
| GET/POST/PUT/DELETE | `/api/poli` | CRUD poli |
| GET/POST/PUT/DELETE | `/api/dokter` | CRUD dokter + GET /poli/{id}, /user/{id} |
| GET/POST/PUT/DELETE | `/api/jadwal` | CRUD jadwal + GET /dokter/{id} |
| GET/POST/PUT | `/api/pasien` | CRUD pasien + GET /user/{id} |
| POST | `/api/appointment` | Booking janji temu (7 validasi) |
| GET | `/api/appointment/pasien/{id}` | Appointment by pasien |
| GET | `/api/appointment/dokter/{id}` | Appointment by dokter |
| PATCH | `/api/appointment/{id}/status` | Update status appointment |
| POST | `/api/antrian/generate/{id}` | Generate nomor antrian |
| GET | `/api/antrian/appointment/{id}` | Antrian by appointment |
| POST | `/api/rekam-medis` | Buat rekam medis dari appointment |
| GET | `/api/rekam-medis/pasien/{id}` | Riwayat rekam medis pasien |
| GET | `/api/rekam-medis/dokter/{id}` | Rekam medis by dokter |
| POST | `/api/resep` | Tambah resep obat |
| GET | `/api/resep/rekam-medis/{id}` | Resep by rekam medis |
| DELETE | `/api/resep/{id}` | Hapus resep |
| GET/POST/PUT/DELETE | `/api/admin/*` | 16 endpoint admin (dashboard, poli, dokter, jadwal, pasien, antrian, users) |

### 2. ML Service (FastAPI)

**Tech:** Python 3.10, FastAPI, TensorFlow 2.21, OpenCV, NumPy

### 3. Chatbot Service — Senta AI (FastAPI)

**Tech:** Python 3.11, FastAPI, ChromaDB, Sentence-Transformers, vLLM client

**Tanggung Jawab:**

- Chatbot asisten virtual RS — jawab pertanyaan layanan, dokter, jadwal, prosedur
- RAG dengan knowledge base 3 dokumen Markdown (informasi umum, layanan, prosedur)
- Embedding via Sentence-Transformers, disimpan di ChromaDB
- Koneksi ke vLLM (Qwen 2.5 14B) via HTTP OpenAI-compatible API — GPU bare-metal atau sewa (Vast.ai RTX 3090)
- Endpoint publik `/api/chat/public/message` (tanpa login) + authenticated `/api/chat/message`
- Streaming + non-streaming response
- Floating chat widget "Tanya Senta" di semua halaman publik

### 4. Frontend (React)

**Tanggung Jawab:**

- Menerima citra X-ray via endpoint `POST /predict/osteoporosis` (multipart upload)
- Preprocessing citra: decode grayscale → median filter (kernel 3) → CLAHE (clipLimit 2.0, tileGridSize 8×8) → resize 224×224 (INTER_AREA) → max-scaling ke 0-255 → stack 3 channel → expand dims ke (1, 224, 224, 3)
- Inferensi model EfficientNetV2 fine-tuning: klasifikasi 3 kelas (Normal, Osteopenia, Osteoporosis) dengan output label, confidence, dan probabilitas per kelas
- Grad-CAM: aktivasi gradient dari layer `top_activation` → Otsu thresholding → morphology close/open → buang aktivasi < 0.4 → colormap JET → blend 60/40 dengan citra asli → encode PNG base64
- Pesan risiko kontekstual per kelas prediksi
- Pengukuran latency inferensi (dalam milidetik)

### 3. Frontend (React)

**Tech:** React 19, JavaScript, Vite, Tailwind CSS 4, Axios, React Router 7, Lucide Icons

**Tanggung Jawab:**

- Antarmuka pengguna untuk tiga peran: Pasien, Dokter, Admin — masing-masing dengan navigasi dan halaman terpisah
- Autentikasi: halaman login & register, access token hanya di memori + refresh token HttpOnly, interceptor axios untuk JWT otomatis
- Alur booking appointment: wizard 4 langkah (pilih poli → dokter → jadwal → tanggal+keluhan) dengan validasi sisi klien (hari cocok, tanggal tidak masa lalu)
- Halaman "Appointment Saya": daftar janji temu + status + nomor antrian + batalkan
- Halaman profil pasien: lihat/edit data diri, tampilkan nomor rekam medis, mode "lengkapi profil" jika belum ada
- Halaman riwayat rekam medis: daftar rekam medis dengan resep obat (lazy load per kartu)
- Halaman skrining: drag & drop upload X-ray, tampilkan hasil AI (label, confidence bar, Grad-CAM overlay)
- Admin panel: dasbor statistik, manajemen poli/dokter/jadwal/pasien (CRUD), papan antrian (auto-refresh 15 detik), manajemen pengguna (toggle aktif, reset password)
- Panel dokter: antrian pasien hari ini, mulai periksa, buat rekam medis + tambah resep obat (multiple), riwayat rekam medis dokter
- Route guards: `ProtectedRoute` (butuh login), `AdminRoute` (role ADMIN), `DokterRoute` (role DOKTER)

---

## Alur Skrining Osteoporosis

1. **Pasien upload citra X-ray** melalui halaman `/osteoporosis/unggah` (drag & drop atau klik, format JPG/PNG, maks 10 MB)
2. **Frontend** mengirim request `POST /api/screening/upload` dengan header `Authorization: Bearer <JWT>` dan body multipart berisi file citra
3. **Backend Spring Boot** menerima request, mengekstrak identitas pasien dari JWT, memvalidasi file, lalu meneruskan citra via multipart form-data ke `POST /predict/osteoporosis` di ML service (FastAPI)
4. **ML service FastAPI** melakukan preprocessing: decode grayscale → median filter (k=3) → CLAHE (contrast limited adaptive histogram equalization) → resize ke 224×224 → max-scaling → stack 3 channel
5. **Model EfficientNetV2** melakukan inferensi 3 kelas: Normal, Osteopenia, Osteoporosis — menghasilkan label, confidence (%), dan probabilitas per kelas
6. **Grad-CAM** menghitung aktivasi gradient dari layer `top_activation`, lalu **Otsu thresholding** memisahkan area relevan dari background, morphology operation membersihkan noise, aktivasi < 0.4 dibuang
7. **Overlay** heatmap Grad-CAM (colormap JET) di-blend 60/40 dengan citra asli → di-encode sebagai PNG base64 data URI
8. **FastAPI** mengembalikan response JSON (label, confidence, probabilitas, risk message, gradcamImage base64, latencyMs) ke backend
9. **Backend** menyimpan hasil skrining ke PostgreSQL (label, confidence, risk, gradcamImage, latency) terkait user pasien
10. **Frontend** menerima response dan menampilkan: label prediksi, confidence bar, probabilitas per kelas, citra Grad-CAM overlay, dan pesan risiko

> **Alasan Desain:** ML service dipisah dari backend utama untuk isolasi dependency. TensorFlow (beserta CUDA jika tersedia) adalah dependency berat yang akan memperlambat startup dan menambah ukuran container backend utama jika disatukan. Dengan pemisahan ini, model dapat di-update, di-roll-back, atau di-scale secara independen tanpa redeploy aplikasi Spring Boot. Backend cukup bertindak sebagai orchestrator yang meneruskan citra dan menyimpan hasil.

---

## Dokumentasi API

- **Swagger UI (backend):** nonaktif secara default. Untuk development jalankan dengan `SWAGGER_ENABLED=true`, lalu buka `http://localhost:8080/swagger-ui.html`.
- **FastAPI Docs (ML/chatbot):** nonaktif secara default. Untuk development jalankan dengan `ENABLE_DOCS=true`; di production dokumentasi tidak boleh diekspos.
- **Insomnia/Postman collection:** `medikasentosa/Insomnia_2026-07-24.yaml` (Fase 1, 47 request) + `medikasentosa/Insomnia2_2026-07-24.yaml` (Fase 2-3, 42 request) — lengkap dengan dokumentasi tiap endpoint

---

## Pengujian

**22 test files** menggunakan JUnit 5 + Mockito, menguji service layer semua modul (appointment 10 tests, dokter, screening, auth, dll). Backend compile + test PASS di CI/CD pipeline.

Integration test tersedia (`MedikasentosaApplicationTests.java`) tapi belum menguji skenario database nyata.

Frontend: Vitest 7 tests + build `npm run build` sukses.

---

## Fitur Utama

### Pasien
- Registrasi akun & login
- Melengkapi profil pasien (NIK, tanggal lahir, alamat, dll) — nomor rekam medis digenerate otomatis
- Melihat jadwal praktik dokter per poli (drill-down: poli → dokter → jadwal)
- Booking janji temu (pilih poli → dokter → jadwal → tanggal + keluhan) dengan validasi hari & kuota
- Melihat daftar appointment + status + nomor antrian
- Membatalkan janji temu yang masih menunggu
- Melihat riwayat rekam medis beserta resep obat
- Upload citra X-ray untuk skrining osteoporosis AI

### Dokter
- Melihat antrian pasien yang booking ke dokter tersebut (filter hari ini / semua)
- Memulai pemeriksaan (ubah status appointment ke DIPERIKSA)
- Membuat rekam medis dari appointment (diagnosa, tindakan, catatan)
- Menambahkan resep obat (multiple obat per rekam medis)
- Melihat riwayat rekam medis yang pernah dibuat

### Admin
- Dasbor dengan statistik (jumlah pasien, dokter, poli, appointment)
- CRUD poli (dengan soft delete)
- CRUD dokter (buat akun user dokter + profil sekaligus, termasuk kredensial login)
- CRUD jadwal praktik dokter
- CRUD pasien (buat akun user pasien + profil sekaligus)
- Papan antrian (semua antrian + status, auto-refresh 15 detik)
- Manajemen pengguna (filter per role, aktif/nonaktifkan akun, reset password)

### Sistem
- Generate nomor antrian otomatis per jadwal + tanggal
- Generate nomor rekam medis unik format `RM-YYYY-XXXXXX`
- Status appointment: MENUNGGU → DIPERIKSA → SELESAI (atau BATAL)
- Soft delete untuk poli, dokter, jadwal (field `isActive`)
- DataSeeder: akun admin default dibuat otomatis saat startup

---

## Keamanan

### Yang sudah diimplementasi:

- **JWT stateless** — HMAC-SHA, 24 jam, auto-attach via axios interceptor
- **BCrypt password** — tidak pernah plain text
- **Role-based access** — PASIEN, DOKTER, ADMIN + route guards di frontend
- **CORS** — origin `localhost:5173` saja
- **Input validation** — `@Valid` + Bean Validation di DTO
- **Nginx security headers** — X-Content-Type-Options, X-Frame-Options, XSS-Protection, Referrer-Policy, Permissions-Policy
- **Rate limiting** — API 10 req/s, login 5 req/min (anti brute-force)
- **GlobalExceptionHandler** — 7 handler (400, 403, 404, 405, 409, 500)
- **JwtFilter try-catch** — token invalid tidak menyebabkan 500
- **Non-root Docker** — semua container jalan sebagai user non-root
- **Resource limits** — CPU & memory cap di docker-compose
- **Secret scanning** — Gitleaks di CI (0 real leaks)
- **Request ID tracking** — X-Request-ID di header + MDC logging

### Yang direncanakan:

- HTTPS/TLS (Let's Encrypt)
- Refresh token
- Secrets management (HashiCorp Vault)
- Audit logging (ELK Stack)

---

## Performa & Reliabilitas

- **Model singleton pattern:** model EfficientNetV2 (~248 MB) dimuat sekali saat modul Python di-import, tidak di-load ulang tiap request
- **Warm-up saat startup:** ML service melakukan warm-up model dengan input dummy `np.zeros((1, 224, 224, 3))` via FastAPI `lifespan` context manager, sehingga request pertama dari user tidak lambat
- **Lazy loading resep:** frontend memuat resep obat per kartu rekam medis secara lazy (saat kartu dirender), bukan memuat semua resep sekaligus — mengurangi beban network saat daftar rekam medis panjang
- **Auto-refresh papan antrian:** halaman papan antrian admin me-refresh data setiap 15 detik via interval, tanpa perlu WebSocket (cukup untuk MVP)
- **Soft delete:** poli, dokter, dan jadwal tidak dihapus permanen — field `isActive` di-set `false`, sehingga data historis (appointment, rekam medis) yang ber-relasi tetap valid

> **Catatan jujur:** Tidak ada caching (Redis), tidak ada WebSocket real-time, tidak ada connection pooling tuning khusus. Latency inferensi AI di CPU sekitar 2-5 detik per citra (setelah warm-up). Ini acceptable untuk MVP tapi perlu optimasi untuk produksi.

---

## Tech Stack

| Kategori | Digunakan Saat Ini | Roadmap (belum diimplementasi) |
|----------|-------------------|-------------------------------|
| **Backend** | Java 21, Spring Boot 4.1.0, Spring Security, Spring Data JPA, springdoc-openapi 2.7.0 | Spring Boot Actuator, Redis caching |
| **Frontend** | React 19, JavaScript, Vite, Tailwind CSS 4, Axios, React Router 7, Lucide Icons | — |
| **ML Service** | Python 3.10, FastAPI, TensorFlow 2.x, OpenCV, NumPy, Pydantic | GPU inference, model serving (TFLite) |
| **Database** | PostgreSQL | — |
| **Testing** | JUnit 5, Mockito (111 tests, 20 test files) | Playwright (E2E), integration test, coverage report |
| **Deployment** | Lokal (localhost) | ✅ Docker + docker-compose |
| **Monitoring** | — | ✅ Actuator + Prometheus + Grafana |
| **Real-time** | Polling 15 detik (papan antrian) | WebSocket untuk notifikasi antrian |

---

## Struktur Project

```
Portofolio/
├── docs/                           # Dokumentasi SRS, ERD, diagram alur
│   ├── srs-rs-medika-sentosa.md    # Software Requirements Specification
│   ├── erd-rs-medika-sentosa.md    # Entity Relationship Diagram
│   └── 3-alur-mvp.mermaid          # Diagram alur data MVP
│
├── medikasentosa/                  # Backend — Spring Boot
│   └── src/main/java/com/medikasentosa/
│       ├── features/               # Package-by-feature
│       │   ├── admin/              # controller, service, dto
│       │   ├── antrian/            # entity, repository, dto, service, controller
│       │   ├── appointment/        # entity, repository, dto, service, controller
│       │   ├── auth/               # entity, repository, dto, service, controller
│       │   ├── dokter/             # entity, repository, dto, service, controller
│       │   ├── jadwaldokter/       # entity, repository, dto, service, controller
│       │   ├── pasien/             # entity, repository, dto, service, controller
│       │   ├── poli/               # entity, repository, dto, service, controller
│       │   ├── rekammedis/         # entity, repository, dto, service, controller
│       │   ├── resep/              # entity, repository, dto, service, controller
│       │   └── screening/          # entity, repository, dto, service, controller
│       └── shared/                 # config, controller, dto, exception, security, seed
│
├── hospital-frontendjs/            # Frontend aktif — React + JavaScript
│   └── src/
│       ├── core/                   # api/client, context/AuthContext, theme
│       ├── data/                   # layanan.js (shared data)
│       ├── features/               # Package-by-feature
│       │   ├── admin/              # pages (7 halaman), services
│       │   ├── appointment/        # pages, components, services
│       │   ├── auth/               # pages, services
│       │   ├── dokter/             # components, services
│       │   ├── dokterpanel/        # pages, services
│       │   ├── jadwal/             # pages, components, services
│       │   ├── pasien/             # pages, services
│       │   ├── poli/               # components, services
│       │   ├── rekammedis/         # pages, components, services
│       │   └── screening/          # pages, components, services
│       ├── pages/                  # Home, Layanan, TentangKami, Kontak
│       ├── components/             # Layout, Navbar, Footer, home/, admin/, dokter/
│       └── routes/                 # AppRoutes, ProtectedRoute, AdminRoute, DokterRoute
│
├── hospital-frontend/              # Arsip versi TypeScript lama
│
├── ml-service/                     # ML Service — FastAPI + TensorFlow
│   ├── app/
│   │   ├── main.py                 # FastAPI app, endpoint /predict/osteoporosis, warm-up
│   │   ├── predictor.py            # Load model, inferensi 3 kelas
│   │   ├── preprocessing.py        # Median filter, CLAHE, resize, max-scaling
│   │   ├── gradcam.py              # Grad-CAM + Otsu thresholding + overlay
│   │   └── schema.py               # Pydantic response model
│   └── models/
│       └── EfficientNetV2_FineTuning.keras  # Model terlatih (248 MB)
│
└── README.md
```

---

## Roadmap Pengembangan

### Fase 1 — MVP (dalam pengembangan)

**Tahap 1 — Fondasi & Autentikasi**
- [x] SRS & ERD mengikuti ISO/IEC/IEEE 29148:2018
- [x] Struktur project package-by-feature di ketiga layanan
- [x] Entity, repository, DTO, service, controller untuk semua modul MVP
- [x] Autentikasi JWT (register, login, logout)
- [x] Role-based access control (PASIEN, DOKTER, ADMIN)
- [x] DataSeeder akun admin default

**Tahap 2 — Data Master & Booking**
- [x] CRUD poli dengan soft delete
- [x] CRUD dokter (set role user ke DOKTER saat create)
- [x] CRUD jadwal praktik (validasi jamMulai < jamSelesai)
- [x] Halaman jadwal praktik (drill-down poli → dokter → jadwal)
- [x] Booking appointment dengan 7 validasi (pasien, jadwal, hari, double-booking, kuota)
- [x] Generate nomor antrian otomatis per jadwal + tanggal
- [x] Halaman "Appointment Saya" + batalkan janji

**Tahap 3 — Pasien & Rekam Medis**
- [x] Profil pasien (view, edit, mode lengkapi jika belum ada)
- [x] Generate nomor rekam medis otomatis `RM-YYYY-XXXXXX`
- [x] Halaman riwayat rekam medis + resep obat (lazy load)
- [x] Buat rekam medis dari appointment (dari panel dokter)
- [x] Tambah resep obat (multiple per rekam medis)

**Tahap 4 — Skrining AI**
- [x] Model EfficientNetV2 fine-tuning (3 kelas)
- [x] Preprocessing pipeline (median filter, CLAHE, resize, max-scaling)
- [x] Grad-CAM + Otsu thresholding + overlay
- [x] Endpoint FastAPI `/predict/osteoporosis`
- [x] Backend forward citra ke ML service + simpan hasil
- [x] Frontend upload X-ray + tampilkan hasil (label, confidence, Grad-CAM)
- [ ] Halaman riwayat skrining pasien (service ada, UI belum)

**Tahap 5 — Panel Admin & Dokter**
- [x] Admin panel (dasbor, poli, dokter, jadwal, pasien, antrian, pengguna)
- [x] Panel dokter (antrian pasien, buat rekam medis + resep, riwayat)
- [x] Admin buat akun dokter/pasien (User + profil sekaligus)
- [x] Admin reset password pengguna
- [x] Papan antrian dengan auto-refresh

**Tahap 6 — Halaman Publik, Polish, Keamanan**
- [x] Halaman beranda, layanan, tentang kami, kontak
- [x] Halaman 404 / NotFound
- [x] GlobalExceptionHandler: 403 + 500 + request ID
- [x] JwtFilter: try-catch token parsing
- [x] Nginx security headers + rate limiting
- [x] Non-root Docker + resource limits
- [x] Secret scanning (Gitleaks)
- [x] CI/CD pipeline (GitHub Actions — 4 jobs)
- [x] Docker Compose 8 service + build offline
- [x] Monitoring Prometheus + Grafana 6 alert rules
- [x] Chatbot Senta AI (FastAPI + ChromaDB + vLLM)
- [x] Floating chat widget di semua halaman
- [x] Halaman riwayat skrining
- [x] `.env.example` + env vars untuk semua secret

### Fase 2 — Deployment, AI & Monitoring _(selesai)_
- [x] Docker Compose 8 service + multi-stage build
- [x] Docker image offline cache (`setup.sh`, `Dockerfile.deps`)
- [x] CI/CD pipeline (GitHub Actions)
- [x] Prometheus + Grafana monitoring + 6 alert rules
- [x] Chatbot Senta AI (FastAPI + ChromaDB + vLLM Qwen 2.5 14B)
- [x] Floating chat widget publik
- [x] Non-root Docker + resource limits + security headers
- [x] `.dockerignore` + `.env.example`

### Fase 3 — Production & Final _(direncanakan)_
- [ ] Deploy ke GPU server (vLLM + Chatbot + ML)
- [ ] Deploy ke 2 VPS (Backend + Frontend + DB)
- [ ] HTTPS (Let's Encrypt) + UFW firewall
- [ ] Kubernetes cluster 3 node
- [ ] Billing & pembayaran
- [ ] Order laboratorium & radiologi
- [ ] WebSocket notifikasi antrian real-time
- [ ] Redis caching (saat ini disabled — `spring.cache.type=simple`)
- [ ] Integration test + E2E Playwright
- [ ] HashiCorp Vault (secret management)
- [ ] ELK Stack (audit logging)

---

## Menjalankan via Docker (Direkomendasikan)

Cara termudah — semua service (backend, frontend, ML, PostgreSQL, Redis) berjalan dengan **satu perintah**.

### Prasyarat

- Docker & Docker Compose v2
- Port 5432, 6379, 8080, 8001, 5173 tidak dipakai service lain

### Setup & Jalankan

```bash
# 1. Download semua dependencies (sekali saja, butuh internet)
./setup.sh

# 2. Jalankan semua service
docker compose up -d

# 3. Cek status
docker compose ps

# 4. Akses
#    Frontend:     http://localhost:5173
#    Grafana:      http://localhost:3000 (password dari GRAFANA_PASSWORD)
#    Prometheus:   http://localhost:9090
#    Swagger:      nonaktif default (SWAGGER_ENABLED=true untuk development)
```

> **Offline mode:** Setelah `./setup.sh` selesai, semua dependency tersimpan di Docker layer cache. Rebuild selanjutnya (`docker compose build`) tidak butuh internet.

### Perintah Berguna

```bash
docker compose logs -f backend    # Log real-time backend
docker compose logs -f ml-service # Log ML service (ada warm-up TF)
docker compose down               # Hentikan semua
docker compose down -v            # Hapus juga volume (database)
docker compose build --no-cache   # Build ulang dari awal
```

### Service & Port

| Service | Container | Port |
|:--------|:----------|:----:|
| Frontend | `medika-frontend` | 5173 |
| Backend | `medika-backend` | 8080 |
| ML Service | `medika-ml-service` | 8001 |
| Chatbot | `medika-chatbot` | 8002 |
| PostgreSQL | `medika-postgres` | 5432 |
| Redis | `medika-redis` | 6379 |
| Prometheus | `medika-prometheus` | 9090 |
| Grafana | `medika-grafana` | 3000 |

---

## CI/CD — GitHub Actions

[![CI](https://github.com/arityo182/portofolio-belajar/actions/workflows/ci.yml/badge.svg)](https://github.com/arityo182/portofolio-belajar/actions/workflows/ci.yml)

Pipeline otomatis jalan setiap **push** ke `main`/`develop` dan **pull request**.

### Jobs

| Job | Apa yang dicek |
|-----|---------------|
| **backend-test** | `./gradlew test` — 119 unit tests (Java 21, PostgreSQL) |
| **ml-service-check** | Python imports, dependencies OK (model tidak di-load) |
| **frontend-check** | `npm test` + `npm run build` |
| **docker-build** | Build semua Docker image (setelah 3 job sukses) |

### Cara trigger

```bash
git push origin main
```

Cek hasil di tab **Actions** di GitHub → klik workflow → klik job yang fail untuk lihat log.

---

## Menjalankan Secara Lokal (Manual)

### Prasyarat

| Software | Versi | Catatan |
|----------|-------|---------|
| Java JDK | 21 | Spring Boot 4.1.0 membutuhkan Java 21+ |
| Node.js | 18+ | Untuk Vite dev server |
| Python | 3.10+ | Untuk ML service |
| PostgreSQL | 14+ | Database `medika_sentosa` |
| OS | Linux/WSL Ubuntu | Dikembangkan di WSL Ubuntu |

### 1. Database (PostgreSQL)

Buat database dan user:

```sql
CREATE DATABASE medika_sentosa;
CREATE USER core WITH PASSWORD '<password-anda>';
GRANT ALL PRIVILEGES ON DATABASE medika_sentosa TO core;
```

Skema tabel dibuat otomatis oleh Hibernate (`spring.jpa.hibernate.ddl-auto=update`).

### 2. Backend (Spring Boot)

```bash
cd medikasentosa
./gradlew bootRun
```

Backend berjalan di `http://localhost:8080`.

**Environment variables** (`src/main/resources/application.properties`):

| Variable | Deskripsi | Contoh |
|----------|-----------|--------|
| `spring.datasource.url` | URL PostgreSQL | `jdbc:postgresql://localhost:5432/medika_sentosa` |
| `spring.datasource.username` | User database | `core` |
| `spring.datasource.password` | Password database | _(rahasia)_ |
| `app.jwt.secret` | Secret key JWT | _(rahasia, minimal 32 karakter)_ |
| `app.jwt.expiration-ms` | Masa berlaku access token (ms) | `900000` (15 menit) |
| `app.jwt.refresh-expiration-ms` | Masa berlaku refresh token (ms) | `604800000` (7 hari) |
| `app.fastapi.url` | URL ML service | `http://localhost:8001` |

Akun demo (seed data, 20 users):

- Password setiap akun dibuat **acak dan unik** saat seeding; tidak ada
  password default yang dibagikan di repo.
- Kredensial lokal hanya disimpan di `CREDENTIALS.md` (di-gitignore).
- Jangan menjalankan `seed-data.sql` di production. Rotasi seluruh kredensial
  sebelum deployment bersama.

### 3. ML Service (FastAPI)

```bash
cd ml-service
source venv/bin/activate
uvicorn app.main:app --port 8001
```

ML service berjalan di `http://localhost:8001`.

> **Catatan:** `requirements.txt` belum tersedia. Dependensi yang dibutuhkan: `tensorflow`, `opencv-python`, `numpy`, `fastapi`, `uvicorn`, `pydantic`. Model `.keras` harus berada di `ml-service/models/`.

### 4. Frontend (React)

```bash
cd hospital-frontendjs
npm install
npm run dev
```

Frontend berjalan di `http://localhost:5173`.

**CORS:** Backend diconfigure untuk menerima origin `http://localhost:5173`. Jika port Vite berubah, update `SecurityConfig.java`.

### 5. Verifikasi

- Buka `http://localhost:5173` — Frontend
- Swagger UI backend nonaktif secara default; nyalakan hanya untuk development
  dengan `SWAGGER_ENABLED=true`.
- Dokumentasi FastAPI ML/chatbot juga nonaktif secara default (`ENABLE_DOCS=true`
  hanya untuk development).
- Kredensial admin awal dibuat dari `ADMIN_PASSWORD` (wajib di production).

---

## Lisensi & Kredit

**Pembuat:** Ari — proyek tugas akhir (skripsi) sekaligus portofolio untuk lamaran magang dan CPNS.

**Tujuan:** Membangun sistem informasi rumah sakit full-stack yang mengintegrasikan AI untuk skrining osteoporosis, dengan fokus pada arsitektur yang bersih, validasi bisnis yang ketat, dan dokumentasi yang jujur soal kondisi aktual proyek.

**Dokumen acuan:**
- SRS: `docs/srs-rs-medika-sentosa.md` (ISO/IEC/IEEE 29148:2018)
- ERD: `docs/erd-rs-medika-sentosa.md`

---

> README ini ditulis berdasarkan scan kode aktual, bukan template generik. Setiap klaim teknis dapat ditelusuri balik ke kode sumber. Jika ada bagian yang tidak akurat, silakan konfirmasi untuk diperbaiki.
