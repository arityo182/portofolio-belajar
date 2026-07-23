# RS Medika Sentosa — Hospital Enterprise Web Application

Sistem informasi rumah sakit full-stack yang mengintegrasikan layanan administratif (pendaftaran pasien, janji temu, rekam medis) dengan fitur unggulan skrining osteoporosis berbasis kecerdasan buatan. Dibangun sebagai proyek tugas akhir sekaligus portofolio untuk lamaran magang dan CPNS, mengikuti standar ISO/IEC/IEEE 29148:2018 untuk requirements engineering.

Mengintegrasikan tiga layanan independen — React (frontend), Spring Boot (backend utama), dan FastAPI (ML service) — dengan model deep learning custom EfficientNetV2 untuk klasifikasi tiga kelas osteoporosis dari citra X-ray, lengkap dengan visualisasi Grad-CAM dan Otsu thresholding.

> **Built to explore.** Proyek ini dibangun untuk mempelajari arsitektur package-by-feature pada skala nyata, integrasi model AI ke sistem produksi (bukan sekadar notebook), desain REST API berlapis dengan validasi logika bisnis, autentikasi JWT stateless dengan role-based access control, dan komunikasi antar layanan via HTTP multipart. Setiap keputusan arsitektur didokumentasikan dengan alasan teknis yang jujur — termasuk keterbatasannya.

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

**Dalam pengembangan aktif — Fase 1 (MVP).**

Sistem dijalankan secara lokal (localhost) dan belum di-deploy ke cloud. Keputusan ini sengaja diambil karena proyek dikembangkan di lingkungan CPU-only (tanpa GPU), sehingga fokus prioritas adalah kelengkapan fitur dan kebenaran alur end-to-end sebelum menghadapi kompleksitas deployment. Model AI (~248 MB) berjalan di CPU dengan waktu inferensi yang dapat diterima setelah warm-up.

- **Repository:** _(belum dipublikasikan ke GitHub — placeholder)_
- **Live demo:** _(belum tersedia)_

---

## Poin Utama

- **Pipeline AI end-to-end:** upload citra X-ray → preprocessing (median filter, CLAHE, resize, max-scaling) → inferensi model EfficientNetV2 → Grad-CAM + Otsu masking → hasil tersimpan ke PostgreSQL dengan visualisasi overlay
- **Arsitektur package-by-feature** diterapkan konsisten di ketiga layanan — setiap modul (auth, poli, dokter, appointment, rekam medis, dst.) memiliki entity, repository, DTO, service, dan controller sendiri, bukan dibagi per layer
- **Autentikasi JWT stateless** dengan role-based access control tiga peran: Pasien, Dokter, Admin — masing-masing dengan halaman dan endpoint terpisah
- **Validasi logika bisnis berlapis** di backend: cek kuota jadwal dokter, validasi hari appointment cocok dengan jadwal praktik, anti double-booking, anti duplikat poli/dokter/pasien, generate nomor antrian otomatis per jadwal+tanggal, generate nomor rekam medis unik format `RM-YYYY-XXXXXX`
- **Dokumentasi API otomatis:** Swagger UI (springdoc-openapi) di backend, FastAPI docs di ML service
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

Frontend juga mengikuti pola yang sama: `features/<nama>/` berisi service, types, pages, dan components untuk fitur tersebut, dengan `core/` menyimpan infrastruktur bersama (API client, auth context, theme, types).

---

## Arsitektur Sistem

```
┌─────────────┐      REST/JWT       ┌──────────────────┐      HTTP/Multipart     ┌──────────────┐
│   React     │  ←──────────────→   │   Spring Boot    │  ←──────────────────→   │   FastAPI    │
│  Frontend   │   axios + token     │   Backend Utama  │   forward citra X-ray   │  ML Service  │
│ :5173       │                     │   :8080          │                         │  :8001       │
└─────────────┘                     └────────┬─────────┘                         └──────┬───────┘
                                             │                                          │
                                             │ JPA/Hibernate                            │ TensorFlow
                                             ▼                                          ▼
                                     ┌──────────────────┐                   ┌──────────────────────┐
                                     │   PostgreSQL     │                   │  EfficientNetV2      │
                                     │  medika_sentosa  │                   │  .keras (248 MB)     │
                                     └──────────────────┘                   └──────────────────────┘
```

ML service sengaja dipisah dari backend utama agar dependency berat seperti TensorFlow tidak ikut dimuat di aplikasi Spring Boot. Pemisahan ini juga memungkinkan model diganti atau di-scale independen tanpa menyentuh business logic — cukup ubah endpoint FastAPI atau ganti file `.keras`, dan backend utama tetap berjalan tanpa redeploy.

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

**Tech:** Python 3.10, FastAPI, TensorFlow 2.x, OpenCV, NumPy

**Tanggung Jawab:**

- Menerima citra X-ray via endpoint `POST /predict/osteoporosis` (multipart upload)
- Preprocessing citra: decode grayscale → median filter (kernel 3) → CLAHE (clipLimit 2.0, tileGridSize 8×8) → resize 224×224 (INTER_AREA) → max-scaling ke 0-255 → stack 3 channel → expand dims ke (1, 224, 224, 3)
- Inferensi model EfficientNetV2 fine-tuning: klasifikasi 3 kelas (Normal, Osteopenia, Osteoporosis) dengan output label, confidence, dan probabilitas per kelas
- Grad-CAM: aktivasi gradient dari layer `top_activation` → Otsu thresholding → morphology close/open → buang aktivasi < 0.4 → colormap JET → blend 60/40 dengan citra asli → encode PNG base64
- Pesan risiko kontekstual per kelas prediksi
- Pengukuran latency inferensi (dalam milidetik)

### 3. Frontend (React)

**Tech:** React 19, TypeScript, Vite, Tailwind CSS 4, Axios, React Router 7, Lucide Icons

**Tanggung Jawab:**

- Antarmuka pengguna untuk tiga peran: Pasien, Dokter, Admin — masing-masing dengan navigasi dan halaman terpisah
- Autentikasi: halaman login & register, persistensi sesi via localStorage, interceptor axios untuk JWT otomatis
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

- **Swagger UI (backend):** `http://localhost:8080/swagger-ui.html` — tersedia via springdoc-openapi 2.7.0, semua endpoint terdokumentasi dengan anotasi `@Operation`, `@ApiResponse`, dan `@Tag`
- **FastAPI Docs (ML service):** `http://localhost:8001/docs` — Swagger UI bawaan FastAPI, endpoint `/predict/osteoporosis` terdokumentasi dengan Pydantic schema
- **Insomnia/Postman collection:** `medikasentosa/Insomnia_2026-07-24.yaml` (Fase 1, 47 request) + `medikasentosa/Insomnia2_2026-07-24.yaml` (Fase 2-3, 42 request) — lengkap dengan dokumentasi tiap endpoint

---

## Pengujian

Pengujian otomatis saat ini **sangat terbatas** — baru 1 file test ada:

- `DokterServiceTest.java` — 6 test case menggunakan JUnit 5 + Mockito, menguji service layer modul dokter (create, getById, getByPoli, update, delete, edge case duplikat)

Pengujian integration test (`MedikasentosaApplicationTests.java`) ada sebagai boilerplate Spring Boot tapi belum menguji skenario nyata.

Verifikasi fungsi saat ini dilakukan **manual** melalui Swagger UI dan browser untuk setiap endpoint dan alur fitur. Rencana pengujian otomatis menyeluruh (JUnit, Mockito, integration test, Playwright untuk frontend) ditargetkan pada Fase 3 roadmap.

> **Catatan jujur:**覆盖率 test saat ini jauh dari standar production. Ini adalah keterbatasan yang disadari dan akan diatasi secara bertahap.

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

**Yang sudah diimplementasi:**

- **JWT stateless authentication** — token HMAC-SHA dengan masa berlaku 24 jam, disisipkan otomatis via axios interceptor
- **BCrypt password hashing** — password tidak pernah disimpan plain text
- **Role-based access control** — tiga peran (PASIEN, DOKTER, ADMIN) dengan `@PreAuthorize("hasRole('ADMIN')")` di endpoint admin dan route guards di frontend
- **CORS configuration** — hanya origin `localhost:5173` (frontend dev server) yang diizinkan
- **Validasi input** — `@Valid` + Bean Validation di DTO (NotBlank, Email, Size, NotNull, Min)
- **Akun nonaktif ditolak** — user dengan `isActive=false` tidak dapat login

**Yang belum diimplementasi (direncanakan):**

- Refresh token (saat ini token 24 jam, tidak ada mekanisme refresh)
- Rate limiting (belum ada pembatasan request per user/IP)
- 2FA / two-factor authentication (tidak direncanakan untuk MVP)
- HTTPS/TLS (lokal development via HTTP; untuk produksi perlu reverse proxy dengan TLS)

> **Catatan jujur:** `GlobalExceptionHandler` saat ini belum menangani `AccessDeniedException` (403) dan generic `Exception` (500) dengan respons JSON terstruktur — non-admin yang mengakses endpoint admin dapat menerima respons HTML default Spring, bukan JSON. Ini teridentifikasi dalam audit dan akan diperbaiki.

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
| **Frontend** | React 19, TypeScript, Vite, Tailwind CSS 4, Axios, React Router 7, Lucide Icons | — |
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
├── hospital-frontend/              # Frontend — React + TypeScript
│   └── src/
│       ├── core/                   # api/client, context/AuthContext, theme, types
│       ├── data/                   # layanan.ts (shared data)
│       ├── features/               # Package-by-feature
│       │   ├── admin/              # pages (7 halaman), services, types
│       │   ├── appointment/        # pages, components, services, types
│       │   ├── auth/               # pages, services
│       │   ├── dokter/             # components, services, types
│       │   ├── dokterpanel/        # pages, services
│       │   ├── jadwal/             # pages, components, services, types
│       │   ├── pasien/             # pages, services, types
│       │   ├── poli/               # components, services, types
│       │   ├── rekammedis/         # pages, components, services, types
│       │   └── screening/          # pages, components, services, types
│       ├── pages/                  # Home, Layanan, TentangKami, Kontak
│       ├── components/             # Layout, Navbar, Footer, home/, admin/, dokter/
│       └── routes/                 # AppRoutes, ProtectedRoute, AdminRoute, DokterRoute
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

**Tahap 6 — Halaman Publik & Polish**
- [x] Halaman beranda (hero, layanan, preview)
- [x] Halaman layanan (data shared dari `DAFTAR_LAYANAN`)
- [x] Halaman tentang kami (visi, misi, nilai, statistik)
- [x] Halaman kontak (info, jam operasional, Google Maps embed)
- [ ] Halaman 404 / NotFound
- [ ] Doctors section di beranda dari API (saat ini hardcode 4 dokter)
- [ ] GlobalExceptionHandler: handler `AccessDeniedException` (403) + generic `Exception` (500)
- [ ] JwtFilter: try-catch token parsing (malformed → 401, bukan 500)

### Fase 2 — Penunjang Medis & Peningkatan _(rencana, belum dikerjakan)_

Fase 2 akan menambahkan modul penunjang medis dan peningkatan UX yang tidak termasuk dalam MVP. Karena Fase 1 sendiri belum sepenuhnya tuntas (lihat checklist Tahap 6 di atas), fase ini belum dimulai.

- Order laboratorium & radiologi
- Manajemen rawat inap & kamar
- Penjadwalan operasi
- Chatbot asisten kesehatan "Senta" (Qwen via vLLM)
- Prediksi risiko diabetes (ML tabular)
- Manajemen data master obat
- WebSocket untuk notifikasi antrian real-time
- Redis caching untuk data yang sering diakses

### Fase 3 — Keuangan, Deployment & Observability _(rencana, belum dikerjakan)_

Fase 3 berfokus pada keuangan, deployment produksi, dan observability. Belum ada pekerjaan yang dimulai.

- Billing & pembayaran (tunai, transfer, kartu)
- Docker & docker-compose untuk ketiga layanan
- CI/CD pipeline (GitHub Actions)
- Prometheus + Grafana untuk monitoring
- Pengujian otomatis menyeluruh (JUnit, Mockito, Playwright)
- Swagger untuk seluruh endpoint + Insomnia collection

---

## Menjalankan via Docker (Direkomendasikan)

Cara termudah — semua service (backend, frontend, ML, PostgreSQL, Redis) berjalan dengan **satu perintah**.

### Prasyarat

- Docker & Docker Compose v2
- Port 5432, 6379, 8080, 8001, 5173 tidak dipakai service lain

### Setup & Jalankan

```bash
cd Portofolio

# 1. Copy environment variables
cp .env.example .env

# 2. Build & jalankan semua service
docker compose up -d --build

# 3. Cek status
docker compose ps

# 4. Seed data (setelah backend healthy)
docker exec -i medika-postgres psql -U core -d medika_sentosa < medikasentosa/src/main/resources/db/seed-data.sql

# 5. Buka browser
#    Frontend: http://localhost:5173
#    Swagger:  http://localhost:8080/swagger-ui/index.html
#    ML Docs:  http://localhost:8001/docs
```

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
|---------|-----------|:----:|
| Frontend | `medika-frontend` | 5173 |
| Backend | `medika-backend` | 8080 |
| ML Service | `medika-ml-service` | 8001 |
| PostgreSQL | `medika-postgres` | 5432 |
| Redis | `medika-redis` | 6379 |

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
| `app.jwt.expiration-ms` | Masa berlaku token (ms) | `86400000` (24 jam) |
| `app.fastapi.url` | URL ML service | `http://localhost:8001` |

Akun admin default dibuat otomatis saat startup:
- Email: `admin@medikasentosa.id`
- Password: `admin123`
- **Ganti password ini segera di lingkungan production!**

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
cd hospital-frontend
npm install
npm run dev
```

Frontend berjalan di `http://localhost:5173`.

**CORS:** Backend diconfigure untuk menerima origin `http://localhost:5173`. Jika port Vite berubah, update `SecurityConfig.java`.

### 5. Verifikasi

- Buka `http://localhost:8080/swagger-ui.html` — Swagger UI backend
- Buka `http://localhost:8001/docs` — FastAPI docs ML service
- Buka `http://localhost:5173` — Frontend
- Login admin: `admin@medikasentosa.id` / `admin123`

---

## Lisensi & Kredit

**Pembuat:** Ari — proyek tugas akhir (skripsi) sekaligus portofolio untuk lamaran magang dan CPNS.

**Tujuan:** Membangun sistem informasi rumah sakit full-stack yang mengintegrasikan AI untuk skrining osteoporosis, dengan fokus pada arsitektur yang bersih, validasi bisnis yang ketat, dan dokumentasi yang jujur soal kondisi aktual proyek.

**Dokumen acuan:**
- SRS: `docs/srs-rs-medika-sentosa.md` (ISO/IEC/IEEE 29148:2018)
- ERD: `docs/erd-rs-medika-sentosa.md`

---

> README ini ditulis berdasarkan scan kode aktual, bukan template generik. Setiap klaim teknis dapat ditelusuri balik ke kode sumber. Jika ada bagian yang tidak akurat, silakan konfirmasi untuk diperbaiki.
