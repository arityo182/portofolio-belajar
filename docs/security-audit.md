# Security Audit — RS Medika Sentosa

**Tanggal audit:** 26 Oktober 2026  
**Cakupan:** Backend Spring Boot, Frontend React/Vite (`hospital-frontendjs`), ML Service, Chatbot Service, PostgreSQL/Redis, Docker Compose, deployment, Nginx, dan CI.  
**Arsip:** `hospital-frontend/` (TypeScript) adalah arsip lama dan tidak dipakai untuk deployment; seluruh temuan frontend mengacu pada `hospital-frontendjs/`.  
**Status akhir:** seluruh temuan **CRITICAL** dan **HIGH** telah diperbaiki di repository. Temuan **MEDIUM/LOW** yang bersifat residual (memerlukan keputusan arsitektur/operasional) didokumentasikan di bagian akhir.

## Metode

- Inventarisasi 392 file tracked dan 477 file source-like.
- Review manual seluruh controller, konfigurasi Spring Security, JWT/filter, WebSocket/STOMP, service dan entity utama, ML, chatbot, Docker Compose/Dockerfile, Nginx, deployment, serta auth/API client frontend.
- Pencarian pola berbahaya: SQL/JPQL string concatenation, `dangerouslySetInnerHTML`, `innerHTML`, `eval`, `new Function`, secret hardcoded, `.env` tracked, `localStorage` token, bind `0.0.0.0`, dependency CVE.
- Verifikasi: `./gradlew test` (119 test, 3 skipped), `npm test` (7 test), `npm run build`, `npm audit` (0 vulnerability), `py_compile` ML/chatbot, serta `docker compose config` untuk root dan seluruh file deploy.

## Ringkasan Severity

| Severity | Jumlah | Status |
|---|---:|---|
| CRITICAL | 7 | Semua diperbaiki |
| HIGH | 17 | Semua diperbaiki |
| MEDIUM | 18 | 6 diperbaiki, 12 residual/diterima dengan mitigasi |
| LOW | 7 | 5 diperbaiki, 2 residual |
| **Total** | **49** | 35 diperbaiki, 14 residual |

> **Catatan:** angka pada tabel di atas dan tabel akhir dihitung dari temuan yang didokumentasikan di bawah. Temuan residual adalah risiko yang tidak dapat ditutup hanya dengan perubahan kode repository (mis. TLS, enkripsi at-rest, pemisahan jaringan) dan wajib menjadi prasyarat go-live.

---

# CRITICAL

## C-01 — Broken Object Level Authorization (BOLA/IDOR) pada data klinis

- **Severity:** CRITICAL
- **Service:** Backend
- **Module:** Rekam Medis, Laboratorium, Radiologi, Resep, Rawat Inap, Jadwal Operasi
- **File:** `medikasentosa/src/main/java/com/medikasentosa/features/rekammedis/controller/RekamMedisController.java`, `.../laboratorium/controller/LabOrderController.java`, `.../laboratorium/controller/LabHasilController.java`, `.../radiologi/controller/RadiologiController.java`, `.../resep/controller/ResepController.java`, `.../rawatinap/controller/RawatInapController.java`, `.../jadwaloperasi/controller/JadwalOperasiController.java`
- **Line:** RekamMedis 50–132; LabOrder 39–78; LabHasil 40–65; Radiologi 43–83; Resep 45–88; RawatInap 40–88; Operasi 41–80
- **Issue:** Seluruh endpoint hanya dilindungi `.anyRequest().authenticated()`. Setiap user yang login — termasuk pasien — dapat membaca atau mengubah rekam medis, hasil lab, radiologi, resep, rawat inap, dan jadwal operasi milik pasien lain hanya dengan menebak ID pada path/body.
- **Code:**
  ```java
  @GetMapping("/{id}")
  public RekamMedisResponse getById(@PathVariable Long id) {
      return rekamMedisService.getById(id); // tidak ada cek pemilik/relasi dokter-pasien
  }
  ```
- **Fix:** Ditambahkan `AuthorizationService` sebagai pusat kebijakan. Controller memanggil `requireRekamMedisAccess`, `requireRekamMedisWrite`, `requireLabOrderAccess`, `requireLabOrderWrite`, `requireRadiologiWrite`, `requireResepWrite`, `requireRawatInapOfPasien`, `requireOperasiOfPasien`, dan variannya. Admin boleh semua; pasien hanya miliknya; dokter hanya pasien dengan relasi perawatan (`appointment`, `rekam_medis`, atau `rawat_inap`). Akses yang ditolak menghasilkan 403, bukan 404/500.
  ```java
  authorization.requireRekamMedisAccess(id);
  authorization.requireRekamMedisWrite(request.rekamMedisId());
  ```
  Implementasi: `shared/security/AuthorizationService.java:155–464`.

## C-02 — IDOR pada billing dan pembayaran

- **Severity:** CRITICAL
- **Service:** Backend
- **Module:** Billing, Pembayaran
- **File:** `.../billing/controller/BillingController.java`, `.../pembayaran/controller/PembayaranController.java`
- **Line:** Billing 35–69; Pembayaran 35–52
- **Issue:** Pasien dapat membaca tagihan pasien lain, melihat riwayat pembayaran orang lain, dan memproses pembayaran untuk `billingId` milik pasien lain. Daftar billing global juga dapat diakses selain admin.
- **Code:**
  ```java
  @GetMapping("/{id}")
  public BillingResponse getById(@PathVariable Long id) {
      return billingService.getById(id);
  }
  ```
- **Fix:** `GET /api/billing` dan `GET /api/pembayaran` dibatasi `@PreAuthorize("hasRole('ADMIN')")`. Detail billing/pembayaran diverifikasi dengan `requireBillingAccess(id)` / `requireBillingOfPasien(pasienId)`, dan proses bayar dengan `requireBillingPayment(req.billingId())`. Pasien hanya dapat mengakses billing miliknya.
  ```java
  authorization.requireBillingAccess(id);
  authorization.requireBillingPayment(req.billingId());
  ```

## C-03 — Subscription WebSocket/STOMP tanpa autentikasi dan tanpa scoping

- **Severity:** CRITICAL
- **Service:** Backend
- **Module:** WebSocket / STOMP
- **File:** `medikasentosa/src/main/java/com/medikasentosa/shared/config/WebSocketConfig.java`, `.../shared/security/StompAuthChannelInterceptor.java`
- **Line:** WebSocketConfig 47–58; StompAuthChannelInterceptor 50–133
- **Issue:** Endpoint `/ws/**` bersifat `permitAll`, dan tidak ada interceptor yang memvalidasi frame `CONNECT` atau `SUBSCRIBE`. Siapa pun dapat berlangganan `/topic/pasien/{id}` atau `/topic/appointment/{id}` milik pasien lain dan menerima notifikasi berisi data medis. Frame `SEND` juga terbuka.
- **Code:**
  ```java
  registry.addEndpoint("/ws").setAllowedOrigins("http://localhost:5173").withSockJS();
  // tidak ada configureClientInboundChannel / validasi token
  ```
- **Fix:** `StompAuthChannelInterceptor` memvalidasi Bearer token pada frame `CONNECT` menggunakan `jwtService.isTokenValid()` (termasuk tipe access, status aktif, dan token version). Setiap `SUBSCRIBE` diotorisasi: `/topic/pasien/{id}` → `requirePasienAccess`, `/topic/appointment/{id}` → `requireAppointmentAccess`, `/topic/antrian/**` → `requireStaff`. Semua `SEND` ditolak. Origin handshake dibaca dari `CORS_ALLOWED_ORIGINS`.
  ```java
  if (StompCommand.CONNECT.equals(command)) accessor.setUser(authenticate(accessor));
  if (StompCommand.SUBSCRIBE.equals(command)) authorizeSubscription(accessor.getDestination(), authentication);
  if (StompCommand.SEND.equals(command)) throw new AccessDeniedException("...");
  ```

## C-04 — Kredensial default/berbagi pada seed, dokumentasi, dan admin bootstrap

- **Severity:** CRITICAL
- **Service:** Backend / Repository
- **Module:** Seeder, dokumentasi, collection API
- **File:** `medikasentosa/src/main/resources/db/seed-data.sql`, `medikasentosa/src/main/java/com/medikasentosa/shared/seed/DataSeeder.java`, `README.md`, `docs/5-docker-local.md`, `medikasentosa/Insomnia_2026-07-24.yaml`
- **Line:** seed-data 38–50; DataSeeder 40–78; README 599–603; docs 97; Insomnia 22–23/115/230/288
- **Issue:** Seluruh akun demo memakai password yang sama (`<demo-password>`), `DataSeeder` membuat admin dengan default `<default-admin-password>`, dan password tersebut terdokumentasi di README/Insomnia. Siapa pun yang membaca repository mendapat akses ADMIN penuh.
- **Code:**
  ```sql
  -- Password: <demo-password> → BCrypt hash
  (1, 'Admin Medika', 'admin@medikasentosa.id', '$2a$10$...', 'ADMIN', true, ...)
  ```
  ```java
  String adminPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "<default-admin-password>");
  ```
- **Fix:** 20 user seed sekarang memakai hash BCrypt cost 12 yang unik dan acak; tidak ada password default di repo. `DataSeeder` hanya membuat admin jika `ADMIN_PASSWORD` diisi, menolak password <12 karakter/tanpa huruf+angka, dan pada production mewajibkan variabel tersebut. README/docs tidak lagi mencantumkan password; kredensial lokal hanya di `CREDENTIALS.md` yang di-gitignore.
  ```java
  if (adminPassword.length() < 12
          || !adminPassword.matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
      throw new IllegalStateException("ADMIN_PASSWORD minimal 12 karakter ...");
  }
  ```

## C-05 — JWT lemah/default, umur token 24 jam, tanpa revokasi/refresh/logout

- **Severity:** CRITICAL
- **Service:** Backend
- **Module:** Autentikasi / JWT
- **File:** `medikasentosa/src/main/java/com/medikasentosa/shared/security/JwtService.java`, `.../features/auth/controller/AuthController.java`, `.../features/auth/service/AuthService.java`, `.../shared/security/SecurityConfig.java`, `medikasentosa/src/main/resources/application.properties`, `medikasentosa/src/main/resources/schema.sql`
- **Line:** JwtService 38–120/137–175; AuthController 77–168; AuthService 87–170; application.properties 25–34; schema.sql token_version
- **Issue:** Secret JWT memiliki default yang dapat ditebak (`dev_jwt_secret_change_me...`), access token berlaku 24 jam, tidak ada klaim tipe token, tidak ada token version, dan tidak ada endpoint refresh/logout. Token yang dicuri tetap valid sampai 24 jam meskipun user logout, dinonaktifkan, atau password diubah.
- **Code:**
  ```java
  app.jwt.secret=${JWT_SECRET:dev_jwt_secret_change_me_only_for_local_development_32b}
  app.jwt.expiration-ms=86400000
  // token hanya berisi subject + expiry, tanpa type/version
  ```
- **Fix:** Access token 15 menit, refresh token 7 hari, klaim `type=access|refresh` dan `ver` (token version). `JwtFilter` memvalidasi tipe access, status aktif, dan `ver` terhadap DB. Refresh token hanya dikirim melalui cookie `HttpOnly`, `Secure` di production, `SameSite=Lax`, path `/api/auth`; endpoint `/api/auth/refresh`, `/api/auth/logout`, dan `/api/auth/me` ditambahkan. Logout menaikkan `token_version` sehingga seluruh access/refresh lama invalid. Secret <32 byte atau nilai default ditolak di production.
  ```java
  .claim("type", "access").claim("ver", versionOf(user))
  if (Boolean.FALSE.equals(user.getIsActive())) return false;
  return (version == null ? 0 : version) == versionOf(user);
  ```
  `users.token_version INTEGER NOT NULL DEFAULT 0` ditambahkan ke `schema.sql`.

## C-06 — PostgreSQL dan Redis terekspos ke publik dengan password lemah/tanpa autentikasi

- **Severity:** CRITICAL
- **Service:** Infrastructure / Database
- **Module:** Docker Compose, deployment
- **File:** `docker-compose.yml`, `deploy/docker-compose.svr1-db.yml`
- **Line:** root compose 24–64; deploy 13–48
- **Issue:** Port `5432` dan `6379` di-bind ke `0.0.0.0`, password Postgres memiliki default lemah, dan Redis berjalan tanpa `requirepass`. Siapa pun di jaringan dapat membaca/mengubah seluruh data pasien atau memakai Redis untuk serangan lanjutan.
- **Code:**
  ```yaml
  ports: ["5432:5432", "6379:6379"]
  POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-dev_password_change_me}
  # redis tanpa requirepass
  ```
- **Fix:** Root compose hanya bind `127.0.0.1`; deploy bind IP privat (`10.0.1.10`). Password Postgres dan Redis wajib diisi (`${VAR:?}`), Redis memakai `--requirepass` dan healthcheck terautentikasi. Tidak ada default password.
  ```yaml
  ports: ["127.0.0.1:5432:5432"]
  command: ["redis-server", "--requirepass", "${REDIS_PASSWORD:?REDIS_PASSWORD wajib diisi}"]
  ```

## C-07 — Grafana publik dengan kredensial default `admin/admin`

- **Severity:** CRITICAL
- **Service:** Infrastructure / Monitoring
- **Module:** Docker Compose
- **File:** `docker-compose.yml`
- **Line:** 233–249
- **Issue:** Grafana di-bind ke `0.0.0.0:3000` dan memakai `GF_SECURITY_ADMIN_PASSWORD: admin`. Penyerang dapat login, mengubah dashboard/datasource, dan mengekstrak metrik serta konfigurasi internal.
- **Code:**
  ```yaml
  ports: ["3000:3000"]
  environment:
    GF_SECURITY_ADMIN_USER: admin
    GF_SECURITY_ADMIN_PASSWORD: admin
  ```
- **Fix:** Port di-bind ke `127.0.0.1`, password wajib dari `GRAFANA_PASSWORD`, sign-up dan anonymous access dimatikan, dan container diberi `no-new-privileges`.
  ```yaml
  ports: ["127.0.0.1:3000:3000"]
  GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_PASSWORD:?GRAFANA_PASSWORD wajib diisi}
  GF_USERS_ALLOW_SIGN_UP: "false"
  GF_AUTH_ANONYMOUS_ENABLED: "false"
  ```

---

# HIGH

## H-01 — IDOR pada profil pasien dan pembuatan profil untuk user lain

- **Severity:** HIGH
- **Service:** Backend
- **Module:** Pasien
- **File:** `medikasentosa/src/main/java/com/medikasentosa/features/pasien/controller/PasienController.java`
- **Line:** 44–126
- **Issue:** `GET /api/pasien/{id}`, `GET /api/pasien/user/{userId}`, `POST /api/pasien`, dan `PUT /api/pasien/{id}` tidak memeriksa kepemilikan. Pasien dapat membaca data demografi pasien lain (NIK, alamat, no. HP) dan membuat profil pasien untuk akun orang lain.
- **Code:**
  ```java
  @GetMapping("/{id}")
  public PasienResponse getById(@PathVariable Long id) {
      return pasienService.getById(id); // tanpa cek pemilik
  }
  ```
- **Fix:** Daftar pasien admin-only. Detail diverifikasi `requirePasienAccess(id)`, lookup by user `requirePasienOfUserAccess(userId)`, pembuatan `requirePasienCreationForUser(request.userId())`, dan update `requirePasienWrite(id)`. Pasien hanya dapat mengubah demografinya sendiri; dokter tidak boleh mengubah demografi.
  ```java
  authorization.requirePasienAccess(id);
  authorization.requirePasienCreationForUser(request.userId());
  authorization.requirePasienWrite(id);
  ```

## H-02 — IDOR appointment/antrian dan perubahan status sembarang

- **Severity:** HIGH
- **Service:** Backend
- **Module:** Appointment, Antrian
- **File:** `.../appointment/controller/AppointmentController.java`, `.../antrian/controller/AntrianController.java`
- **Line:** Appointment 48–164; Antrian 44–83
- **Issue:** Pasien dapat membaca appointment pasien lain, melihat daftar appointment dokter lain, dan mengubah status appointment mana pun (mis. menjadi `SELESAI`) karena endpoint status hanya memvalidasi body. Generate/lookup antrian juga tidak memeriksa kepemilikan appointment.
- **Code:**
  ```java
  @PatchMapping("/{id}/status")
  public AppointmentResponse updateStatus(@PathVariable Long id, @RequestParam StatusAppointment status) {
      return appointmentService.updateStatus(id, status);
  }
  ```
- **Fix:** Daftar appointment global dan paginated admin-only. Detail dicek `requireAppointmentAccess`, daftar pasien `requirePasienAccess`, daftar dokter `requireDokterSelfOrAdmin`, pembuatan `requireAppointmentCreate`, dan perubahan status `requireAppointmentStatusChange`. Pasien hanya boleh mengubah status menjadi `BATAL` untuk appointment miliknya; dokter hanya appointment yang ditanganinya. Antrian memakai `requireAppointmentAccess` dan daftar antrian admin-only.
  ```java
  authorization.requireAppointmentStatusChange(id, status);
  authorization.requireAppointmentAccess(appointmentId);
  ```

## H-03 — Endpoint tulis katalog/klinis tanpa role ADMIN

- **Severity:** HIGH
- **Service:** Backend
- **Module:** Poli, Dokter, Jadwal Dokter, Obat, Kamar, Content, Operasi, Lab Order, Rawat Inap
- **File:** `.../poli/controller/PoliController.java`, `.../dokter/controller/DokterController.java`, `.../jadwaldokter/controller/JadwalDokterController.java`, `.../obat/controller/ObatController.java`, `.../rawatinap/controller/KamarController.java`, `.../content/controller/ContentController.java`, `.../jadwaloperasi/controller/JadwalOperasiController.java`, `.../laboratorium/controller/LabOrderController.java`, `.../rawatinap/controller/RawatInapController.java`
- **Line:** Poli 82/103/122; Dokter 110/132/151; Jadwal 72/93/112; Obat 73/94/113; Kamar 49/57/64; Content 24; Operasi 70/79; LabOrder 57/70; RawatInap 61/76
- **Issue:** Banyak operasi `POST/PUT/PATCH/DELETE` hanya membutuhkan autentikasi. Pasien/dokter dapat membuat poli, mengubah dokter, menghapus obat, mengubah kamar, mengubah konten publik, membuat jadwal operasi, membuat lab order, atau mencatat pasien masuk rawat inap.
- **Code:**
  ```java
  @PostMapping
  public PoliResponse create(@RequestBody PoliRequest request) { ... }
  // tanpa @PreAuthorize("hasRole('ADMIN')")
  ```
- **Fix:** Semua operasi tulis katalog diberi `@PreAuthorize("hasRole('ADMIN')")`. Operasi klinis yang memang boleh dilakukan dokter memakai `requireRekamMedisWrite`, `requireLabOrderWrite`, `requireRadiologiWrite`, `requireResepWrite`, `requireStaff`, `requireDokterSelfOrAdmin`, atau `requireRawatInapWrite`; daftar/aktivitas operasional dibatasi admin.
  ```java
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public PoliResponse create(@RequestBody PoliRequest request) { ... }
  ```

## H-04 — Access token disimpan di localStorage dan sesi tidak dapat diperbarui dengan aman

- **Severity:** HIGH
- **Service:** Frontend
- **Module:** Auth, API client
- **File:** `hospital-frontendjs/src/core/api/client.js`, `.../src/core/context/AuthContext.jsx`, `.../src/routes/ProtectedRoute.jsx`
- **Line:** client 23–106; AuthContext 32–92; ProtectedRoute 10–16
- **Issue:** Token JWT disimpan di `localStorage`, sehingga satu XSS dapat mencuri token admin dan menggunakannya sampai 24 jam. Tidak ada refresh token, single-flight refresh, atau pemulihan sesi; route guard juga dapat berkedip ke halaman login sebelum status auth diketahui.
- **Code:**
  ```js
  localStorage.setItem("token", token);
  const token = localStorage.getItem("token");
  ```
- **Fix:** Access token hanya di memori (`setAccessToken/getAccessToken`). Refresh token HttpOnly dikirim otomatis dengan `withCredentials: true`. Axios melakukan single-flight refresh pada 401 lalu mengulang request; kegagalan memancarkan `auth:expired`. `AuthProvider` memulihkan sesi saat mount dan menahan render sampai `loading` selesai; `ProtectedRoute` menunggu `loading`.
  ```js
  let accessToken = null; // tidak pernah localStorage
  api.post("/auth/refresh", null, { _skipAuthRefresh: true });
  ```

## H-05 — Tidak ada rate limiting pada login (brute force/credential stuffing)

- **Severity:** HIGH
- **Service:** Backend
- **Module:** Autentikasi
- **File:** `.../auth/controller/AuthController.java`, `.../shared/security/LoginRateLimiter.java`
- **Line:** AuthController 77–101; LoginRateLimiter 1–69
- **Issue:** Endpoint login dapat dipanggil tanpa batas. Penyerang dapat melakukan brute force/credential stuffing terhadap akun admin/pasien tanpa hambatan.
- **Code:**
  ```java
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
      return ResponseEntity.ok(authService.login(request));
  }
  ```
- **Fix:** `LoginRateLimiter` membatasi 5 kegagalan per 15 menit per kombinasi IP+email; kegagalan dihitung, login sukses mereset counter, dan pelanggaran mengembalikan 429. Nginx juga membatasi `/api/auth/login` 5 request/menit per IP.
  ```java
  rateLimiter.checkAllowed(key);
  // ...
  rateLimiter.recordFailure(key);
  ```

## H-06 — Kebijakan password lemah (6 karakter)

- **Severity:** HIGH
- **Service:** Backend
- **Module:** Registrasi, admin user management
- **File:** `.../auth/dto/RegisterRequest.java`, `.../admin/dto/DokterCreateRequest.java`, `.../admin/dto/PasienCreateRequest.java`, `.../admin/service/AdminService.java`
- **Line:** RegisterRequest 20–24; DokterCreateRequest 28–32; PasienCreateRequest 32–36; AdminService 84/137/257
- **Issue:** Password minimum 6 karakter tanpa kompleksitas. Password seperti `123456` diterima untuk akun baru, termasuk akun dokter yang dibuat admin.
- **Code:**
  ```java
  @Size(min = 6, message = "Password minimal 6 karakter") String password
  ```
- **Fix:** Semua DTO registrasi/pembuatan user mewajibkan 12–128 karakter serta minimal satu huruf dan satu angka. `AdminService` mengulang validasi yang sama di service layer (defense in depth). BCrypt strength dinaikkan ke 12.
  ```java
  @Size(min = 12, max = 128)
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$")
  ```

## H-07 — ML Service tanpa autentikasi dan tanpa validasi upload yang memadai

- **Severity:** HIGH
- **Service:** ML Service
- **Module:** Prediksi osteoporosis
- **File:** `ml-service/app/main.py`, `ml-service/app/preprocessing.py`, `medikasentosa/src/main/java/com/medikasentosa/features/screening/service/ScreeningService.java`
- **Line:** main 40–75/132–160; preprocessing 9–49; ScreeningService 88–125
- **Issue:** `/predict/osteoporosis` dapat dipanggil siapa pun yang menjangkau port 8001, tanpa API key. Upload hanya memeriksa `Content-Type` dari client, tanpa batas ukuran/dimensi, sehingga penyerang dapat mengirim file raksasa atau decompression bomb untuk menghabiskan RAM/GPU.
- **Code:**
  ```python
  if file.content_type not in ["image/jpeg", "image/png"]:
      raise HTTPException(...)
  img_bytes = await file.read()  # tanpa batas
  ```
- **Fix:** Wajib `X-Internal-API-Key` (dibandingkan `hmac.compare_digest`) untuk endpoint prediksi. Backend mengirim header tersebut dari `ML_INTERNAL_API_KEY`. Upload dibatasi 10 MB, diverifikasi dari magic bytes JPEG/PNG, struktur file diperiksa dengan Pillow `verify()`, dimensi maksimum 12.000 px dan 40 MP. Backend juga memvalidasi ukuran, content type, dan mengirim API key.
  ```python
  image_bytes = await file.read(MAX_UPLOAD_BYTES + 1)
  if len(image_bytes) > MAX_UPLOAD_BYTES: raise HTTPException(413, ...)
  validate_image_bytes(image_bytes)
  ```

## H-08 — Chatbot tanpa autentikasi, CORS terbuka, prompt injection, dan input tidak dibatasi

- **Severity:** HIGH
- **Service:** Chatbot Service
- **Module:** Chat / RAG / vLLM
- **File:** `chatbot-service/app/main.py`, `app/core/config.py`, `app/features/chat/router.py`, `app/features/chat/schema.py`, `app/features/chat/prompt_builder.py`
- **Line:** main 39–52; config 11–58; router 30–153; schema 18–47; prompt_builder 5–85
- **Issue:** Endpoint chat dapat dipanggil tanpa autentikasi dan CORS mengizinkan banyak origin dengan credentials. Pesan/riwayat tidak dibatasi, role riwayat berasal dari client, dan prompt tidak memisahkan data dari instruksi — memungkinkan prompt injection, pembengkakan prompt, dan penyalahgunaan vLLM.
- **Code:**
  ```python
  app.add_middleware(CORSMiddleware, allow_origins=[...], allow_credentials=True,
                     allow_methods=["*"], allow_headers=["*"])
  messages.append({"role": h.get("role", "user"), "content": h.get("content", "")})
  ```
- **Fix:** Tidak ada CORS (hanya server-to-server), wajib `X-Internal-API-Key`, pesan maks. 2.000 karakter, riwayat maks. 20 item, role hanya `user`/`assistant`, context maks. 100 item. Prompt builder membersihkan newline/control character dan menegaskan bahwa teks pengguna/FAQ adalah data, bukan instruksi. Error vLLM tidak lagi membocorkan detail internal.
  ```python
  @router.post("/message", dependencies=[Depends(require_internal_key)])
  class HistoryMessage(BaseModel):
      role: Literal["user", "assistant"]
      content: str = Field(..., max_length=MAX_MESSAGE_CHARS)
  ```

## H-09 — Inferensi ML tanpa batas konkurensi/timeout

- **Severity:** HIGH
- **Service:** ML Service
- **Module:** Prediksi osteoporosis
- **File:** `ml-service/app/main.py`
- **Line:** 43–58/160–200
- **Issue:** Setiap request menjalankan TensorFlow di thread tanpa batas jumlah, tanpa timeout, dan tanpa antrean. Beberapa request paralel dapat menghabiskan RAM/CPU/GPU dan membuat service tidak tersedia.
- **Code:**
  ```python
  img = preprocess(img_bytes)
  result = predict(img)  # blocking, tanpa semaphore/timeout
  ```
- **Fix:** Semaphore `MAX_CONCURRENCY` (default 2), antrean 5 detik (503 bila penuh), timeout inferensi 30 detik (504), dan slot semaphore ditahan sampai thread TensorFlow benar-benar selesai walau request timeout — mencegah bypass konkurensi lewat timeout.
  ```python
  await asyncio.wait_for(_inference_semaphore.acquire(), timeout=5)
  future = loop.run_in_executor(None, _run_inference, image_bytes)
  await asyncio.wait_for(asyncio.shield(future), timeout=INFERENCE_TIMEOUT_SECONDS)
  ```

## H-10 — Chatbot tanpa batas konkurensi/rate limit (vLLM exhaustion)

- **Severity:** HIGH
- **Service:** Chatbot Service
- **Module:** Chat / vLLM
- **File:** `chatbot-service/app/features/chat/router.py`, `app/core/config.py`
- **Line:** router 30–153; config 54–58
- **Issue:** Tidak ada batas request paralel atau rate limit. Satu klien dapat membanjiri vLLM, meningkatkan biaya GPU, dan menurunkan layanan bagi pasien lain.
- **Code:**
  ```python
  @router.post("/message")
  async def chat_message(req: ChatRequest):
      return ChatResponse(response=await chat_complete(messages))
  ```
- **Fix:** Semaphore `MAX_CONCURRENT_REQUESTS` (default 8) membungkus pemanggilan vLLM; rate limiter fixed-window 60 detik per `X-Client-Key`/IP (default 30 pesan/menit). Backend mengirim `X-Client-Key` berisi `user-{id}` atau `public`, dan juga memiliki `ChatRateLimiter` 30 pesan/menit.
  ```python
  _enforce_rate_limit(client_key)
  async with _request_semaphore:
      return await chat_complete(messages)
  ```

## H-11 — Tidak ada audit trail untuk akses data medis sensitif

- **Severity:** HIGH
- **Service:** Backend
- **Module:** Rekam Medis, Lab, Screening
- **File:** `.../shared/audit/AuditLogger.java`, controller terkait
- **Line:** AuditLogger 19–39; RekamMedis 59/75/91/110/134; LabHasil 49/65; Screening 55/71
- **Issue:** Pembacaan/penulisan rekam medis, hasil lab, dan screening tidak dicatat. Jika terjadi penyalahgunaan akses, tidak ada bukti aktor, resource, waktu, dan IP.
- **Code:**
  ```java
  @GetMapping("/{id}")
  public RekamMedisResponse getById(@PathVariable Long id) { ... } // tanpa audit
  ```
- **Fix:** `AuditLogger` menulis ke logger `AUDIT` dengan `action`, `resource`, `id`, `actor`, dan `ip`. Dipanggil pada baca/tulis rekam medis, baca/tulis hasil lab, upload screening, dan riwayat screening. Log dapat diarahkan ke file append-only/SIEM.
  ```java
  auditLogger.record("READ", "REKAM_MEDIS", id);
  auditLogger.record("CREATE", "REKAM_MEDIS", response.id());
  ```

## H-12 — PHI/PII bocor ke log aplikasi

- **Severity:** HIGH
- **Service:** Backend
- **Module:** Logging, JPA
- **File:** `medikasentosa/src/main/resources/application.properties`, `.../auth/service/AuthService.java`, `.../shared/seed/DataSeeder.java`
- **Line:** application.properties 18–19; AuthService 57–110; DataSeeder 77
- **Issue:** `show-sql` dan `format_sql` dapat diaktifkan, dan log login/registrasi menyebut email. Log aplikasi biasanya dikirim ke agregator dengan akses lebih luas daripada database, sehingga PII/PHI dapat bocor.
- **Code:**
  ```java
  log.info("Login attempt for email: {}", request.email());
  log.info("Registering new user with email: {}", request.email());
  ```
- **Fix:** `show-sql=false` dan `format_sql=false` secara default (production dipaksa false). Log autentikasi tidak lagi memuat email; hanya status generik. Email admin pada bootstrap tetap dicatat sebagai peristiwa provisioning, bukan kredensial.
  ```java
  log.info("Login attempt");
  log.warn("Login failed: invalid password");
  ```

## H-13 — Dependency frontend rentan (npm audit)

- **Severity:** HIGH
- **Service:** Frontend
- **Module:** Supply chain
- **File:** `hospital-frontendjs/package.json`, `package-lock.json`
- **Line:** dependencies 18/39
- **Issue:** Versi `axios` dan `vitest` yang lama memiliki kerentanan yang dilaporkan `npm audit`; dependency adalah permukaan serangan langsung di browser dan pipeline.
- **Code:**
  ```json
  "axios": "^1.x", "vitest": "^x"
  ```
- **Fix:** `axios` dinaikkan ke `^1.20.0`, `vitest` ke `^5.0.5`, lockfile diperbarui. `npm audit` sekarang melaporkan **0 vulnerability**. CI menjalankan `npm ci`, test, dan build.
  ```json
  "axios": "^1.20.0", "vitest": "^5.0.5"
  ```

## H-14 — Script deploy menyalin `.env`, virtualenv, dan cache ke server

- **Severity:** HIGH
- **Service:** DevOps
- **Module:** Deployment
- **File:** `deploy/deploy-all.sh`
- **Line:** 33–57
- **Issue:** `rsync` menyalin seluruh direktori service, termasuk `.env` berisi secret, `venv`, `.gradle`, `build`, dan `__pycache__`. Secret dapat mendarat di server yang salah atau tersimpan di backup/log deploy.
- **Code:**
  ```bash
  rsync -avz ../chatbot-service/ $SVR4:~/medika/chatbot-service/ --exclude chroma_db
  rsync -avz ../medikasentosa/ $SVR2:~/medika/medikasentosa/ --exclude .gradle --exclude build
  ```
- **Fix:** Semua rsync mengecualikan `.env`, `.env.*`, `venv`, `.venv`, `__pycache__`, `.gradle`, dan `build`. Secret production harus dibuat di server melalui mekanisme secret manager, bukan disalin dari laptop.
  ```bash
  rsync -avz ../chatbot-service/ $SVR4:~/medika/chatbot-service/ \
    --exclude chroma_db --exclude venv --exclude .venv \
    --exclude __pycache__ --exclude .env --exclude '.env.*'
  ```

## H-15 — Profil production tidak mewajibkan secret dan memakai `ddl-auto=update`

- **Severity:** HIGH
- **Service:** Backend / DevOps
- **Module:** Konfigurasi Spring, Docker Compose
- **File:** `medikasentosa/src/main/resources/application.properties`, `deploy/docker-compose.svr2-backend.yml`, `docker-compose.yml`
- **Line:** application.properties 76–92; deploy backend 20–40; root compose 88–105
- **Issue:** Tanpa profil production, aplikasi dapat memakai password DB/JWT default, `ddl-auto=update`, dan secret kosong; dokumentasi API juga dapat diaktifkan tanpa pembatasan. Deploy dapat berjalan dengan konfigurasi development tanpa gagal cepat.
- **Code:**
  ```properties
  spring.jpa.hibernate.ddl-auto=update
  spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:dev_password_change_me}
  app.jwt.secret=${JWT_SECRET:dev_jwt_secret_change_me...}
  ```
- **Fix:** Profil `production` mewajibkan `SPRING_DATASOURCE_*`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, `ML_INTERNAL_API_KEY`, `CHATBOT_INTERNAL_API_KEY`, dan `REDIS_PASSWORD` dari environment; `ddl-auto=validate`, `show-sql=false`, Swagger/docs false. Compose production mengaktifkan `SPRING_PROFILES_ACTIVE=production` dan memakai `${VAR:?}` agar gagal saat secret kosong.
  ```properties
  spring.config.activate.on-profile=production
  spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
  spring.jpa.hibernate.ddl-auto=validate
  app.jwt.secret=${JWT_SECRET}
  ```

## H-16 — Nginx tidak mengirim security header/CSP

- **Severity:** HIGH
- **Service:** Frontend / Infrastructure
- **Module:** Nginx
- **File:** `hospital-frontendjs/nginx.conf`
- **Line:** 19–37
- **Issue:** Tidak ada CSP, `X-Frame-Options`, `X-Content-Type-Options`, `Referrer-Policy`, COOP/CORP, atau `server_tokens off`. XSS menjadi lebih mudah dieksploitasi dan aplikasi dapat di-embed di iframe (clickjacking).
- **Code:**
  ```nginx
  server {
      listen 80;
      root /usr/share/nginx/html;
      # tanpa add_header ...
  }
  ```
- **Fix:** Ditambahkan CSP (`default-src 'self'`, `script-src 'self'`, `frame-ancestors 'none'`, `connect-src 'self' ws: wss:`), `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Referrer-Policy`, `Permissions-Policy`, COOP/CORP, `server_tokens off`, batas body 10 MB, dan rate limit. Header tetap diwarisi pada static assets/service worker.
  ```nginx
  add_header Content-Security-Policy "default-src 'self'; ...; frame-ancestors 'none'; ..." always;
  add_header X-Frame-Options "DENY" always;
  server_tokens off;
  ```

## H-17 — `/api/auth/**` terlalu luas di `permitAll`

- **Severity:** HIGH
- **Service:** Backend
- **Module:** Spring Security
- **File:** `.../shared/security/SecurityConfig.java`
- **Line:** 47–61
- **Issue:** Seluruh `/api/auth/**` dibuka tanpa autentikasi. Endpoint `/api/auth/me` dan endpoint auth baru yang ditambahkan di masa depan otomatis publik, sehingga mudah terjadi kebocoran data user.
- **Code:**
  ```java
  .requestMatchers("/api/auth/**", "/api/health").permitAll()
  ```
- **Fix:** Hanya `login`, `register`, `refresh`, dan `logout` yang publik; sisanya `/api/auth/**` wajib authenticated. Endpoint katalog publik dibatasi method GET; `/api/admin/**` admin-only; actuator hanya health/prometheus; `OPTIONS` diizinkan untuk CORS preflight.
  ```java
  .requestMatchers("/api/auth/login", "/api/auth/register",
          "/api/auth/refresh", "/api/auth/logout").permitAll()
  .requestMatchers("/api/auth/**").authenticated()
  ```

---

# MEDIUM

## M-01 — Dokumentasi API (Swagger/FastAPI docs) terekspos

- **Severity:** MEDIUM
- **Service:** Backend, ML, Chatbot
- **Module:** OpenAPI/Swagger
- **File:** `application.properties`, `ml-service/app/main.py`, `chatbot-service/app/main.py`, `.../shared/security/SecurityConfig.java`
- **Line:** application.properties 63–92; ML 106; chatbot 39; SecurityConfig 57
- **Issue:** Swagger UI, `/v3/api-docs`, `/docs`, dan `/redoc` dapat diakses publik. Dokumentasi mempermudah pemetaan endpoint, parameter, dan model data bagi penyerang.
- **Code:**
  ```properties
  springdoc.api-docs.enabled=true
  springdoc.swagger-ui.enabled=true
  ```
- **Fix:** Dokumentasi dimatikan secara default dan hanya aktif dengan `SWAGGER_ENABLED=true` / `ENABLE_DOCS=true` untuk development. Production memaksa false. Endpoint docs tetap memerlukan pembatasan jaringan bila diaktifkan.
  ```properties
  springdoc.api-docs.enabled=${SWAGGER_ENABLED:false}
  springdoc.swagger-ui.enabled=${SWAGGER_ENABLED:false}
  ```

## M-02 — Actuator/Prometheus terekspos

- **Severity:** MEDIUM
- **Service:** Backend
- **Module:** Actuator
- **File:** `application.properties`, `.../shared/security/SecurityConfig.java`
- **Line:** application.properties 56–61; SecurityConfig 59–60
- **Issue:** `/actuator/prometheus` dan `/actuator/health` publik. Metrik dapat membocorkan pola trafik, endpoint, dan kondisi internal; endpoint actuator lain hanya admin tetapi tetap ada.
- **Code:**
  ```java
  .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
  .requestMatchers("/actuator/**").hasRole("ADMIN")
  ```
- **Fix:** Hanya `health` dan `prometheus` yang diekspos, detail health `never`, dan backend di-bind ke loopback/IP privat pada deployment. Prometheus hanya di-bind `127.0.0.1`. Untuk production, scrape sebaiknya lewat network policy/internal network.
  ```properties
  management.endpoints.web.exposure.include=health,prometheus
  management.endpoint.health.show-details=never
  ```

## M-03 — CORS terlalu longgar

- **Severity:** MEDIUM
- **Service:** Backend
- **Module:** Spring Security CORS
- **File:** `.../shared/security/SecurityConfig.java`
- **Line:** 72–88
- **Issue:** `allowedHeaders("*")`, origin hardcoded, dan `allowCredentials(true)`. Kombinasi ini menyulitkan pembatasan dan berisiko jika origin ditambah tanpa review.
- **Code:**
  ```java
  config.setAllowedOrigins(List.of("http://localhost:5173"));
  config.setAllowedHeaders(List.of("*"));
  config.setAllowCredentials(true);
  ```
- **Fix:** Origin dibaca dari `CORS_ALLOWED_ORIGINS` (dipisah koma), header dibatasi ke `Authorization`, `Content-Type`, `Accept`, `X-Requested-With`, dan `maxAge` 1 jam. Production mewajibkan origin eksplisit; jangan memakai `*` bersama credentials.
  ```java
  config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))...toList());
  config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
  ```

## M-04 — Error internal bocor dari ML/chatbot

- **Severity:** MEDIUM
- **Service:** ML, Chatbot
- **Module:** Error handling
- **File:** `ml-service/app/main.py`, `chatbot-service/app/features/chat/router.py`, `chatbot-service/app/main.py`
- **Line:** ML 182–190; chatbot router 120–132; chatbot main 47–52
- **Issue:** `HTTPException(detail=f"Gagal terhubung ke vLLM: {str(e)}")` dan exception handler default dapat membocorkan path, stack trace, atau detail koneksi internal.
- **Code:**
  ```python
  except Exception as e:
      raise HTTPException(status_code=503, detail=f"Gagal terhubung ke vLLM: {str(e)}")
  ```
- **Fix:** Semua error eksternal dikembalikan generik; detail hanya di log server. Ditambahkan global exception handler chatbot yang mengembalikan `{"detail": "Internal server error"}` tanpa stack trace.
  ```python
  except Exception:
      logger.exception("Gagal memanggil vLLM")
      raise HTTPException(status_code=503, detail="Layanan chat sedang tidak tersedia...")
  ```

## M-05 — Rate limit/audit memakai `X-Forwarded-For` yang dapat dipalsukan

- **Severity:** MEDIUM
- **Service:** Backend, Nginx
- **Module:** Client IP resolution
- **File:** `.../shared/security/ClientIpResolver.java`, `.../auth/controller/AuthController.java`, `.../chat/controller/ChatController.java`, `.../shared/audit/AuditLogger.java`, `hospital-frontendjs/nginx.conf`
- **Line:** ClientIpResolver 15–35; AuthController 166–170; Nginx 58–59/77–78/84–85
- **Issue:** Backend mengambil elemen pertama `X-Forwarded-For`, yang dapat diisi klien. Penyerang dapat memutar IP untuk melewati rate limit atau mengacaukan audit log.
- **Code:**
  ```java
  String forwarded = request.getHeader("X-Forwarded-For");
  String ip = forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
  ```
- **Fix:** `ClientIpResolver` memakai `X-Real-IP` yang ditimpa Nginx dengan `$remote_addr`, divalidasi sebagai literal IP, lalu fallback `getRemoteAddr()`. Nginx menimpa `X-Forwarded-For` dengan `$remote_addr` (bukan `$proxy_add_x_forwarded_for`).
  ```java
  String realIp = request.getHeader("X-Real-IP");
  if (realIp != null && IP_LITERAL.matcher(realIp.trim()).matches()) return realIp.trim();
  return request.getRemoteAddr();
  ```
  **Residual:** bila backend dapat diakses langsung (bukan lewat Nginx), `X-Real-IP` masih dapat dipalsukan; backend sudah di-bind loopback/IP privat, dan production harus memblokir akses langsung.

## M-06 — `.env`/credentials dapat masuk ke Docker build context

- **Severity:** MEDIUM
- **Service:** DevOps
- **Module:** Docker build context
- **File:** `.dockerignore`, `medikasentosa/.dockerignore`, `ml-service/.dockerignore`, `chatbot-service/.dockerignore`, `hospital-frontendjs/.dockerignore`
- **Line:** root `.dockerignore` environment section; tiap subdirektori
- **Issue:** Beberapa `.dockerignore` hanya mengecualikan `.env` atau `.env.local`, tidak `.env.*`/`CREDENTIALS.md`. File secret dapat ikut ke image layer dan tidak hilang meskipun dihapus di layer berikutnya.
- **Code:**
  ```dockerignore
  .env
  .env.local
  ```
- **Fix:** Semua `.dockerignore` mengecualikan `.env`, `.env.*`, dan `CREDENTIALS.md`. Deploy rsync juga mengecualikan file yang sama.
  ```dockerignore
  .env
  .env.*
  CREDENTIALS.md
  ```

## M-07 — PHI (diagnosa/tindakan) dikirim ke vLLM

- **Severity:** MEDIUM (residual)
- **Service:** Backend, Chatbot
- **Module:** Chat context
- **File:** `.../features/chat/service/ChatService.java`, `chatbot-service/app/features/chat/prompt_builder.py`
- **Line:** ChatService 156–180; prompt_builder 52–58
- **Issue:** `buildContext()` mengirim rekam medis terakhir pasien (diagnosa dan tindakan) ke chatbot, lalu chatbot meneruskannya ke vLLM. Data medis sensitif meninggalkan batas backend dan diproses oleh model/penyedia GPU.
- **Code:**
  ```java
  var rm = rekamMedisRepository.findTopByPasienIdOrderByCreatedAtDesc(pasienId);
  context = new ContextData(..., rm.getDiagnosa(), rm.getTindakan(), ...);
  ```
- **Fix:** Untuk saat ini diterima karena vLLM berjalan internal dan hanya menerima data pasien yang sedang login. **Rekomendasi go-live:** hapus diagnosa/tindakan dari prompt, gunakan ringkasan non-identifiable, atau jalankan vLLM dalam boundary data yang sama dengan perjanjian processing. Jangan kirim NIK/alamat/no. HP.

## M-08 — Data medis tersimpan plaintext di database

- **Severity:** MEDIUM (residual)
- **Service:** Database
- **Module:** PostgreSQL schema
- **File:** `medikasentosa/src/main/resources/schema.sql`, entity pasien/rekam medis
- **Line:** tabel `pasien`, `rekam_medis`, `lab_hasil`, `radiologi`, `chat_message`
- **Issue:** NIK, alamat, no. HP, diagnosa, hasil lab, dan isi chat tersimpan plaintext. Backup database, snapshot volume, atau akses langsung ke disk dapat membocorkan PHI.
- **Code:**
  ```sql
  nik VARCHAR(...), alamat TEXT, no_hp VARCHAR(...), diagnosa TEXT, tindakan TEXT
  ```
- **Fix:** Belum diubah karena memerlukan migrasi data dan manajemen kunci. **Rekomendasi:** enkripsi at-rest (LUKS/EBS encryption), enkripsi kolom sensitif dengan KMS/pgcrypto, pembatasan backup, dan retensi log. Minimal pastikan volume Docker berada di disk terenkripsi dan akses DB hanya dari backend.

## M-09 — Pasien dapat menandai tagihannya sendiri `LUNAS`

- **Severity:** MEDIUM (residual)
- **Service:** Backend
- **Module:** Pembayaran
- **File:** `.../pembayaran/controller/PembayaranController.java`, `.../pembayaran/service/PembayaranService.java`
- **Line:** controller 47–52; service proses pembayaran
- **Issue:** `POST /api/pembayaran/proses` mengizinkan pasien memproses pembayaran billing miliknya dan menandai status `LUNAS` tanpa verifikasi eksternal (gateway/kasir). Ini adalah risiko integritas finansial, bukan kebocoran data.
- **Code:**
  ```java
  @PostMapping("/proses")
  public PembayaranResponse proses(@Valid @RequestBody PembayaranRequest req) {
      authorization.requireBillingPayment(req.billingId());
      return pembayaranService.proses(req);
  }
  ```
- **Fix:** Otorisasi sudah membatasi ke billing sendiri, tetapi status `LUNAS` masih self-declared. **Rekomendasi:** ubah alur menjadi `PENDING`/`MENUNGGU_VERIFIKASI`; hanya kasir/admin atau callback gateway terverifikasi yang boleh menetapkan `LUNAS`. Untuk demo, dokumentasikan bahwa pembayaran tidak terhubung gateway.

## M-10 — Rotasi refresh token tidak membatalkan token lama secara kriptografis

- **Severity:** MEDIUM (residual)
- **Service:** Backend
- **Module:** Autentikasi
- **File:** `.../features/auth/controller/AuthController.java`, `.../shared/security/JwtService.java`
- **Line:** AuthController 103–120; JwtService 107–175
- **Issue:** `/api/auth/refresh` menerbitkan cookie refresh baru, tetapi refresh token lama tetap valid sampai expiry karena tidak ada penyimpanan token/jti yang dicabut. Jika token lama dicuri, penyerang dapat terus menukarnya menjadi access token.
- **Code:**
  ```java
  AuthResponse auth = authService.refresh(refreshToken);
  issueRefreshCookie(response, auth); // token lama tidak di-revoke
  ```
- **Fix:** Mitigasi saat ini: refresh token 7 hari, HttpOnly, Secure di production, `token_version` mencabut semua token saat logout/reset. **Rekomendasi:** simpan hash refresh token/jti per sesi, rotasi dengan deteksi reuse, dan cabut token lama pada setiap refresh (refresh-token family).

## M-11 — Rate limiter in-memory tidak konsisten di multi-instance

- **Severity:** MEDIUM (residual)
- **Service:** Backend, Chatbot
- **Module:** Rate limiting
- **File:** `.../shared/security/LoginRateLimiter.java`, `.../shared/security/ChatRateLimiter.java`, `chatbot-service/app/features/chat/router.py`
- **Line:** LoginRateLimiter 24–50; ChatRateLimiter 22–45; chatbot router 30–60
- **Issue:** Counter disimpan di memori per proses. Jika backend/chatbot di-scale horizontal, penyerang dapat mendistribusikan request ke beberapa instance untuk melewati batas; restart juga mereset counter.
- **Code:**
  ```java
  private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();
  ```
- **Fix:** Cukup untuk deployment single-instance saat ini. **Rekomendasi:** pindahkan ke Redis (atomic INCR + TTL) atau API gateway rate limit saat multi-instance.

## M-12 — Terminasi TLS tidak ada di repository

- **Severity:** MEDIUM (residual operasional)
- **Service:** Infrastructure
- **Module:** Nginx / deployment
- **File:** `hospital-frontendjs/nginx.conf`, `deploy/docker-compose.svr3-frontend.yml`
- **Line:** nginx 6–8; deploy frontend 14–15
- **Issue:** Nginx hanya `listen 80`. Jika diekspos langsung ke internet, kredensial, cookie, dan PHI dikirim plaintext; HSTS juga belum aktif.
- **Code:**
  ```nginx
  server { listen 80; server_name localhost; ... }
  ```
- **Fix:** Terminasi TLS adalah prasyarat operasional (reverse proxy/load balancer dengan sertifikat). **Rekomendasi:** terminate TLS di edge, redirect 80→443, aktifkan HSTS setelah HTTPS stabil, set `REFRESH_COOKIE_SECURE=true`, dan batasi backend hanya menerima traffic dari proxy. Repository tetap HTTP untuk demo lokal.

## M-13 — Image container memakai tag `latest`

- **Severity:** MEDIUM (residual)
- **Service:** Infrastructure
- **Module:** Docker images
- **File:** `docker-compose.yml`, `deploy/docker-compose.svr5-gpu.yml`
- **Line:** root compose 213/233; deploy GPU 20 (komentar)
- **Issue:** `prom/prometheus:latest` dan `grafana/grafana:latest` tidak deterministik; update tak terduga dapat memasukkan regresi/kerentanan, dan supply chain tidak dapat diverifikasi per digest.
- **Code:**
  ```yaml
  image: prom/prometheus:latest
  image: grafana/grafana:latest
  ```
- **Fix:** **Rekomendasi:** pin ke versi minor/patch dan digest (`@sha256:...`), perbarui melalui PR dengan review. Image aplikasi sudah di-build dari source, bukan `latest`.

## M-14 — CI belum melakukan dependency/image vulnerability scan

- **Severity:** MEDIUM (residual)
- **Service:** DevOps
- **Module:** CI/CD
- **File:** `.github/workflows/ci.yml`
- **Line:** seluruh pipeline
- **Issue:** CI hanya build/test. Tidak ada `npm audit --audit-level=high`, Gradle dependency-check, Trivy/Grype image scan, atau secret scanning. Kerentanan supply chain dapat lolos ke production.
- **Code:**
  ```yaml
  # tidak ada step scan
  ```
- **Fix:** `npm audit` sudah 0 saat audit manual. **Rekomendasi:** tambahkan `npm audit --audit-level=high`, OWASP Dependency-Check/Snyk untuk Gradle, Trivy untuk image, dan gitleaks/trufflehog untuk secret scanning; jadikan blocking untuk CRITICAL/HIGH.

## M-15 — Verifikasi checksum model ML bersifat opsional

- **Severity:** MEDIUM (residual)
- **Service:** ML
- **Module:** Model loading
- **File:** `ml-service/app/predictor.py`
- **Line:** 19–44
- **Issue:** `MODEL_SHA256` hanya diverifikasi jika diisi. Model `.keras` dapat ditukar/dimodifikasi di storage tanpa terdeteksi, sehingga penyerang yang dapat menulis ke volume model dapat mengubah hasil diagnosis.
- **Code:**
  ```python
  if not MODEL_SHA256:
      return
  ```
- **Fix:** Dukungan verifikasi sudah ada. **Rekomendasi:** wajibkan `MODEL_SHA256` di production dan deploy model dari artifact bertanda tangan; mount model read-only.

## M-16 — Kontak dokter terekspos pada katalog publik

- **Severity:** MEDIUM (residual)
- **Service:** Backend / Frontend
- **Module:** Dokter
- **File:** `.../dokter/dto/DokterResponse.java`, `.../dokter/controller/DokterController.java`, `hospital-frontendjs/src/features/dokter`
- **Line:** DokterController 44–93; DokterResponse field `noHp`
- **Issue:** `GET /api/dokter` publik mengembalikan `noHp` (dan data profil dokter). Nomor HP dapat dipanen untuk spam/phishing; sebagian data mungkin tidak perlu untuk booking.
- **Code:**
  ```java
  new DokterResponse(id, userId, nama, email, poliId, namaPoli, noStr, spesialisasi, noHp)
  ```
- **Fix:** Belum diubah karena frontend mungkin menampilkan kontak. **Rekomendasi:** keluarkan `noHp`/`email` dari response publik, sediakan endpoint kontak terautentikasi, atau tampilkan hanya nomor kantor poli.

## M-17 — PII masih tercatat pada log operasional

- **Severity:** MEDIUM (residual)
- **Service:** Backend
- **Module:** Logging
- **File:** `.../admin/service/AdminService.java`, `.../shared/audit/AuditLogger.java`
- **Line:** AdminService 83/136; AuditLogger 24–31
- **Issue:** AdminService mencatat email saat membuat dokter/pasien; audit log mencatat actor email dan IP. Ini memang diperlukan untuk audit, tetapi harus dilindungi karena merupakan PII.
- **Code:**
  ```java
  log.info("Creating dokter with user: email={}", request.email());
  audit.info("... actor={} ip={}", actor, clientIp());
  ```
- **Fix:** Log email pada AdminService dapat dihapus/di-mask. **Rekomendasi:** audit log disimpan append-only dengan akses terbatas, retensi jelas, dan masking email/IP bila tidak diperlukan; jangan kirim ke agregator publik.

## M-18 — Pemindaian CVE dependency Python tidak tersedia

- **Severity:** MEDIUM (residual)
- **Service:** ML, Chatbot
- **Module:** Supply chain Python
- **File:** `ml-service/requirements.txt`, `chatbot-service/requirements.txt`
- **Line:** seluruh file
- **Issue:** Tidak ada `pip-audit`/Safety di CI atau environment saat audit; kerentanan TensorFlow/OpenCV/FastAPI/transformers dapat tidak terdeteksi. Pemindaian otomatis tidak dapat dijalankan karena tooling/DB CVE tidak tersedia pada audit ini.
- **Code:**
  ```text
  # requirements.txt tanpa lock hash / audit
  ```
- **Fix:** **Rekomendasi:** pin versi + hash, jalankan `pip-audit -r requirements.txt` di CI, rebuild image saat CVE baru, dan gunakan base image Python yang di-scan.

---

# LOW

## L-01 — Dokumentasi masih mencantumkan kredensial demo dan URL Swagger default

- **Severity:** LOW
- **Service:** Documentation
- **Module:** README/docs
- **File:** `README.md`, `docs/5-docker-local.md`, `docs/7-cicd-github-actions.md`
- **Line:** README 599–603/634; docs 5 31–106; docs 7 43
- **Issue:** Dokumentasi lama memuat password `<demo-password>`/`<default-admin-password>`, default `dev_password_change_me`, dan URL Swagger yang seolah selalu aktif.
- **Code:**
  ```markdown
  - Admin: `admin@medikasentosa.id` / `<demo-password>`
  - Semua password: `<demo-password>`
  ```
- **Fix:** Diganti dengan penjelasan password acak + `CREDENTIALS.md` (gitignored), URL Swagger sebagai opsional/development, dan secret wajib dari environment. `docs/5-docker-local.md` menjelaskan compose menolak start bila secret kosong.

## L-02 — Collection Insomnia memuat password demo dan batas password lama

- **Severity:** LOW
- **Service:** Documentation
- **Module:** API collection
- **File:** `medikasentosa/Insomnia_2026-07-24.yaml`
- **Line:** 22–23/115/230/288; dokumentasi field password 223
- **Issue:** Request contoh memakai password demo yang sama dan dokumentasi menyebut minimum 6 karakter.
- **Code:**
  ```yaml
  "password": "<demo-password>"
  ```
- **Fix:** Password contoh diganti placeholder `GantiPasswordAnda12`, dan dokumentasi diubah menjadi minimal 12 karakter + huruf/angka. Tidak ada kredensial nyata di collection.

## L-03 — Versi Nginx terekspos

- **Severity:** LOW
- **Service:** Frontend / Infrastructure
- **Module:** Nginx
- **File:** `hospital-frontendjs/nginx.conf`
- **Line:** 31–32
- **Issue:** Header `Server` menampilkan versi Nginx, membantu penyerang memetakan kerentanan versi.
- **Code:**
  ```nginx
  # server_tokens default on
  ```
- **Fix:** `server_tokens off;` ditambahkan.

## L-04 — Route guard berkedip ke login sebelum status sesi diketahui

- **Severity:** LOW
- **Service:** Frontend
- **Module:** Routing/auth
- **File:** `hospital-frontendjs/src/routes/ProtectedRoute.jsx`, `.../src/core/context/AuthContext.jsx`
- **Line:** ProtectedRoute 10–16; AuthContext 32–69
- **Issue:** Sebelum bootstrap refresh selesai, `isAuthenticated=false` dapat menyebabkan redirect ke `/login` walau sesi valid.
- **Code:**
  ```jsx
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
  ```
- **Fix:** `loading` state ditambahkan; guard menampilkan indikator "Memuat sesi…" sampai refresh selesai.

## L-05 — Password PostgreSQL lokal masih lemah (`.env` gitignored)

- **Severity:** LOW (residual lokal)
- **Service:** Local environment
- **Module:** `.env`
- **File:** `.env`
- **Line:** 5/8
- **Issue:** `.env` lokal masih memakai `POSTGRES_PASSWORD=<demo-password>` agar cocok dengan volume Postgres yang sudah ada. File ini tidak pernah di-track dan hanya untuk demo lokal.
- **Code:**
  ```env
  POSTGRES_PASSWORD=<demo-password>
  ```
- **Fix:** Tidak dirotasi otomatis karena akan memutus volume lokal yang berjalan. **Rekomendasi:** sebelum demo bersama/production, buat volume baru dan gunakan password acak; production memakai secret manager. `.env` sudah di-gitignore dan tidak pernah ada di history.

## L-06 — Health check chatbot membocorkan detail model/RAG

- **Severity:** LOW
- **Service:** Chatbot
- **Module:** Health
- **File:** `chatbot-service/app/features/chat/router.py`
- **Line:** 62–66
- **Issue:** `/chat/health` mengembalikan nama model, status vLLM, dan jumlah dokumen RAG — informasi infrastruktur yang tidak perlu.
- **Code:**
  ```python
  return {"status": "UP", "model": VLLM_MODEL, "vllm_reachable": vllm_up, "rag_docs": ...}
  ```
- **Fix:** Health check hanya mengembalikan status dan nama service.

## L-07 — Header `X-XSS-Protection` sudah usang

- **Severity:** LOW (residual)
- **Service:** Frontend / Infrastructure
- **Module:** Nginx
- **File:** `hospital-frontendjs/nginx.conf`
- **Line:** 22
- **Issue:** `X-XSS-Protection` diabaikan browser modern dan dapat memicu perilaku lama yang tidak diinginkan; bukan mitigasi XSS yang efektif.
- **Code:**
  ```nginx
  add_header X-XSS-Protection "1; mode=block" always;
  ```
- **Fix:** Dipertahankan untuk kompatibilitas browser lama; CSP `script-src 'self'` adalah kontrol utama. **Rekomendasi:** hapus header ini pada hardening berikutnya.

---

# Verifikasi dan Status

## Perbaikan yang diverifikasi

| Pemeriksaan | Hasil |
|---|---|
| `medikasentosa/./gradlew test` | 119 test lulus, 3 skipped |
| `medikasentosa/./gradlew compileJava` | BUILD SUCCESSFUL |
| `hospital-frontendjs/npm test` | 2 file, 7 test lulus |
| `hospital-frontendjs/npm run build` | Berhasil |
| `hospital-frontendjs/npm audit` | 0 vulnerability |
| `py_compile` ML + chatbot | Lulus |
| `docker compose --env-file .env config -q` | Lulus |
| Deploy compose `config -q` (dengan dummy secret) | 5 file lulus |
| Pencarian SQL injection/string-concat JPQL | Tidak ditemukan |
| Pencarian `dangerouslySetInnerHTML`/`innerHTML`/`eval`/`new Function` | Tidak ditemukan |
| `.env`/`CREDENTIALS.md` tracked di Git | Tidak ada |
| Bind `0.0.0.0` pada service internal | Tidak ada (loopback/IP privat) |

## Residual risk yang wajib menjadi prasyarat go-live

1. **TLS termination** di edge/reverse proxy, redirect HTTP→HTTPS, HSTS, dan `REFRESH_COOKIE_SECURE=true` (M-12).
2. **Enkripsi at-rest** untuk database/volume dan kolom PHI sensitif (M-08).
3. **Kebijakan data untuk vLLM**: hapus diagnosa/tindakan dari prompt atau pastikan boundary data/perjanjian processing (M-07).
4. **Verifikasi pembayaran** oleh kasir/gateway; jangan biarkan pasien menetapkan `LUNAS` sendiri (M-09).
5. **Refresh token family/reuse detection** dan penyimpanan hash token (M-10).
6. **Rate limit terdistribusi** (Redis/API gateway) saat multi-instance (M-11).
7. **Pin image + digest** dan **CI supply-chain scan** (M-13, M-14, M-18).
8. **Model checksum wajib** dan mount read-only (M-15).
9. **Minimisasi data publik dokter** dan **masking PII pada log** (M-16, M-17).
10. **Network policy**: backend/DB/Redis/ML/chatbot hanya menerima traffic dari service yang berwenang; blokir akses langsung ke `X-Real-IP` (M-05).

## Kesimpulan

Seluruh temuan **CRITICAL (7)** dan **HIGH (17)** telah diperbaiki pada kode/konfigurasi repository dan diverifikasi dengan test, build, audit dependency, serta validasi compose. Residual **MEDIUM (12)** dan **LOW (2)** memerlukan keputusan arsitektur/operasional dan harus ditutup sebelum go-live produksi.

## Total Akhir

| Severity | Total | Diperbaiki | Residual |
|---|---:|---:|---:|
| CRITICAL | 7 | 7 | 0 |
| HIGH | 17 | 17 | 0 |
| MEDIUM | 18 | 6 | 12 |
| LOW | 7 | 5 | 2 |
| **TOTAL** | **49** | **35** | **14** |
