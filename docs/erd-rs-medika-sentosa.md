# ERD — RS Medika Sentosa (Enterprise)

Desain database lengkap untuk RS enterprise. Ditandai fase pengerjaan:
`[MVP]` = kerjakan sekarang · `[FASE 2]` = penunjang medis · `[FASE 3]` = keuangan & manajemen

Notasi: **PK** = Primary Key, **FK** = Foreign Key, `→` = referensi ke tabel lain.

---

## KELOMPOK 1 — USER & AKTOR (MVP)

### users [MVP]
Akun login untuk semua aktor (pasien, dokter, admin).
```
PK  id              BIGINT
    nama            VARCHAR
    email           VARCHAR (unique)
    password        VARCHAR (hash BCrypt)
    role            ENUM (PASIEN, DOKTER, ADMIN)
    created_at      TIMESTAMP
```

### pasien [MVP]
Data detail pasien (1 user PASIEN punya 1 profil pasien).
```
PK  id              BIGINT
FK  user_id         → users.id
    no_rekam_medis  VARCHAR (unique)  -- nomor RM
    nik             VARCHAR
    tanggal_lahir   DATE
    jenis_kelamin   ENUM (L, P)
    alamat          TEXT
    no_hp           VARCHAR
    gol_darah       VARCHAR
    created_at      TIMESTAMP
```

### dokter [MVP]
Data detail dokter (1 user DOKTER punya 1 profil dokter).
```
PK  id              BIGINT
FK  user_id         → users.id
FK  poli_id         → poli.id
    no_str          VARCHAR   -- Surat Tanda Registrasi
    spesialisasi    VARCHAR
    no_hp           VARCHAR
    created_at      TIMESTAMP
```

---

## KELOMPOK 2 — LAYANAN & POLI (MVP)

### poli [MVP]
Poliklinik / departemen (poli umum, jantung, tulang, dll).
```
PK  id              BIGINT
    nama            VARCHAR   -- "Poli Jantung"
    deskripsi       TEXT
    is_active       BOOLEAN
```

### jadwal_dokter [MVP]
Jadwal praktik dokter per hari + kuota pasien.
```
PK  id              BIGINT
FK  dokter_id       → dokter.id
    hari            ENUM (SENIN..MINGGU)
    jam_mulai       TIME
    jam_selesai     TIME
    kuota           INT       -- maks pasien per sesi
    is_active       BOOLEAN
```

---

## KELOMPOK 3 — APPOINTMENT & ANTRIAN (MVP)

### appointment [MVP]
Janji temu pasien dengan dokter.
```
PK  id              BIGINT
FK  pasien_id       → pasien.id
FK  dokter_id       → dokter.id
FK  jadwal_id       → jadwal_dokter.id
    tanggal         DATE
    status          ENUM (MENUNGGU, DIKONFIRMASI,
                          SELESAI, DIBATALKAN)
    keluhan         TEXT
    created_at      TIMESTAMP
```

### antrian [MVP]
Nomor antrian per appointment.
```
PK  id              BIGINT
FK  appointment_id  → appointment.id
    nomor_antrian   VARCHAR   -- "A-012"
    status          ENUM (MENUNGGU, DIPANGGIL, SELESAI)
    created_at      TIMESTAMP
```

---

## KELOMPOK 4 — REKAM MEDIS (MVP)

### rekam_medis [MVP]
Catatan kunjungan: diagnosa, tindakan (dibuat dokter saat pasien diperiksa).
```
PK  id              BIGINT
FK  pasien_id       → pasien.id
FK  dokter_id       → dokter.id
FK  appointment_id  → appointment.id
    tanggal         TIMESTAMP
    anamnesa        TEXT      -- keluhan pasien
    diagnosa        TEXT
    tindakan        TEXT
    catatan         TEXT
```

### resep [MVP]
Resep obat dari satu rekam medis.
```
PK  id              BIGINT
FK  rekam_medis_id  → rekam_medis.id
FK  obat_id         → obat.id
    jumlah          INT
    aturan_pakai    VARCHAR   -- "3x1 sehari"
```

### screening [MVP]  ← SUDAH ADA di project kamu
Hasil skrining osteoporosis AI.
```
PK  id              BIGINT
FK  user_id         → users.id
    label           VARCHAR   -- Normal/Osteopenia/Osteoporosis
    confidence      DOUBLE
    gradcam_image   TEXT      -- base64
    latency_ms      BIGINT
    created_at      TIMESTAMP
```

---

## KELOMPOK 5 — PENUNJANG MEDIS (FASE 2)

### risk_assessment [FASE 2]
Hasil prediksi risiko diabetes dari model ML tabular. Input data kesehatan pasien, output tingkat risiko + probabilitas.
```
PK  id                   BIGINT
FK  user_id              → users.id
    umur                 INT
    jumlah_kehamilan     INT       -- pregnancies (dataset Pima)
    glukosa              DOUBLE     -- kadar glukosa plasma
    tekanan_darah        DOUBLE     -- diastolik (mmHg)
    ketebalan_kulit      DOUBLE     -- skin thickness (mm)
    insulin              DOUBLE     -- serum insulin
    bmi                  DOUBLE
    diabetes_pedigree    DOUBLE     -- riwayat keluarga
    hasil_risiko         VARCHAR    -- RENDAH / TINGGI
    probabilitas         DOUBLE     -- skor 0-1
    rekomendasi          TEXT       -- saran tindak lanjut
    created_at           TIMESTAMP
```

### obat [FASE 2]
Master data obat.
```
PK  id              BIGINT
    nama            VARCHAR
    satuan          VARCHAR   -- tablet, botol
    harga           DECIMAL
    stok            INT
```

### lab_order [FASE 2]
Permintaan tes laboratorium.
```
PK  id              BIGINT
FK  rekam_medis_id  → rekam_medis.id
FK  dokter_id       → dokter.id
    jenis_tes       VARCHAR   -- "Darah lengkap"
    status          ENUM (DIMINTA, PROSES, SELESAI)
    created_at      TIMESTAMP
```

### lab_hasil [FASE 2]
Hasil dari lab_order.
```
PK  id              BIGINT
FK  lab_order_id    → lab_order.id
    parameter       VARCHAR   -- "Hemoglobin"
    nilai           VARCHAR
    satuan          VARCHAR
    nilai_normal    VARCHAR
```

### radiologi [FASE 2]
Order & hasil radiologi (termasuk X-ray osteoporosis).
```
PK  id              BIGINT
FK  rekam_medis_id  → rekam_medis.id
FK  screening_id    → screening.id  (nullable, kalau AI dipakai)
    jenis           VARCHAR   -- "X-Ray Tulang"
    gambar_url      VARCHAR
    hasil_baca      TEXT
    created_at      TIMESTAMP
```

### kamar [FASE 2]
Master kamar rawat inap.
```
PK  id              BIGINT
    nomor_kamar     VARCHAR
    kelas           ENUM (VIP, KELAS_1, KELAS_2, KELAS_3)
    tarif_per_hari  DECIMAL
    status          ENUM (KOSONG, TERISI, MAINTENANCE)
```

### rawat_inap [FASE 2]
Pasien yang menginap.
```
PK  id              BIGINT
FK  pasien_id       → pasien.id
FK  kamar_id        → kamar.id
FK  dokter_id       → dokter.id
    tanggal_masuk   TIMESTAMP
    tanggal_keluar  TIMESTAMP (nullable)
    status          ENUM (DIRAWAT, PULANG)
```

### jadwal_operasi [FASE 2]
Penjadwalan operasi.
```
PK  id              BIGINT
FK  pasien_id       → pasien.id
FK  dokter_id       → dokter.id  (dokter bedah utama)
    nama_operasi    VARCHAR
    ruang_operasi   VARCHAR
    tanggal         TIMESTAMP
    status          ENUM (DIJADWALKAN, SELESAI, DIBATALKAN)
    catatan         TEXT
```

### chat_session [FASE 2]
Sesi percakapan pasien dengan chatbot Senta (Qwen/vLLM).
```
PK  id              BIGINT
FK  user_id         → users.id
    judul           VARCHAR   -- ringkasan topik chat
    created_at      TIMESTAMP
```

### chat_message [FASE 2]
Pesan dalam satu sesi chat (dari user maupun bot).
```
PK  id              BIGINT
FK  session_id      → chat_session.id
    role            ENUM (USER, BOT)
    content         TEXT
    created_at      TIMESTAMP
```

---

## KELOMPOK 6 — KEUANGAN (FASE 3)

### billing [FASE 3]
Tagihan pasien (gabungan biaya konsultasi, obat, tindakan).
```
PK  id              BIGINT
FK  pasien_id       → pasien.id
FK  appointment_id  → appointment.id (nullable)
FK  rawat_inap_id   → rawat_inap.id (nullable)
    total           DECIMAL
    status          ENUM (BELUM_BAYAR, LUNAS, DIBATALKAN)
    created_at      TIMESTAMP
```

### billing_item [FASE 3]
Rincian item dalam satu tagihan.
```
PK  id              BIGINT
FK  billing_id      → billing.id
    deskripsi       VARCHAR   -- "Konsultasi Dokter"
    qty             INT
    harga_satuan    DECIMAL
    subtotal        DECIMAL
```

### pembayaran [FASE 3]
Transaksi pembayaran (BPJS ditunda — cukup kolom metode).
```
PK  id              BIGINT
FK  billing_id      → billing.id
    metode          ENUM (TUNAI, TRANSFER, KARTU, BPJS*)
    jumlah_bayar    DECIMAL
    status          ENUM (PENDING, BERHASIL, GAGAL)
    ref_pembayaran  VARCHAR   -- ID dari Midtrans dll
    created_at      TIMESTAMP
```
> *BPJS: masuk sebagai opsi enum, tapi belum diproses (ditunda).

---

## RINGKASAN RELASI (Alur Utama)

```
users ──1:1── pasien ──┐
users ──1:1── dokter ──┤
                       │
poli ──1:N── dokter    │
dokter ──1:N── jadwal_dokter
                       │
pasien ──1:N── appointment ──N:1── dokter
appointment ──1:1── antrian
appointment ──1:1── rekam_medis
                       │
rekam_medis ──1:N── resep ──N:1── obat
rekam_medis ──1:N── lab_order ──1:N── lab_hasil
rekam_medis ──1:N── radiologi ──N:1── screening
                       │
pasien ──1:N── rawat_inap ──N:1── kamar
pasien ──1:N── jadwal_operasi ──N:1── dokter
                       │
pasien ──1:N── billing ──1:N── billing_item
billing ──1:N── pembayaran
                       │
users ──1:N── chat_session ──1:N── chat_message
```

---

## URUTAN IMPLEMENTASI

```
SEKARANG (MVP):
  users → pasien → dokter → poli → jadwal_dokter
  → appointment → antrian → rekam_medis → resep → screening

FASE 2 (penunjang medis + chatbot):
  obat → lab_order → lab_hasil → radiologi
  → kamar → rawat_inap → jadwal_operasi
  → chat_session → chat_message (Chatbot Senta)

FASE 3 (keuangan):
  billing → billing_item → pembayaran
```

---

## CATATAN DESAIN

- **user vs pasien/dokter dipisah**: `users` untuk login (semua role), `pasien`/`dokter` untuk data detail. Ini pola umum & rapi.
- **screening sudah ada** di project kamu, tinggal dihubungkan ke `radiologi` nanti (Fase 2).
- **risk_assessment** (Fase 2) = fitur AI kedua: prediksi risiko diabetes dari data tabular (model ringan, cocok CPU). Berdiri sendiri seperti `screening`, terhubung ke `users`. Bisa jadi trigger rujukan ke poli penyakit dalam.
- **BPJS ditunda**: struktur `pembayaran` sudah siap (ada enum BPJS), tinggal implementasi integrasi V-Claim nanti.
- **Foreign key nullable** di beberapa tempat (misal `billing.rawat_inap_id`) karena tidak semua tagihan dari rawat inap.
- Desain sudah **normalized** (3NF) — tidak ada data redundan.
