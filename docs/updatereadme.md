Saya ingin README.md project ini ditulis dengan gaya yang detail & naratif —
tiap layanan dijelaskan tanggung jawabnya, alur kerja dijelaskan langkah demi
langkah, dan ada catatan jujur soal apa yang sudah jalan vs belum. Ini BUKAN
gaya README template generik dengan bullet pendek-pendek, tapi juga BUKAN
berarti melebih-lebihkan pencapaian — kejujuran tetap nomor satu.

TUJUAN: dokumentasi portofolio GitHub profesional untuk lamaran magang & CPNS.
Pembaca target: recruiter/reviewer teknis.

LANGKAH KERJA:

1. SCAN kondisi project SEBENARNYA sebelum menulis apapun:
   - backend/src/main/java/com/medikasentosa/features/ → modul apa saja yang
     benar-benar ada, baca controller untuk endpoint yang benar-benar
     terimplementasi (method + path + validasi apa saja di service-nya)
   - frontend/src/features/ → halaman/fitur yang benar-benar ada
   - ml-service/app/features/ → model & endpoint AI yang aktif, baca
     preprocessing.py & predictor.py untuk detail teknis yang akurat
   - docs/ → baca srs-rs-medika-sentosa.md dan erd-rs-medika-sentosa.md
   - Cek apakah sudah ada Swagger aktif (springdoc), FastAPI /docs, file
     Insomnia/Postman collection
   - Cek apakah ada unit test (kemungkinan besar BELUM ada — jangan klaim ada
     kalau tidak ada)
   - Baca README.md lama di root sebagai referensi, tapi jangan percaya
     mentah-mentah

2. LAPORKAN dulu ringkasan temuan ke saya (modul apa saja yang ada + status,
   endpoint yang terverifikasi, apakah ada testing, dsb). Tunggu konfirmasi
   saya sebelum menulis README final.

3. Setelah saya konfirmasi, tulis README.md dengan struktur & GAYA berikut:

   # RS Medika Sentosa — Hospital Enterprise Web Application

   2-3 kalimat deskripsi: sistem informasi rumah sakit full-stack dengan
   fitur skrining osteoporosis berbasis AI, dibangun sebagai tugas akhir
   (skripsi) sekaligus portofolio untuk magang & CPNS.

   Satu baris highlight teknis yang JUJUR (bukan angka user fiktif), misalnya:
   "Mengintegrasikan tiga layanan independen (React, Spring Boot, FastAPI)
   dengan model deep learning custom (EfficientNetV2) untuk skrining
   osteoporosis tiga kelas, lengkap dengan visualisasi Grad-CAM."

   > Blockquote gaya "Built to explore": jelaskan tujuan belajar di balik
   > project ini — arsitektur package-by-feature, integrasi model AI ke
   > sistem produksi, desain REST API, autentikasi JWT, komunikasi antar
   > layanan.

   ## Daftar Isi

   (link ke semua section di bawah)

   ***

   ## Status Proyek

   Ganti section "Live Demo" ala CodeDuels dengan versi jujur:
   - Status saat ini: dalam pengembangan aktif (Fase 1 / MVP)
   - Dijalankan secara lokal (belum di-deploy ke cloud) — sebutkan alasan
     singkat jika relevan (mis. dikembangkan di laptop CPU-only, fokus dulu
     pada kelengkapan fitur sebelum deployment)
   - Link repository (placeholder jika belum ada)
   - JANGAN cantumkan link demo live jika memang belum ada

   ***

   ## Poin Utama (Key Highlights)

   Bullet list pencapaian teknis yang REAL dari hasil scan, contoh pola
   (isi sesuai temuan aktual, jangan sekadar contoh ini):
   - Pipeline AI end-to-end: upload citra X-ray -> preprocessing ->
     inferensi model -> Grad-CAM -> hasil tersimpan ke database
   - Arsitektur package-by-feature diterapkan konsisten di tiga layanan
     (backend, frontend, ML service)
   - Autentikasi JWT dengan role-based access (Pasien/Dokter/Admin)
   - Dokumentasi API otomatis (Swagger/OpenAPI, FastAPI docs) jika sudah aktif
   - Validasi logika bisnis berlapis (contoh nyata dari kode: cek kuota
     jadwal, validasi hari appointment, dsb — ambil dari service yang
     benar-benar ada)

   ***

   ## Screenshot

   (placeholder path ./assets/screenshots/, beri catatan saya perlu isi manual)

   ***

   ## Evolusi Sistem

   Narasikan perjalanan REAL project ini (bukan fiktif) — mis. dimulai dari
   struktur package-by-layer, lalu di-refactor total ke package-by-feature
   di ketiga layanan begitu jumlah modul bertambah, karena struktur lama
   mulai terasa tidak rapi untuk maintenance & penambahan fitur. Ambil detail
   ini dari histori kerja yang saya ceritakan, konfirmasi dulu jika perlu.

   ***

   ## Arsitektur Sistem

   Diagram alur: React <-> Spring Boot <-> FastAPI <-> PostgreSQL.
   Paragraf jelaskan kenapa dipisah 3 layanan (mis. ML service terpisah agar
   backend utama tidak perlu memuat TensorFlow, model bisa di-scale/diganti
   independen dari business logic).

   ***

   ## Layanan (Services Breakdown)

   Ikuti gaya CodeDuels: tiap layanan dapat sub-section sendiri dengan daftar
   "Tanggung Jawab" (Responsibilities), berdasarkan kode yang BENAR-BENAR ada:

   ### 1. Backend Utama (Spring Boot)

   Tech: Java 21, Spring Boot 3.4.1, Spring Security, Spring Data JPA
   Tanggung Jawab: (isi sesuai scan — REST API, autentikasi JWT, validasi
   bisnis appointment/rekam medis, forward request screening ke FastAPI, dst)

   ### 2. ML Service (FastAPI)

   Tech: Python, FastAPI, TensorFlow
   Tanggung Jawab: (preprocessing citra, inferensi model, generate Grad-CAM,
   dst — sesuai kode aktual di ml-service/)

   ### 3. Frontend (React)

   Tech: React, TypeScript, Vite, Tailwind CSS
   Tanggung Jawab: (autentikasi, alur booking, tampilan hasil skrining, dst)

   ***

   ## Alur Skrining Osteoporosis

   Gaya "Code Submission Flow" — langkah bernomor 1-N yang AKURAT sesuai kode
   (baca ulang service & controller screening untuk memastikan urutan benar):
   1. Pasien upload citra X-ray via halaman React
   2. Frontend kirim request ke Spring Boot backend
   3. Backend validasi & forward citra via multipart ke FastAPI
   4. FastAPI preprocessing (sebutkan step nyata: median filter, CLAHE,
      resize, normalisasi — ambil dari preprocessing.py yang sebenarnya)
   5. Model EfficientNetV2 melakukan inferensi 3 kelas
   6. Grad-CAM + Otsu masking menghasilkan visualisasi area yang relevan
   7. FastAPI kembalikan hasil ke backend
   8. Backend simpan hasil ke PostgreSQL
   9. Response dikirim ke frontend, ditampilkan lengkap dengan grafik
      confidence & overlay Grad-CAM

   > Blockquote "Alasan Desain": jelaskan kenapa ML service dipisah dari
   > backend utama (isolasi dependency berat seperti TensorFlow, kemudahan
   > scaling/replace model tanpa sentuh business logic).

   ***

   ## Dokumentasi API

   Sebutkan Swagger UI (/swagger-ui.html) & FastAPI docs (/docs) JIKA sudah
   aktif dari hasil scan. Sebutkan juga file Insomnia collection jika ada
   di project.

   ***

   ## Pengujian

   JUJUR: jika belum ada automated test, katakan terus terang — misalnya
   "Pengujian otomatis (JUnit, Mockito) direncanakan pada Fase 3 roadmap;
   saat ini verifikasi dilakukan manual melalui Insomnia/Postman untuk
   setiap endpoint." JANGAN mengarang jumlah test class seperti contoh
   CodeDuels jika memang belum ada.

   ***

   ## Fitur Utama

   Kelompokkan per peran (Pasien/Dokter/Admin/Sistem), HANYA fitur yang
   benar-benar terimplementasi dari hasil scan Langkah 1.

   ***

   ## Keamanan

   Jelaskan yang REAL: JWT stateless authentication, BCrypt password hashing,
   role-based access control, CORS configuration. Jika ada hal yang BELUM
   diimplementasi tapi direncanakan (mis. rate limiting, refresh token),
   sebutkan dengan catatan jujur gaya CodeDuels soal 2FA — jangan sembunyikan,
   tapi jangan juga menjanjikan sebagai fitur aktif.

   ***

   ## Performa & Reliabilitas

   Sebutkan hal REAL dari kode, misalnya: model dimuat sekali saat startup
   (singleton pattern) untuk menghindari overhead re-load tiap request;
   proses warm-up model di awal agar request pertama tidak lambat (jika ini
   memang diimplementasikan — cek app/core/model_loader.py).

   ***

   ## Tech Stack

   Tabel kategori | teknologi, untuk backend/frontend/ML/database. Pisahkan
   jelas "Digunakan Saat Ini" vs "Roadmap Teknologi (belum diimplementasi)".

   ***

   ## Struktur Project

   Tree folder ASLI hasil scan (package-by-feature), bukan template generik.

   ***

   ## Roadmap Pengembangan

   ATURAN KHUSUS (tetap berlaku): Fase 1/MVP mendapat checklist LENGKAP &
   granular per Tahap 1-6 sesuai kondisi aktual (jangan tandai selesai kalau
   belum). Fase 2 & Fase 3 cukup RINGKAS — 1 paragraf + bullet nama fitur
   saja, TANPA checklist detail, dengan catatan eksplisit ini rencana ke
   depan yang belum dikerjakan (karena Fase 1 sendiri belum tuntas, jangan
   sampai README terkesan menjanjikan lebih dari kondisi aktual).

   ***

   ## Menjalankan Secara Lokal

   Prasyarat (Java 21, Node.js, Python, PostgreSQL, WSL Ubuntu jika relevan),
   cara clone, environment variable yang dibutuhkan tiap layanan (application
   .properties untuk Spring, .env untuk FastAPI/React) — TANPA menampilkan
   value rahasia asli, hanya nama variabelnya.

   ***

   ## Lisensi & Kredit

   Nama pembuat, tujuan (tugas akhir/skripsi + portofolio magang/CPNS).

ATURAN PALING PENTING:

- ADAPTASI GAYA (naratif, detail, breakdown per service, alur bernomor,
  blockquote design rationale, catatan jujur soal keterbatasan) — JANGAN
  ADAPTASI KONTEN CodeDuels. Tidak ada AWS, tidak ada Terraform, tidak ada
  angka user, tidak ada klaim testing yang tidak nyata di project ini.
- Setiap klaim teknis HARUS bisa ditelusuri balik ke kode asli hasil scan.
  Kalau ragu suatu fitur/detail benar-benar ada, TANYAKAN ke saya dulu,
  jangan menebak atau mengarang supaya README "terlihat lengkap".
- Porsi Fase 1 vs Fase 2/3 tetap timpang sengaja (lihat section Roadmap).
- Simpan README.md di root Portofolio/. Tanyakan dulu apakah saya mau review
  draft sebelum menimpa file lama.

Setelah selesai, tampilkan ringkasan section apa saja yang berubah signifikan
dibanding versi lama, dan sebutkan bagian mana (jika ada) yang perlu saya isi
manual (screenshot, link repository, dsb).
