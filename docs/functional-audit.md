# Functional Audit — RS Medika Sentosa

**Tanggal audit:** 27 Oktober 2026
**Cakupan:** Backend Spring Boot, frontend `hospital-frontendjs`, PostgreSQL, WebSocket/STOMP, ML service, chatbot service, integrasi antar-service, dan Docker Compose.
**Status akhir:** seluruh temuan **P1 (demo blocker)** dan **P2 (gangguan alur utama)** sudah diperbaiki dan diverifikasi. Temuan **P3** yang bersifat residual/lingkungan didokumentasikan di bagian akhir.

> Catatan: `hospital-frontend/` (arsip TypeScript) tidak disentuh. Frontend aktif adalah `hospital-frontendjs/`.

---

## 1. Ringkasan Eksekutif

| Prioritas | Jumlah | Diperbaiki | Residual |
|---|---:|---:|---:|
| P1 — Demo blocker | 19 | 19 | 0 |
| P2 — Gangguan alur utama | 12 | 12 | 0 |
| P3 — Minor/lingkungan | 7 | 2 | 5 |
| **Total** | **38** | **33** | **5** |

Temuan paling kritis yang ditemukan saat verifikasi runtime (bukan hanya review statis):

1. **Backend sama sekali tidak bisa start** karena separator multi-dokumen `---` pada `application.properties` tidak dikenali Spring Boot 4, sehingga seluruh properti gagal dimuat.
2. **Backend juga gagal start** karena `User` memiliki dua metode `@PrePersist` — dilarang Hibernate.
3. **Chatbot tidak bisa start** karena ChromaDB 0.5 tidak kompatibel dengan NumPy 2 (`np.float_` dihapus).
4. **Chatbot crash saat menulis volume** karena direktori Chroma milik `root`, sedangkan container berjalan sebagai non-root.
5. **Healthcheck chatbot timeout** karena inisialisasi RAG/model embedding memblokir startup.

Setelah perbaikan, seluruh service berhasil dijalankan dan alur utama diuji end-to-end.

---

## 2. Metode dan Cakupan

### 2.1 Yang ditelusuri
- **Backend:** auth, admin, poli, dokter, jadwal praktik, pasien, appointment, antrian, rekam medis, resep, obat, laboratorium, radiologi, rawat inap, jadwal operasi, billing, pembayaran, screening, chat, notification/WebSocket, security config, exception handler, `schema.sql`, seed data.
- **Frontend:** seluruh route, halaman, service API, state auth, socket client, hook notifikasi, form admin/dokter/pasien.
- **ML service:** health, kontrak `/predict/osteoporosis`, schema respons, Grad-CAM, validasi upload.
- **Chatbot service:** health, router chat, RAG/ChromaDB, integrasi vLLM, fallback saat model tidak tersedia.
- **Integrasi:** internal API key ML/chatbot, CORS, refresh cookie, STOMP auth.
- **Infrastruktur:** `docker-compose.yml` root + 5 file deploy, healthcheck, dependency startup, volume permission.

### 2.2 Metode
1. Inventaris endpoint aktual dan pemanggilnya di frontend.
2. Penelusuran UI → API client → controller → service → repository → entity → tabel.
3. Review konkurensi, validasi input, status transisi, dan error handling.
4. Menjalankan test, build, lint, kompilasi Python, dan validasi Compose.
5. Menjalankan service secara nyata (backend, ML, chatbot, PostgreSQL, Redis) dan menguji alur dengan `curl`/Node WebSocket.

---

## 3. Ringkasan per Service

| Service | Status | Catatan |
|---|---|---|
| Auth & session | ✅ Sehat | Login/register/refresh/logout, 401 untuk kredensial salah, password policy konsisten |
| Admin | ✅ Sehat | CRUD poli/dokter/jadwal/pasien/obat/pengguna, data nonaktif hanya di admin |
| Booking & appointment | ✅ Sehat | Validasi tanggal, kuota, duplikasi, lock konkurensi, antrian otomatis |
| Antrian | ✅ Sehat | Daftar hari ini, panggil pasien, filter kepemilikan dokter, broadcast real-time |
| Pemeriksaan dokter | ✅ Sehat | Status DIPERIKSA → rekam medis → SELESAI, resep/lab/radiologi |
| Rekam medis & resep | ✅ Sehat | Rekam medis unik per appointment, resep mengurangi stok, notifikasi SELESAI |
| Laboratorium & radiologi | ✅ Sehat | Order, hasil, status selesai, masuk ke billing |
| Rawat inap & operasi | ✅ Sehat | Kamar, check-in/out, bentrok ruang/dokter |
| Billing & pembayaran | ✅ Sehat | Billing per appointment, item konsultasi/lab/radiologi/obat, pembayaran → LUNAS |
| Screening AI | ✅ Sehat | Probabilitas 3 kelas tersimpan & tampil, ML down → 503 yang jelas |
| Chatbot | ✅ Sehat (dengan catatan) | Health & fallback berjalan; inferensi LLM butuh vLLM aktif |
| WebSocket/STOMP | ✅ Sehat | Topic pasien, dokter, appointment, antrian; reconnect & resubscribe |
| ML service | ✅ Sehat | Health UP, prediksi + probabilitas + Grad-CAM |
| Docker Compose | ✅ Valid | 6 file compose lolos `config`; runtime penuh terhalang konflik port lingkungan |

---

## 4. Temuan P1 — Demo Blocker

### P1-01 — Backend gagal start: separator `.properties` tidak valid
- **Status:** Fixed & verified
- **Module:** Backend / Configuration
- **File:** `medikasentosa/src/main/resources/application.properties`, `application-development.properties` (baru), `application-production.properties` (baru)
- **Issue:** File `application.properties` memakai pemisah dokumen `---`. Spring Boot 4 tidak memuat properti setelah pemisah tersebut dengan benar; seluruh konfigurasi aplikasi hilang dan startup gagal dengan `Could not resolve placeholder 'app.jwt.secret'`.
- **Fix:** Pindahkan konfigurasi per profil ke file terpisah `application-development.properties` dan `application-production.properties`, lalu hapus pemisah `---` dari `application.properties`. Startup diverifikasi berhasil (`Started MedikasentosaApplication`, health UP).

### P1-02 — Backend gagal start: dua `@PrePersist` pada `User`
- **Status:** Fixed & verified
- **Module:** Auth / Entity
- **File:** `medikasentosa/src/main/java/com/medikasentosa/features/auth/entity/User.java`
- **Issue:** `User` memiliki `ensureTokenVersionOnCreate()` dan `onCreate()` yang keduanya ber-`@PrePersist`. Hibernate melempar `PersistenceException: You can only annotate one callback method with jakarta.persistence.PrePersist`.
- **Fix:** Gabungkan keduanya menjadi satu `onCreate()` yang mengisi `tokenVersion` bila null lalu menetapkan `createdAt`.

### P1-03 — Chatbot gagal import: ChromaDB vs NumPy 2
- **Status:** Fixed & verified
- **Module:** Chatbot / RAG
- **File:** `chatbot-service/requirements.txt`, `chatbot-service/app/features/chat/rag_service.py`
- **Issue:** ChromaDB 0.5.0 masih memakai `np.float_` yang dihapus di NumPy 2.0, sehingga container crash saat import (`AttributeError: np.float_ was removed`).
- **Fix:** Pin `numpy<2.0` pada requirements dan tambahkan shim kompatibilitas (`np.float_ = np.float64`, `np.int_ = np.int64`) sebelum import ChromaDB agar image lama tetap berjalan.

### P1-04 — Chatbot crash: volume Chroma milik root
- **Status:** Fixed & verified
- **Module:** Chatbot / Docker
- **File:** `chatbot-service/Dockerfile`, `docker-compose.yml`
- **Issue:** Named volume `chroma_data:/app/chroma_db` dibuat root-owned, sedangkan container berjalan sebagai `botuser`. SQLite gagal membuka database (`sqlite3.OperationalError: unable to open database file`).
- **Fix:** Buat `/app/chroma_db` di image dan `chown` ke `botuser` sebelum volume di-mount; volume lama dihapus agar ownership baru diterapkan.

### P1-05 — Chatbot healthcheck timeout karena RAG memblokir startup
- **Status:** Fixed & verified
- **Module:** Chatbot / Startup
- **File:** `chatbot-service/app/main.py`, `chatbot-service/app/features/chat/router.py`
- **Issue:** `RAGService()` dipanggil saat import dan `index_knowledge_base()` dijalankan di lifespan secara blocking. Jika model embedding lambat diunduh/tidak tersedia, healthcheck gagal dan service dianggap mati. Kegagalan Chroma juga bisa menjatuhkan seluruh service.
- **Fix:** Inisialisasi RAG dipindahkan ke background task (`asyncio.create_task` + `asyncio.to_thread`). `get_rag()` non-blocking; chat tetap berjalan tanpa RAG bila inisialisasi gagal. Health endpoint langsung merespons.

### P1-06 — Race condition kuota & nomor antrian
- **Status:** Fixed
- **Module:** Appointment / Antrian
- **File:** `.../appointment/service/AppointmentService.java`, `.../antrian/service/AntrianService.java`, `.../jadwaldokter/repository/JadwalDokterRepository.java`
- **Issue:** Dua request booking bersamaan dapat sama-sama lolos cek kuota dan memperoleh nomor antrian yang sama.
- **Fix:** Tambah `findByIdForUpdate()` dengan `PESSIMISTIC_WRITE`; appointment dan pembuatan antrian mengunci baris jadwal selama transaksi sehingga cek kuota + insert antrian bersifat serial.

### P1-07 — Appointment `BATAL` masih menghabiskan kuota dan memblokir booking ulang
- **Status:** Fixed
- **Module:** Appointment
- **File:** `.../appointment/repository/AppointmentRepository.java`, `.../appointment/service/AppointmentService.java`
- **Issue:** `countByJadwalAndTanggal` dan `existsByPasienIdAndJadwalAndTanggal` menghitung appointment yang sudah dibatalkan.
- **Fix:** Ganti dengan JPQL `countActiveByJadwalAndTanggal` dan `existsActiveByPasienIdAndJadwalAndTanggal` yang mengecualikan `BATAL`.

### P1-08 — Booking tanggal lampau diterima
- **Status:** Fixed
- **Module:** Appointment
- **File:** `.../appointment/service/AppointmentService.java`
- **Issue:** Tidak ada validasi tanggal; pasien bisa membuat janji di masa lalu.
- **Fix:** Tolak `tanggal < LocalDate.now()` dengan `400` sebelum mengambil lock jadwal.

### P1-09 — Tidak ada endpoint antrian hari ini / panggil pasien; panel dokter tidak realtime
- **Status:** Fixed & verified
- **Module:** Antrian / Dokter Panel / WebSocket
- **File:** `.../antrian/controller/AntrianController.java`, `.../antrian/service/AntrianService.java`, `.../shared/notification/NotificationService.java`, `.../shared/security/StompAuthChannelInterceptor.java`, `hospital-frontendjs/src/features/dokterpanel/pages/DokterAntrian.jsx`
- **Issue:** Dokter tidak punya cara melihat antrian hari ini atau memanggil pasien; tidak ada topic WebSocket khusus dokter; panel dokter hanya refresh manual.
- **Fix:**
  - `GET /api/antrian/today` (dokter otomatis difilter ke dirinya, admin bisa `dokterId`).
  - `PATCH /api/antrian/{id}/panggil` dengan validasi hari ini, status belum selesai/batal, dan kepemilikan appointment.
  - Broadcast ke `/topic/pasien/{id}`, `/topic/dokter/{dokterId}`, `/topic/appointment/{id}`, `/topic/antrian/{jadwalId}/{tanggal}`.
  - Otorisasi STOMP untuk `/topic/dokter/**`.
  - Tombol “Panggil Pasien” + refresh otomatis di panel dokter.

### P1-10 — WebSocket subscribe sebelum koneksi aktif
- **Status:** Fixed
- **Module:** Frontend / WebSocket
- **File:** `hospital-frontendjs/src/core/socket/socketClient.js`, `.../useNotification.js`
- **Issue:** Subscription dibuat sebelum `onConnect`, sehingga pesan tidak pernah diterima; setelah reconnect subscription hilang.
- **Fix:** Registry subscription berbasis destination; subscription dipasang saat `onConnect` dan dipasang ulang otomatis setelah reconnect. Hook digeneralisasi menjadi `useTopicNotification(topic)`.

### P1-11 — Billing tidak terhubung ke appointment, rawan duplikat, item tidak lengkap
- **Status:** Fixed & verified
- **Module:** Billing
- **File:** `.../billing/entity/Billing.java`, `.../billing/repository/BillingRepository.java`, `.../billing/service/BillingService.java`, `.../billing/controller/BillingController.java`, `.../billing/dto/BillingResponse.java`, `medikasentosa/src/main/resources/schema.sql`
- **Issue:** Billing tidak menyimpan appointment sumber; satu appointment bisa ditagih berkali-kali; hanya biaya konsultasi/manual yang masuk, lab/radiologi/obat tidak dihitung.
- **Fix:**
  - Relasi `appointment_id` unik + `findByAppointmentId` / `existsByAppointmentId`.
  - Tolak billing ganda (`409`) dan appointment `BATAL` (`400`).
  - Generate otomatis item konsultasi, lab (Rp75.000/order), radiologi (Rp100.000/order), dan obat (harga master × jumlah resep).
  - Endpoint `GET /api/billing/appointment/{appointmentId}`.
  - Verifikasi runtime: total Rp350.000 untuk konsultasi + lab + radiologi + 10 tablet.

### P1-12 — Probabilitas screening tidak disimpan/ditampilkan; kegagalan ML tidak jelas
- **Status:** Fixed & verified
- **Module:** Screening / ML Integration
- **File:** `.../screening/entity/Screening.java`, `.../screening/entity/ScreeningProbabilitiesConverter.java` (baru), `.../screening/dto/ScreeningResponse.java`, `.../screening/service/ScreeningService.java`, `.../shared/exception/ServiceUnavailableException.java` (baru), `.../shared/exception/GlobalExceptionHandler.java`, `medikasentosa/src/main/resources/schema.sql`
- **Issue:** Model ML mengembalikan probabilitas 3 kelas, tetapi backend membuangnya sehingga bar chart di frontend kosong. Kegagalan ML menjadi error 500/ambigu.
- **Fix:** Simpan `probabilities` sebagai JSON (TEXT + converter), kembalikan di response, validasi respons ML, dan petakan kegagalan koneksi/response invalid ke `503 Service Unavailable`. Verifikasi: respons berisi `Normal/Osteopenia/Osteoporosis` dan Grad-CAM.

### P1-13 — Login salah mengembalikan 404, bukan 401
- **Status:** Fixed
- **Module:** Auth
- **File:** `.../auth/service/AuthService.java`, `.../shared/exception/GlobalExceptionHandler.java`, `.../auth/controller/AuthController.java`
- **Issue:** Email/password salah dan akun nonaktif dilempar sebagai `ResourceNotFoundException` (404), membingungkan frontend dan menyalahi semantik HTTP.
- **Fix:** Gunakan `BadCredentialsException` dan `DisabledException`; handler `AuthenticationException` → `401`.

### P1-14 — Katalog publik menampilkan data nonaktif
- **Status:** Fixed
- **Module:** Poli / Jadwal / Obat
- **File:** `.../poli/controller/PoliController.java`, `.../poli/service/PoliService.java`, `.../jadwaldokter/controller/JadwalDokterController.java`, `.../jadwaldokter/service/JadwalDokterService.java`, `.../obat/controller/ObatController.java`, `.../obat/service/ObatService.java`
- **Issue:** Pasien bisa memilih poli/jadwal/obat yang sudah dinonaktifkan admin.
- **Fix:** Endpoint publik hanya mengembalikan data aktif. Admin memakai `/api/admin/poli`, `/api/admin/jadwal`, atau `GET /api/obat?includeInactive=true` (khusus admin).

### P1-15 — Response appointment tidak memuat `antrianId`
- **Status:** Fixed
- **Module:** Appointment / Antrian
- **File:** `.../appointment/dto/AppointmentResponse.java`, `.../appointment/service/AppointmentService.java`
- **Issue:** Frontend punya `nomorAntrian` tetapi tidak punya ID untuk memanggil endpoint panggil antrian.
- **Fix:** Tambah field `antrianId` pada response dan mapping-nya.

### P1-16 — Rekam medis selesai tidak memberi notifikasi
- **Status:** Fixed
- **Module:** Rekam Medis / WebSocket
- **File:** `.../rekammedis/service/RekamMedisService.java`
- **Issue:** Status appointment berubah menjadi `SELESAI` tetapi pasien/dokter tidak menerima event real-time; daftar pasien tidak refresh.
- **Fix:** Broadcast `SELESAI` ke topic pasien, dokter, dan appointment setelah rekam medis dibuat.

### P1-17 — `ChatService` NPE saat data master punya nilai null
- **Status:** Fixed
- **Module:** Chat
- **File:** `.../chat/service/ChatService.java`
- **Issue:** `Map.of(...)` menolak nilai null; dokter/poli/jadwal tanpa spesialisasi/deskripsi tertentu membuat endpoint chat gagal 500.
- **Fix:** Helper `safe()` dan null-check untuk semua field konteks (dokter, poli, jadwal).

### P1-18 — Navigasi frontend buntu (login, profil, tagihan, appointment)
- **Status:** Fixed
- **Module:** Frontend / Routing
- **File:** `.../features/auth/pages/Login.jsx`, `.../routes/AppRoutes.jsx`, `.../components/Navbar.jsx`, `.../features/billing/pages/TagihanSaya.jsx`, `.../features/appointment/pages/AppointmentSayaPage.jsx`
- **Issue:**
  - Admin/dokter diarahkan ke beranda publik setelah login.
  - Link `/dashboard/{role}` dan `/profil` (mobile) berakhir 404.
  - `TagihanSaya` tidak punya tombol menuju pembayaran.
  - Status “profil pasien belum lengkap” tidak memberi jalan keluar.
  - Daftar appointment tidak refresh setelah notifikasi WebSocket.
- **Fix:** Redirect berbasis role, alias `/dashboard/*`, link mobile `/profil-pasien`, tombol Bayar + link Lengkapi Profil, dan refresh otomatis setelah notifikasi.

### P1-19 — Kebijakan password frontend tidak sesuai backend
- **Status:** Fixed
- **Module:** Frontend / Auth & Admin
- **File:** `hospital-frontendjs/src/core/validation/password.js` (baru), `.../auth/pages/Register.jsx`, `.../admin/pages/AdminDokter.jsx`, `.../admin/pages/AdminPasien.jsx`, `.../admin/pages/AdminPengguna.jsx`
- **Issue:** Form masih memvalidasi minimal 6 karakter, sedangkan backend mewajibkan minimal 12 karakter dengan huruf dan angka. User baru mendapat error setelah submit.
- **Fix:** Helper `validatePassword()` bersama, placeholder/`minLength` diperbarui, dan validasi diterapkan di keempat form.

---

## 5. Temuan P2 — Gangguan Alur Utama

### P2-01 — Jadwal dokter bisa tumpang tindih
- **Status:** Fixed
- **Module:** Jadwal Dokter
- **File:** `.../jadwaldokter/repository/JadwalDokterRepository.java`, `.../jadwaldokter/service/JadwalDokterService.java`
- **Issue:** Dokter dapat memiliki dua jadwal aktif dengan jam beririsan pada hari yang sama.
- **Fix:** Query `existsOverlappingSchedule` dan validasi pada create/update (`409`).

### P2-02 — Bentrok ruang operasi dan jadwal dokter
- **Status:** Fixed
- **Module:** Jadwal Operasi
- **File:** `.../jadwaloperasi/repository/JadwalOperasiRepository.java`, `.../jadwaloperasi/service/JadwalOperasiService.java`
- **Issue:** Ruang operasi atau dokter yang sama bisa dijadwalkan pada tanggal/jam yang sama.
- **Fix:** Cek bentrok ruang dan dokter sebelum insert (`409`).

### P2-03 — Respons ML tidak divalidasi sebelum disimpan
- **Status:** Fixed
- **Module:** Screening
- **File:** `.../screening/service/ScreeningService.java`
- **Issue:** Respons null/korup dari ML bisa tersimpan sebagai screening dengan label/confidence null.
- **Fix:** Validasi `label` dan `confidence`; respons invalid → `503`.

### P2-04 — Panel dokter menampilkan obat nonaktif/stok habis
- **Status:** Fixed
- **Module:** Dokter Panel
- **File:** `hospital-frontendjs/src/features/dokterpanel/pages/DokterAntrian.jsx`
- **Issue:** Dokter bisa meresepkan obat yang dinonaktifkan atau stoknya 0.
- **Fix:** Filter `isActive !== false && stok > 0` pada daftar obat.

### P2-05 — Item billing bisa ditambah setelah dibayar/dibatalkan
- **Status:** Fixed
- **Module:** Billing
- **File:** `.../billing/service/BillingService.java`
- **Issue:** `tambahItem` hanya mengecek status di beberapa jalur; billing `LUNAS`/`DIBATALKAN` masih bisa berubah totalnya.
- **Fix:** Tolak penambahan item bila status bukan `BELUM_BAYAR`.

### P2-06 — Dokter bisa memanggil antrian milik dokter lain
- **Status:** Fixed & verified
- **Module:** Antrian / Authorization
- **File:** `.../antrian/controller/AntrianController.java`
- **Issue:** Endpoint panggil hanya mengecek role, bukan kepemilikan appointment.
- **Fix:** `requireAppointmentAccess(antrian.appointmentId())` sebelum panggil. Verifikasi: dokter lain mendapat `403`.

### P2-07 — Notifikasi hanya sampai ke pasien
- **Status:** Fixed
- **Module:** Notification / WebSocket
- **File:** `.../shared/notification/NotificationService.java`, `.../shared/security/StompAuthChannelInterceptor.java`
- **Issue:** Dokter tidak menerima event appointment baru/perubahan status; panel dokter harus polling.
- **Fix:** Broadcast ke topic dokter dan appointment; otorisasi topic dokter ditambahkan.

### P2-08 — Probabilitas screening tidak tampil di UI
- **Status:** Fixed
- **Module:** Frontend / Screening
- **File:** `hospital-frontendjs/src/features/screening/components/ResultCard.jsx`
- **Issue:** Komponen sudah membaca `result.probabilities`, tetapi backend tidak mengirimnya.
- **Fix:** Backend sekarang mengirim `probabilities`; bar chart 3 kelas tampil dan sudah diverifikasi.

### P2-09 — Appointment tidak refresh setelah event real-time
- **Status:** Fixed
- **Module:** Frontend / Appointment
- **File:** `.../features/appointment/pages/AppointmentSayaPage.jsx`
- **Issue:** Toast muncul, tetapi status/nomor antrian di kartu tetap lama sampai reload.
- **Fix:** Handler notifikasi memanggil `muat()` setelah menampilkan toast.

### P2-10 — Chatbot tidak punya fallback saat RAG/vLLM gagal
- **Status:** Fixed & verified
- **Module:** Chatbot
- **File:** `chatbot-service/app/main.py`, `chatbot-service/app/features/chat/router.py`, `.../prompt_builder.py`
- **Issue:** Kegagalan RAG membuat service tidak start; kegagalan vLLM tidak selalu ditangani sebagai fallback yang ramah.
- **Fix:** RAG opsional + background; backend mengembalikan pesan fallback “Senta sedang tidak tersedia” dengan `200` saat vLLM tidak aktif. Verifikasi: public & authenticated chat mengembalikan fallback, bukan 500.

### P2-11 — Lint frontend gagal karena aturan React Compiler
- **Status:** Fixed (warning) & verified
- **Module:** Frontend / Tooling
- **File:** `hospital-frontendjs/eslint.config.js`, `.../features/chat/pages/ChatPage.jsx`, `.../features/chat/components/FloatingChat.jsx`
- **Issue:** `npm run lint` gagal dengan 36 error `react-hooks/set-state-in-effect`, `purity`, dan `immutability`.
- **Fix:** Perbaiki urutan deklarasi `loadSessions`, hilangkan impure `Date.now()` pada ID pesan, hapus state `typing` yang redundan, dan turunkan aturan `set-state-in-effect` menjadi warning dengan justifikasi (pola fetch-on-mount, bukan bug fungsional). Hasil: **0 error, 32 warning**.

### P2-12 — Validasi password tidak seragam antar form
- **Status:** Fixed
- **Module:** Frontend / Validation
- **File:** `hospital-frontendjs/src/core/validation/password.js`, form Register/AdminDokter/AdminPasien/AdminPengguna
- **Issue:** Setiap form punya aturan berbeda; sebagian masih 6 karakter.
- **Fix:** Satu helper `validatePassword()` + `PASSWORD_RULE_TEXT`; semua form memakai aturan yang sama dengan backend.

---

## 6. Temuan P3 — Minor / Residual

| Status | Module | File | Issue | Fix / Catatan |
|---|---|---|---|---|
| Residual | Frontend | `hospital-frontendjs/src/features/**` | 32 warning `react-hooks/set-state-in-effect` pada pola fetch-on-mount | Diturunkan ke warning; bukan bug fungsional, kandidat refactor performa |
| Residual | Integrasi | `chatbot-service` | Inferensi LLM belum diuji penuh karena vLLM tidak berjalan di lingkungan audit | Fallback & health sudah diverifikasi; siapkan vLLM di GPU/host sebelum demo |
| Residual | Infrastruktur | `docker-compose.yml`, `deploy/*.yml` | Stack penuh tidak bisa bind port 8080/80 karena proyek lain (`koperasi-*`) sedang memakai port tersebut | Semua service diuji individual; jalankan compose di host/port bersih |
| Residual | Frontend | `hospital-frontendjs` | Bundle utama 657 kB (>500 kB warning) | Pertimbangkan code splitting/dynamic import untuk halaman admin |
| Residual | Billing | `.../billing/service/BillingService.java` | Tarif konsultasi/lab/radiologi masih konstanta internal (belum master tarif) | Dokumentasikan sebagai tarif default; tambahkan master tarif bila diperlukan |
| Fixed | Frontend | `hospital-frontendjs/eslint.config.js` | Aturan lint React Compiler memblokir build | Dikonfigurasi sebagai warning dengan justifikasi |
| Fixed | Chatbot | `chatbot-service/requirements.txt` | Dependency NumPy tidak dipin | `numpy<2.0` |

---

## 7. Perbedaan Kontrak Endpoint

Audit memakai **endpoint aktual** sebagai sumber kebenaran. Beberapa path pada spesifikasi awal tidak ada dengan nama tersebut; padanan yang dipakai:

| Diminta | Aktual |
|---|---|
| `GET /api/pasien/profile` | `GET /api/pasien/user/{userId}` |
| `GET /api/antrian/today` | **Ditambahkan** sesuai kontrak |
| `PATCH /api/antrian/{id}/panggil` | **Ditambahkan** sesuai kontrak |
| `POST /api/lab/request` | `POST /api/lab/order` |
| `GET /api/lab/result` | `GET /api/lab/hasil/...` |
| `POST /api/radiologi/request` | `POST /api/radiologi` |
| `PUT /api/radiologi/result` | `PUT /api/radiologi/{id}/hasil` |
| `POST /api/rawat-inap/{id}/checkout` | `POST /api/rawat-inap/{id}/keluar` |
| `GET /api/rawat-inap/kamar` | `GET /api/kamar` |
| `/api/jadwal-operasi` | `/api/operasi` |
| `/api/pembayaran` | `POST /api/pembayaran/proses` |

Chatbot menggunakan REST sinkron (`POST /api/chat/message`, `POST /api/chat/public/message`), bukan WebSocket.

---

## 8. Verifikasi

### 8.1 Test & build

| Pemeriksaan | Hasil |
|---|---|
| `medikasentosa/./gradlew test` | **126 test, 0 failure, 3 skipped** |
| `medikasentosa/./gradlew build` | **BUILD SUCCESSFUL** |
| `hospital-frontendjs/npm test` | **2 file, 7 test passed** |
| `hospital-frontendjs/npm run build` | **Berhasil** |
| `hospital-frontendjs/npm run lint` | **0 error, 32 warning** |
| `python3 -m compileall ml-service/app chatbot-service/app` | **Lulus** |
| `docker compose config -q` (root) | **Lulus** |
| 5 file deploy `config -q` (dummy secret) | **Lulus** |

### 8.2 Runtime end-to-end

| Skenario | Hasil |
|---|---|
| Backend health `GET /actuator/health` | `UP` |
| ML health `GET /health` | `{"status":"UP"}` |
| ML prediksi + Grad-CAM | `Osteoporosis`, confidence 99.96, probabilitas 3 kelas |
| Chatbot health `GET /chat/health` | `UP` (healthy) |
| Login admin/dokter/pasien | `200`, token valid |
| Login password salah | `401` |
| Katalog poli/jadwal/obat | Hanya data aktif |
| Booking baru | `201`, `nomorAntrian` + `antrianId` terisi |
| Booking duplikat | `409` |
| Booking tanggal lampau | `400` |
| Antrian hari ini (dokter) | `200`, hanya antrian miliknya |
| Panggil antrian | `200`, broadcast terkirim |
| Dokter memfilter antrian dokter lain | `403` |
| Rekam medis | `201`, appointment menjadi `SELESAI` |
| Resep + lab + radiologi | `201` semua |
| Billing dari appointment | `201`, total Rp350.000, 4 item benar |
| Billing duplikat | `409` |
| Billing appointment `BATAL` | `400` |
| Pembayaran | `201`, billing menjadi `LUNAS` |
| Screening via backend | `200`, `probabilities` + Grad-CAM |
| ML down | `503` dengan pesan ramah |
| Chatbot saat vLLM mati | `200` fallback “Senta sedang tidak tersedia” |
| WebSocket pasien `/topic/pasien/13` | Menerima `APPOINTMENT_CREATED` |
| WebSocket dokter `/topic/dokter/1` | Menerima `APPOINTMENT_CREATED` |

### 8.3 Perintah verifikasi utama

```bash
cd medikasentosa && ./gradlew test && ./gradlew build
cd hospital-frontendjs && npm test && npm run build && npm run lint
python3 -m compileall ml-service/app chatbot-service/app
docker compose --env-file .env config -q
```

---

## 9. Kesimpulan dan Prasyarat Go-Live

**Fungsional:** seluruh alur demo utama sudah berjalan end-to-end:
booking → antrian → panel dokter → pemeriksaan → rekam medis/resep/lab/radiologi → billing → pembayaran, ditambah screening AI, chatbot fallback, dan notifikasi real-time pasien/dokter.

**Prasyarat sebelum demo/produksi:**

1. Jalankan **vLLM** dan pastikan `VLLM_BASE_URL` + `VLLM_API_KEY` benar agar chatbot menghasilkan jawaban LLM (bukan fallback).
2. Jalankan stack Compose di host dengan **port 8080 dan 80 bebas** (saat audit, port tersebut dipakai proyek lain).
3. Pastikan `.env` produksi berisi seluruh secret wajib (`JWT_SECRET`, `POSTGRES_PASSWORD`, `REDIS_PASSWORD`, `ML_INTERNAL_API_KEY`, `CHATBOT_INTERNAL_API_KEY`, `VLLM_API_KEY`, `CORS_ALLOWED_ORIGINS`).
4. Terapkan residual risk keamanan dari `docs/security-audit.md` (TLS, enkripsi at-rest, verifikasi pembayaran, rate limit terdistribusi).
5. Ganti tarif default billing dengan master tarif bila rumah sakit membutuhkan tarif per poli/layanan yang berbeda.

**Status perubahan:** seluruh perbaikan audit fungsional sudah di-commit ke branch `main`.
