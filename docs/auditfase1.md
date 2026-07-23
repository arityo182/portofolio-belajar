Saya ingin audit menyeluruh seluruh Fase 1 (MVP) project RS Medika Sentosa.
Kerjakan dalam DUA tahap besar: (A) Audit Kode & Struktur, lalu (B) Audit
Fungsional End-to-End. Laporkan temuan sebelum memperbaiki apapun.

════════════════════════════════════════════════════════════
TAHAP A — AUDIT KODE & STRUKTUR
(scan file, JANGAN jalankan aplikasi dulu, JANGAN ubah kode dulu)
════════════════════════════════════════════════════════════

Scan semua folder dan buat LAPORAN STATUS per modul. Untuk setiap modul,
cek apakah semua layer ada (entity, repository, dto, service, controller
untuk backend; service, types, pages/components untuk frontend).

--- BACKEND (backend/src/main/java/com/medikasentosa/) ---

Cek features/ — laporkan status tiap modul:

[ ] AUTH
    - Entity: User, Role enum
    - Repository: UserRepository
    - DTO: RegisterRequest, LoginRequest, AuthResponse, UserResponse
    - Service: AuthService (register + login)
    - Controller: /api/auth/register (POST 201) + /api/auth/login (POST 200)
    - Security: JwtService, JwtFilter, SecurityConfig, CustomUserDetailsService

[ ] SCREENING
    - Entity: Screening (cek @Table name + kolom gradcamImage casing)
    - Repository: ScreeningRepository
    - DTO: ScreeningResponse, FastApiResponse
    - Service: ScreeningService (forward ke FastAPI + simpan hasil)
    - Controller: /api/screening/upload (POST) + /api/screening/history (GET)

[ ] POLI
    - Entity: Poli (isActive, soft delete)
    - Repository: findByIsActiveTrue + existsByNamaIgnoreCase
    - Service: create (cek duplikat) + getAll + getById + update + deactivate
    - Controller: CRUD /api/poli + GET /api/poli/{id}

[ ] DOKTER
    - Entity: Dokter (@ManyToOne User + Poli)
    - Service: create (validasi user ada, belum jadi dokter, poli ada,
      set role DOKTER) + getAll + getById + getByPoli + update + delete
    - Controller: /api/dokter (CRUD) + GET /api/dokter/poli/{poliId}

[ ] JADWAL DOKTER
    - Entity: JadwalDokter + enum Hari (SENIN..MINGGU, @Enumerated STRING)
    - Service: create (validasi jadwal aktif, jamMulai < jamSelesai) +
      getAll + getByDokter (aktif saja) + update + deactivate
    - Controller: /api/jadwal (CRUD) + GET /api/jadwal/dokter/{dokterId}
    - Cek: IllegalArgumentException ditangkap GlobalExceptionHandler → 400

[ ] PASIEN
    - Entity: Pasien (noRekamMedis, NIK, LocalDate tanggalLahir,
      enum JenisKelamin L/P, alamat, noHp, golDarah)
    - Service: create (generate noRekamMedis otomatis RM-YYYY-XXXXXX,
      set role PASIEN, validasi belum jadi pasien) + getAll + getById +
      getByUserId + update
    - Controller: /api/pasien (CRUD)

[ ] APPOINTMENT
    - Entity: Appointment (@ManyToOne Pasien+Dokter+JadwalDokter,
      LocalDate tanggal, enum StatusAppointment, keluhan, createdAt @PrePersist)
    - Enum StatusAppointment: MENUNGGU, DIPERIKSA, SELESAI, BATAL
    - Repository: countByJadwalAndTanggal, existsByPasienIdAndJadwalAndTanggal,
      findByPasienId, findByJadwalAndTanggalOrderByCreatedAtAsc
    - Service: create dengan 7 validasi (pasien ada, jadwal ada, jadwal aktif,
      hari cocok, tidak double-booking, kuota belum penuh, simpan)
    - Controller: /api/appointment + GET /pasien/{id} +
      PATCH /{id}/status?status=...

[ ] ANTRIAN
    - Entity: Antrian (@OneToOne Appointment, nomorUrut, createdAt)
    - Service: generate (nomor urut otomatis per jadwal+tanggal)
    - Controller: POST /api/antrian/generate/{appointmentId} +
      GET /api/antrian/appointment/{appointmentId}

[ ] REKAM MEDIS
    - Entity: RekamMedis (@ManyToOne Pasien+Dokter, @OneToOne Appointment,
      diagnosa+tindakan+catatan TEXT, createdAt)
    - Service: create (validasi appointment ada, belum punya rekam medis,
      opsional set appointment SELESAI) + getByPasien + getById +
      getByAppointment
    - Controller: /api/rekam-medis + GET /pasien/{id} +
      GET /appointment/{id}

[ ] RESEP
    - Entity: Resep (@ManyToOne RekamMedis, namaObat STRING, dosis,
      jumlah, aturanPakai)
    - Service: create (validasi rekam medis ada) + getByRekamMedis + delete
    - Controller: /api/resep + GET /rekam-medis/{id} + DELETE /{id}

Cek juga shared/:
[ ] config/       → AppConfig (RestTemplate/RestClient bean)
[ ] controller/   → HealthController (/api/health)
[ ] exception/    → GlobalExceptionHandler (404, 409, 400, 403, 500)
[ ] security/     → JwtService, JwtFilter, SecurityConfig, CustomUserDetailsService
[ ] dto/          → ErrorResponse

Cek SecurityConfig:
[ ] Endpoint publik: /api/auth/**, /api/health (permitAll)
[ ] Swagger: /swagger-ui/**, /v3/api-docs/** (permitAll, jika sudah setup)
[ ] CORS: localhost:5173 diizinkan
[ ] Stateless session

Cek @EnableMethodSecurity:
[ ] Aktif → create/update/delete Poli/Dokter/Jadwal dibatasi role ADMIN
[ ] AccessDeniedException → 403 di GlobalExceptionHandler

--- ML SERVICE (ml-service/app/) ---

[ ] core/config.py        → MODEL_PATH, LABELS, GRADCAM_LAYER
[ ] core/model_loader.py  → singleton load model + warm-up
[ ] features/osteoporosis/router.py → POST /predict/osteoporosis
[ ] features/osteoporosis/preprocessing.py → median filter+CLAHE+resize+max-scale
[ ] features/osteoporosis/predictor.py → 3-class prediction
[ ] features/osteoporosis/gradcam.py → Grad-CAM + Otsu
[ ] features/osteoporosis/schema.py → Pydantic response model

--- FRONTEND (frontend/src/) ---

[ ] core/api/client.ts    → axios instance + interceptor JWT
[ ] core/context/AuthContext.tsx → user state + role
[ ] routes/AppRoutes.tsx  → semua route terdaftar
[ ] routes/ProtectedRoute.tsx → guard login
[ ] routes/AdminRoute.tsx → guard role ADMIN (jika sudah ada)

Cek features/:
[ ] auth/           → Login.tsx, Register.tsx, authService.ts
[ ] screening/      → Radiologi.tsx, OsteoScreening.tsx, screeningService.ts,
                      ResultCard.tsx, UploadZone.tsx (atau nama sejenisnya)
[ ] poli/           → halaman daftar poli + dokter + jadwal, service, types
[ ] dokter/         → (mungkin terintegrasi di poli/ atau terpisah)
[ ] jadwal/         → (mungkin terintegrasi)
[ ] appointment/    → BookingPage.tsx + AppointmentSaya.tsx, service, types
[ ] pasien/         → ProfilPasien.tsx, service, types
[ ] rekammedis/     → RiwayatRekamMedis.tsx, service, types
[ ] admin/          → AdminLayout, AdminSidebar, KelolaPoli, KelolaDokter,
                      KelolaJadwal, DaftarPasien, DaftarAppointment

Cek pages/:
[ ] Home.tsx        → data dokter dari API (bukan hardcode) + preview layanan
[ ] Layanan.tsx     → data poli dari GET /api/poli (bukan hardcode)
[ ] TentangKami.tsx → ada konten (bukan kosong)
[ ] Kontak.tsx      → ada konten (bukan kosong)
[ ] NotFound.tsx    → 404 page (opsional)

LAPORAN TAHAP A:
Buat tabel ringkasan untuk SETIAP modul di atas:
| Modul | Status | Catatan |
| --- | --- | --- |
Status: ✅ Lengkap / ⚠️ Sebagian (sebutkan yang kurang) / ❌ Belum ada

Setelah laporan ini, TUNGGU konfirmasi saya sebelum lanjut ke Tahap B.

════════════════════════════════════════════════════════════
TAHAP B — AUDIT FUNGSIONAL (jalankan aplikasi, test di browser)
(kerjakan HANYA setelah saya konfirmasi laporan Tahap A)
════════════════════════════════════════════════════════════

Jalankan semua service terlebih dahulu:
1. PostgreSQL (pastikan running, db medika_sentosa accessible)
2. ML Service: cd ml-service && uvicorn app.main:app --port 8001
3. Backend: cd backend && ./gradlew bootRun
4. Frontend: cd frontend && npm run dev

Lalu jalankan skenario test berikut SATU PER SATU dan laporkan
PASS ✅ / FAIL ❌ + pesan error untuk setiap skenario:

--- SKENARIO 1: Health Check & Infrastruktur ---
[ ] GET /api/health → 200 { status: UP }
[ ] ML service /docs → Swagger FastAPI terbuka, endpoint /predict/osteoporosis ada
[ ] Backend /swagger-ui.html → terbuka (jika springdoc sudah setup)
[ ] Frontend npm run dev → kompilasi tanpa error, terbuka di browser

--- SKENARIO 2: Auth ---
[ ] POST /api/auth/register (user baru) → 201 + token
[ ] POST /api/auth/register (email sama) → 409 duplikat
[ ] POST /api/auth/login (kredensial benar) → 200 + token
[ ] POST /api/auth/login (password salah) → 4xx error
[ ] Frontend: halaman Register tampil, form bisa diisi & submit → redirect
[ ] Frontend: halaman Login tampil, login berhasil → redirect ke dashboard

--- SKENARIO 3: Screening AI (alur utama) ---
[ ] POST /api/screening/upload (tanpa token) → 401
[ ] POST /api/screening/upload (dengan token + file X-ray) → 200 + hasil AI
[ ] GET /api/screening/history → 200 + array riwayat
[ ] Frontend: halaman upload X-ray tampil, bisa upload, hasil muncul
    (label + confidence + Grad-CAM image)

--- SKENARIO 4: Master Data (Poli, Dokter, Jadwal) ---
Promote satu user jadi ADMIN dulu:
  UPDATE users SET role = 'ADMIN' WHERE email = 'xxx@xxx.com';
  (login ulang untuk token baru)
[ ] POST /api/poli (user biasa) → 403 Forbidden
[ ] POST /api/poli (admin) → 201 poli baru
[ ] GET /api/poli → 200 array poli (semua user)
[ ] POST /api/dokter (admin) → 201 (butuh userId + poliId valid)
[ ] GET /api/dokter/poli/{poliId} → 200 dokter di poli itu
[ ] POST /api/jadwal (admin, jam valid) → 201
[ ] POST /api/jadwal (admin, jamSelesai < jamMulai) → 400 validasi

--- SKENARIO 5: Pasien ---
[ ] POST /api/pasien → 201 + noRekamMedis otomatis (format RM-YYYY-XXXXXX)
[ ] POST /api/pasien (user sama lagi) → 409 duplikat
[ ] GET /api/pasien → 200 array pasien

--- SKENARIO 6: Appointment & Antrian ---
[ ] POST /api/appointment (hari tanggal cocok jadwal, kuota ada) → 201
[ ] POST /api/appointment (hari tidak cocok jadwal) → 400
[ ] POST /api/appointment (double-booking) → 409
[ ] POST /api/appointment sampai kuota penuh → 400 kuota penuh
[ ] POST /api/antrian/generate/{appointmentId} → 201 + nomorUrut
[ ] GET /api/antrian/appointment/{appointmentId} → 200 + nomorUrut
[ ] PATCH /api/appointment/{id}/status?status=DIPERIKSA → 200
[ ] PATCH /api/appointment/{id}/status (user biasa, bukan admin/dokter) → 403

--- SKENARIO 7: Rekam Medis & Resep ---
[ ] POST /api/rekam-medis (dari appointmentId yang ada) → 201
[ ] POST /api/rekam-medis (appointmentId sama lagi) → 409
[ ] GET /api/rekam-medis/pasien/{pasienId} → 200 array rekam medis
[ ] POST /api/resep (rekamMedisId valid) → 201
[ ] GET /api/resep/rekam-medis/{rekamMedisId} → 200 array resep
[ ] DELETE /api/resep/{id} → 204

--- SKENARIO 8: Frontend Alur Lengkap (browser) ---
[ ] Beranda: tampil (dokter dari API asli, bukan hardcode)
[ ] Layanan: tampil (dari data poli asli, bukan hardcode)
[ ] Tentang Kami: tampil (ada konten, bukan kosong)
[ ] Kontak: tampil (ada konten, bukan kosong)
[ ] Login sebagai pasien → booking appointment (pilih poli → dokter →
    jadwal → isi tanggal+keluhan → submit → dapat nomor antrian)
[ ] Halaman "Appointment Saya": appointment yang baru dibooking muncul
[ ] Halaman Profil Pasien: data tampil benar
[ ] Halaman Riwayat Rekam Medis: muncul setelah dokter isi rekam medis
[ ] Login sebagai admin → akses /admin → bisa kelola poli/dokter/jadwal
[ ] Screening: upload X-ray di halaman screening → hasil tampil lengkap

--- SKENARIO 9: Navigasi & UX Dasar ---
[ ] Navbar: semua link jalan (tidak ada link mati/404 tiba-tiba)
[ ] Akses /admin tanpa login → redirect ke login
[ ] Akses /admin sebagai pasien → ditolak (bukan redirect ke login,
    tapi pesan "tidak ada akses" atau redirect ke beranda)
[ ] Akses halaman protected (booking, profil) tanpa login → redirect ke login
[ ] npx tsc --noEmit di frontend → 0 error

LAPORAN TAHAP B:
Buat tabel ringkasan:
| Skenario | Status | Detail Masalah (jika FAIL) |
| --- | --- | --- |

Lalu buat DAFTAR PRIORITAS PERBAIKAN:
Pisahkan menjadi:
1. BLOCKER (harus diperbaiki sebelum bisa dibilang Fase 1 selesai)
2. MINOR (sebaiknya diperbaiki tapi tidak memblokir alur utama)
3. NICE TO HAVE (peningkatan, bukan keharusan MVP)

Jangan perbaiki apapun dulu — tunggu saya membaca laporan & memutuskan
mana yang mau diperbaiki duluan. Laporkan saja temuan apa adanya.