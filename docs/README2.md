# RS Medika Sentosa — Hospital Enterprise Web Application

RS Medika Sentosa adalah aplikasi web enterprise rumah sakit full-stack yang
mengintegrasikan layanan administratif dengan **skrining osteoporosis berbasis AI**.
Sistem memproses citra X-ray untuk memprediksi tiga kelas kondisi tulang
(Normal, Osteopenia, Osteoporosis) lengkap dengan visualisasi Grad-CAM.

> Dibangun untuk mengeksplorasi konsep rekayasa perangkat lunak dunia nyata:
> arsitektur multi-layanan, autentikasi & otorisasi, integrasi model deep learning,
> REST API, observability, dan containerization.

---

## Daftar Isi

- [Fitur Utama](#fitur-utama)
- [Arsitektur Sistem](#arsitektur-sistem)
- [Tech Stack](#tech-stack)
- [Alur Skrining Osteoporosis](#alur-skrining-osteoporosis)
- [Struktur Project](#struktur-project)
- [Roadmap Pengembangan](#roadmap-pengembangan)
- [Menjalankan Secara Lokal](#menjalankan-secara-lokal)

---

## Fitur Utama

### Fitur Pasien
- Registrasi dan login berbasis JWT
- Pendaftaran pasien dengan nomor rekam medis otomatis
- Booking janji temu (appointment) dengan dokter sesuai jadwal
- Nomor antrian otomatis per poli
- **Skrining osteoporosis dari citra X-ray dengan AI** (fitur unggulan)
- Riwayat skrining dan rekam medis
- Chatbot asisten kesehatan "Senta" *(fase lanjutan)*

### Fitur Dokter
- Pencatatan rekam medis (anamnesa, diagnosa, tindakan)
- Penulisan resep obat
- Manajemen jadwal praktik

### Fitur Admin
- Manajemen data master (poli, dokter, obat)
- Role-based access control (Pasien / Dokter / Admin)

### Fitur Sistem
- Prediksi 3 kelas osteoporosis (EfficientNetV2 + softmax)
- Visualisasi Grad-CAM dengan ROI masking (Otsu)
- Warm-up model saat startup untuk mengurangi latensi
- Global exception handling dengan respons terstruktur

---

## Arsitektur Sistem

Sistem terdiri dari tiga layanan utama yang berkomunikasi melalui REST API.

```
                         ┌──────────────────┐
                         │   React (Vite)   │
                         │   TypeScript     │
                         └────────┬─────────┘
                                  │ REST (JWT)
                                  ▼
                         ┌──────────────────┐
                         │   Spring Boot 3  │
                         │  Auth · Business │
                         │      Logic       │
                         └───┬──────────┬───┘
                             │          │
              multipart ─────┘          └───── JPA
                    ▼                          ▼
          ┌──────────────────┐      ┌──────────────────┐
          │  FastAPI + TF    │      │   PostgreSQL     │
          │  Model AI +      │      │                  │
          │  Grad-CAM        │      └──────────────────┘
          └──────────────────┘

   (opsional/roadmap) Redis cache · Prometheus + Grafana monitoring
```

**Alur singkat:** React mengirim citra X-ray ke Spring Boot (dengan JWT).
Spring Boot memvalidasi, meneruskan file ke FastAPI untuk inferensi model,
menerima hasil (label + confidence + Grad-CAM), menyimpannya ke PostgreSQL,
lalu mengembalikan ke React untuk ditampilkan.

---

## Tech Stack

| Kategori | Teknologi |
|---|---|
| **Frontend** | React, Vite, TypeScript, Tailwind CSS, Axios, Lucide Icons |
| **Backend** | Java 21, Spring Boot 3, Spring Security, Spring Data JPA |
| **Autentikasi** | JWT (JSON Web Token), BCrypt |
| **ML Service** | Python, FastAPI, TensorFlow, OpenCV, EfficientNetV2 |
| **Explainability** | Grad-CAM, Otsu ROI Masking |
| **Database** | PostgreSQL |
| **Build Tools** | Gradle (backend), Vite (frontend), venv/pip (ML) |

### Roadmap Teknologi *(fase lanjutan — belum diimplementasi penuh)*

| Kategori | Teknologi | Tujuan |
|---|---|---|
| **Cache / State** | Redis | Cache jadwal dokter, session |
| **Real-time** | WebSocket (STOMP) | Notifikasi antrian pasien real-time |
| **Containerization** | Docker, docker-compose | Deployment portabel |
| **Observability** | Prometheus, Grafana | Monitoring latensi & traffic |
| **CI/CD** | GitHub Actions | Build & test otomatis |
| **Testing** | JUnit 5, Mockito, Spring Boot Test | Unit & integration test |
| **Chatbot** | Qwen (LLM) via vLLM | Asisten kesehatan "Senta" |

> **Catatan:** Teknologi roadmap ditambahkan bertahap. Fokus utama adalah
> menyelesaikan fitur inti (MVP) dengan solid sebelum menambah lapisan
> infrastruktur. Pendekatan ini menjaga project tetap fokus dan dapat dijalankan
> secara lokal tanpa ketergantungan cloud berbayar.

---

## Alur Skrining Osteoporosis

Fitur unggulan sistem. Alur lengkap dari unggah hingga hasil:

1. Pasien mengunggah citra X-ray (JPG/PNG, maks 10MB) melalui antarmuka React.
2. Frontend mengirim file ke Spring Boot beserta token JWT.
3. Spring Boot memvalidasi autentikasi dan format file.
4. Spring Boot meneruskan file ke FastAPI sebagai multipart request.
5. FastAPI melakukan preprocessing: Median Filter → CLAHE → Resize 224×224 → Max-scaling.
6. Model EfficientNetV2 memprediksi kelas (Normal / Osteopenia / Osteoporosis).
7. FastAPI menghasilkan visualisasi Grad-CAM dengan ROI masking (Otsu).
8. Hasil (label, confidence, probabilitas, Grad-CAM, latensi) dikembalikan ke Spring Boot.
9. Spring Boot menyimpan hasil ke PostgreSQL (riwayat pasien).
10. Hasil dikembalikan ke React dan ditampilkan dengan bar chart + heatmap.

> **Design rationale:** Pemisahan Spring Boot (business logic) dan FastAPI
> (inference) membuat model AI dapat dikembangkan dan diskalakan independen
> dari backend utama, mengikuti prinsip separation of concerns.

---

## Struktur Project

```
Portofolio/
├── docs/                    # SRS, ERD, diagram alur
│   ├── srs-rs-medika-sentosa.md
│   ├── erd-rs-medika-sentosa.md
│   └── *.mermaid
│
├── backend/                 # Spring Boot 3 (Java 21)
│   └── src/main/java/com/medikasentosa/
│       ├── config/
│       ├── controller/
│       ├── dto/
│       ├── entity/
│       ├── exception/
│       ├── repository/
│       ├── security/
│       └── service/
│
├── frontend/                # React + TypeScript + Vite
│   └── src/
│       ├── components/
│       ├── context/
│       ├── pages/
│       ├── routes/
│       └── services/
│
└── ml-service/              # FastAPI + TensorFlow
    ├── app/
    │   ├── main.py
    │   ├── preprocessing.py
    │   ├── predictor.py
    │   └── gradcam.py
    └── models/
```

---

## Roadmap Pengembangan

Pengembangan dilakukan bertahap untuk menjaga fokus dan kualitas.

### Fase 1 — MVP (fitur inti)
- [x] Autentikasi JWT (register, login, RBAC)
- [x] Skrining osteoporosis AI (React → Spring → FastAPI → PostgreSQL)
- [ ] Pendaftaran pasien
- [ ] Appointment + antrian
- [ ] Rekam medis + resep
- [ ] Manajemen master data (poli, dokter)

### Fase 2 — Penunjang & Chatbot
- [ ] Laboratorium & radiologi
- [ ] Rawat inap & jadwal operasi
- [ ] Chatbot Senta (Qwen via vLLM)
- [ ] Redis cache + WebSocket notifikasi antrian

### Fase 3 — Keuangan & Infrastruktur
- [ ] Billing & pembayaran
- [ ] Docker + docker-compose
- [ ] Prometheus + Grafana (observability)
- [ ] GitHub Actions (CI/CD)

---

## Menjalankan Secara Lokal

### Prasyarat
- Java 21, Gradle
- Node.js, npm
- Python 3.10+, pip
- PostgreSQL

### Backend (Spring Boot)
```bash
cd backend
# atur application.properties (database, JWT secret, URL FastAPI)
./gradlew bootRun
# berjalan di http://localhost:8080
```

### ML Service (FastAPI)
```bash
cd ml-service
python3 -m venv venv && source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8001
# berjalan di http://localhost:8001 (Swagger: /docs)
```

### Frontend (React)
```bash
cd frontend
npm install
npm run dev
# berjalan di http://localhost:5173
```

---

## Lisensi & Kredit

Dikembangkan sebagai project portfolio full-stack.
Dataset X-ray osteoporosis bersumber dari data publik.

---

<p align="center">
  <strong>RS Medika Sentosa</strong> — menghubungkan layanan rumah sakit dengan kecerdasan buatan.
</p>
