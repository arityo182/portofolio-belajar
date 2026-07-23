# Software Requirements Specification (SRS)
# RS Medika Sentosa — Hospital Enterprise Web Application

Dokumen ini menjelaskan kebutuhan perangkat lunak aplikasi web enterprise
rumah sakit RS Medika Sentosa. Disusun sebagai bagian dari portfolio
pengembangan perangkat lunak full-stack.

> **Standar acuan:** Struktur dokumen ini mengacu pada **ISO/IEC/IEEE 29148:2018**
> (standar internasional terkini untuk requirements engineering yang menggantikan
> IEEE 830-1998), dengan tetap mempertahankan tata letak klasik IEEE 830 yang umum
> dikenal (Pendahuluan → Deskripsi Umum → Kebutuhan Spesifik).

---

## 1. PENDAHULUAN

### 1.1 Tujuan
Dokumen ini mendefinisikan kebutuhan fungsional dan non-fungsional dari
aplikasi RS Medika Sentosa, sebuah portal rumah sakit berbasis web yang
mengintegrasikan layanan administratif dengan skrining medis berbasis
kecerdasan buatan (AI).

### 1.2 Ruang Lingkup
RS Medika Sentosa adalah aplikasi web yang memungkinkan pasien melakukan
pendaftaran, membuat janji temu, dan memperoleh skrining awal osteoporosis
dari citra X-ray. Dokter dapat mengelola pemeriksaan dan rekam medis, sedangkan
admin mengelola data master rumah sakit.

Fitur unggulan sistem adalah skrining osteoporosis otomatis menggunakan model
deep learning (EfficientNetV2) yang memberikan prediksi tiga kelas
(Normal, Osteopenia, Osteoporosis) beserta visualisasi Grad-CAM.

Selain skrining, sistem menyediakan asisten kesehatan berbasis AI bernama
**Senta**, yaitu chatbot yang ditenagai model bahasa besar (Qwen) melalui
vLLM, untuk menjawab pertanyaan umum seputar layanan dan informasi kesehatan.

Pengembangan dilakukan bertahap: tahap MVP mencakup autentikasi, pendaftaran,
appointment, rekam medis, dan skrining AI. Tahap berikutnya menambah penunjang
medis (laboratorium, radiologi, rawat inap, operasi), chatbot Senta, dan modul
keuangan.

### 1.3 Definisi & Istilah
| Istilah | Keterangan |
|---|---|
| RM | Rekam Medis |
| Grad-CAM | Gradient-weighted Class Activation Mapping (visualisasi area acuan model) |
| JWT | JSON Web Token (mekanisme autentikasi) |
| MVP | Minimum Viable Product (versi inti awal) |
| Osteopenia | Kondisi penurunan kepadatan tulang tahap awal |
| Appointment | Janji temu pasien dengan dokter |
| Senta | Nama chatbot asisten kesehatan berbasis AI pada sistem |
| Qwen | Model bahasa besar (LLM) yang menenagai chatbot Senta |
| vLLM | Mesin inferensi LLM berkinerja tinggi untuk menjalankan Qwen |
| Redis | Penyimpanan data in-memory untuk caching dan manajemen state |
| WebSocket | Protokol komunikasi dua arah real-time antara klien dan server |
| Docker | Platform containerization untuk deployment yang portabel |
| Prometheus | Sistem pengumpulan metrik untuk observability |
| Grafana | Platform visualisasi dashboard metrik |
| CI/CD | Continuous Integration / Continuous Deployment (otomasi build & deploy) |

### 1.4 Referensi
- Dataset X-ray osteoporosis (sumber publik)
- Dokumentasi Spring Boot, FastAPI, React
- ERD RS Medika Sentosa (dokumen terpisah: erd-rs-medika-sentosa.md)

### 1.5 Gambaran Umum Dokumen
Bagian 2 menjelaskan deskripsi umum sistem. Bagian 3 memuat kebutuhan
fungsional. Bagian 4 memuat kebutuhan non-fungsional.

---

## 2. DESKRIPSI UMUM

### 2.1 Perspektif Produk
RS Medika Sentosa adalah sistem mandiri berbasis arsitektur tiga layanan:
- **Frontend** (React + TypeScript) — antarmuka pengguna
- **Backend** (Spring Boot) — logika bisnis, autentikasi, REST API
- **ML Service** (FastAPI + TensorFlow) — inferensi model osteoporosis

Ketiganya berkomunikasi melalui REST API. Data disimpan pada basis data
PostgreSQL.

```
React (UI) → Spring Boot (API + Auth) → FastAPI (Model AI)
                      ↓
                 PostgreSQL
```

Sistem juga dirancang untuk mendukung lapisan infrastruktur pendukung yang
ditambahkan secara bertahap: **Redis** untuk caching dan manajemen state,
**WebSocket** untuk notifikasi real-time, **Docker** untuk containerization,
serta **Prometheus + Grafana** untuk observability. Kualitas kode dijaga
melalui **pengujian otomatis** (JUnit, Mockito) dan **CI/CD** (GitHub Actions).

### 2.2 Fungsi Utama Produk
- Registrasi dan autentikasi pengguna berbasis peran
- Pendaftaran pasien dan manajemen profil
- Pembuatan janji temu dengan dokter beserta nomor antrian
- Pencatatan rekam medis dan resep oleh dokter
- Skrining osteoporosis otomatis dari citra X-ray dengan Grad-CAM
- Asisten kesehatan berbasis AI (chatbot Senta) untuk tanya jawab
- Pengelolaan data master (poli, dokter, obat) oleh admin

### 2.3 Karakteristik Pengguna
| Pengguna | Deskripsi | Hak Akses Utama |
|---|---|---|
| Pasien | Pengguna umum yang berobat | Daftar, booking, lihat hasil skrining |
| Dokter | Tenaga medis | Kelola pemeriksaan, tulis rekam medis & resep |
| Admin | Pengelola sistem | Kelola data master, monitor sistem |

### 2.4 Batasan
- Aplikasi berjalan pada peramban web modern (Chrome, Firefox, Edge)
- Model AI berjalan pada CPU (tanpa akselerasi GPU)
- Integrasi BPJS ditunda; pembayaran menggunakan metode umum
- Integrasi SAP dan sistem eksternal bersifat mock (simulasi)
- Teknologi pendukung (Redis, WebSocket, Docker, Prometheus, Grafana, CI/CD,
  automated testing) diterapkan bertahap pada fase lanjutan, dirancang agar
  dapat dijalankan pada lingkungan lokal tanpa ketergantungan layanan cloud berbayar

### 2.5 Asumsi & Ketergantungan
- Pengguna memiliki koneksi internet yang stabil
- Citra X-ray yang diunggah berformat JPG atau PNG, maksimal 10MB
- Model AI telah dilatih sebelumnya dan tersedia sebagai file .keras
- Ketiga layanan (frontend, backend, ML) berjalan bersamaan

---

## 3. KEBUTUHAN FUNGSIONAL

Kode: FR = Functional Requirement. Prioritas: [MVP] / [Fase 2] / [Fase 3].

### 3.1 Autentikasi & Pengguna
| Kode | Kebutuhan | Prioritas |
|---|---|---|
| FR-01 | Pengguna dapat mendaftar akun dengan nama, email, dan password | MVP |
| FR-02 | Pengguna dapat login menggunakan email dan password | MVP |
| FR-03 | Sistem memberikan token JWT setelah login berhasil | MVP |
| FR-04 | Sistem membatasi akses fitur berdasarkan peran (pasien/dokter/admin) | MVP |
| FR-05 | Pengguna dapat logout dari sistem | MVP |

### 3.2 Pendaftaran & Profil Pasien
| Kode | Kebutuhan | Prioritas |
|---|---|---|
| FR-06 | Pasien dapat melengkapi data profil (NIK, tanggal lahir, alamat, dll) | MVP |
| FR-07 | Sistem menghasilkan nomor rekam medis unik untuk setiap pasien | MVP |

### 3.3 Appointment & Antrian
| Kode | Kebutuhan | Prioritas |
|---|---|---|
| FR-08 | Pasien dapat melihat jadwal praktik dokter per poli | MVP |
| FR-09 | Pasien dapat membuat janji temu dengan dokter pada jadwal tersedia | MVP |
| FR-10 | Sistem menghasilkan nomor antrian untuk setiap janji temu | MVP |
| FR-11 | Pasien dapat melihat dan membatalkan janji temu | MVP |
| FR-12 | Sistem memvalidasi kuota dokter agar tidak melebihi batas | MVP |
| FR-12a | Sistem mengirim notifikasi status antrian secara real-time (WebSocket) | Fase 2 |

### 3.4 Rekam Medis & Resep
| Kode | Kebutuhan | Prioritas |
|---|---|---|
| FR-13 | Dokter dapat mencatat rekam medis (anamnesa, diagnosa, tindakan) | MVP |
| FR-14 | Dokter dapat menuliskan resep obat pada rekam medis | MVP |
| FR-15 | Pasien dapat melihat riwayat rekam medisnya | MVP |

### 3.5 Skrining Osteoporosis (Fitur Unggulan)
| Kode | Kebutuhan | Prioritas |
|---|---|---|
| FR-16 | Pasien dapat mengunggah citra X-ray (JPG/PNG, maks 10MB) | MVP |
| FR-17 | Sistem memprediksi kelas osteoporosis (Normal/Osteopenia/Osteoporosis) | MVP |
| FR-18 | Sistem menampilkan tingkat keyakinan (confidence) prediksi | MVP |
| FR-19 | Sistem menghasilkan visualisasi Grad-CAM area tulang acuan | MVP |
| FR-20 | Sistem menyimpan riwayat skrining setiap pasien | MVP |
| FR-21 | Sistem menampilkan pesan risiko sesuai hasil prediksi | MVP |

### 3.6 Chatbot Asisten Kesehatan (Senta)
| Kode | Kebutuhan | Prioritas |
|---|---|---|
| FR-22 | Pasien dapat mengajukan pertanyaan kesehatan umum ke chatbot Senta | Fase 2 |
| FR-23 | Sistem menjawab menggunakan model bahasa besar (Qwen via vLLM) | Fase 2 |
| FR-24 | Sistem menyimpan riwayat percakapan per sesi | Fase 2 |
| FR-25 | Pasien dapat melihat kembali riwayat sesi chat sebelumnya | Fase 2 |
| FR-26 | Chatbot memberi arahan untuk konsultasi dokter pada kondisi serius | Fase 2 |

### 3.7 Prediksi Risiko Diabetes (Fitur AI Tabular)
| Kode | Kebutuhan | Prioritas |
|---|---|---|
| FR-38 | Pasien/dokter dapat menginput data kesehatan (umur, tekanan darah, kadar glukosa, BMI, jumlah kehamilan, ketebalan kulit, insulin, diabetes pedigree) | Fase 2 |
| FR-39 | Sistem memprediksi tingkat risiko diabetes (rendah/tinggi) menggunakan model machine learning tabular | Fase 2 |
| FR-40 | Sistem menampilkan probabilitas/skor risiko beserta faktor yang berkontribusi | Fase 2 |
| FR-41 | Sistem memberikan rekomendasi tindak lanjut (mis. rujuk ke poli penyakit dalam, kontrol gula darah) | Fase 2 |
| FR-42 | Sistem menyimpan riwayat penilaian risiko setiap pasien | Fase 2 |

### 3.8 Penunjang Medis
| Kode | Kebutuhan | Prioritas |
|---|---|---|
| FR-27 | Dokter dapat membuat order pemeriksaan laboratorium | Fase 2 |
| FR-28 | Sistem mencatat hasil laboratorium | Fase 2 |
| FR-29 | Dokter dapat membuat order radiologi | Fase 2 |
| FR-30 | Sistem mengelola data rawat inap dan kamar | Fase 2 |
| FR-31 | Sistem mengelola penjadwalan operasi | Fase 2 |

### 3.9 Keuangan
| Kode | Kebutuhan | Prioritas |
|---|---|---|
| FR-32 | Sistem menghasilkan tagihan (billing) dari layanan yang digunakan | Fase 3 |
| FR-33 | Pasien dapat melakukan pembayaran (tunai/transfer/kartu) | Fase 3 |
| FR-34 | Sistem mencatat status pembayaran (pending/berhasil/gagal) | Fase 3 |

### 3.10 Manajemen (Admin)
| Kode | Kebutuhan | Prioritas |
|---|---|---|
| FR-35 | Admin dapat mengelola data poli | MVP |
| FR-36 | Admin dapat mengelola data dokter dan jadwal praktik | MVP |
| FR-37 | Admin dapat mengelola data master obat | Fase 2 |

---

## 4. KEBUTUHAN NON-FUNGSIONAL

Kode: NFR = Non-Functional Requirement.

### 4.1 Performa
| Kode | Kebutuhan |
|---|---|
| NFR-01 | Prediksi osteoporosis (setelah warm-up) selesai dalam waktu wajar pada CPU |
| NFR-02 | Response API backend untuk operasi umum di bawah 1 detik |
| NFR-03 | Sistem melakukan warm-up model saat startup agar request pertama tidak lambat |
| NFR-04 | Chatbot Senta memberi respons awal dalam waktu wajar (streaming token diperbolehkan) |

### 4.2 Keamanan
| Kode | Kebutuhan |
|---|---|
| NFR-05 | Password disimpan dalam bentuk hash (BCrypt), tidak pernah plain text |
| NFR-06 | Autentikasi menggunakan JWT dengan masa berlaku token |
| NFR-07 | Endpoint sensitif hanya dapat diakses pengguna terautentikasi |
| NFR-08 | Akses dibatasi sesuai peran pengguna (role-based access control) |
| NFR-09 | Komunikasi antar layanan diatur melalui CORS yang terkonfigurasi |

### 4.3 Usability
| Kode | Kebutuhan |
|---|---|
| NFR-10 | Antarmuka responsif pada perangkat desktop dan mobile |
| NFR-11 | Sistem menampilkan indikator loading saat proses berlangsung |
| NFR-12 | Sistem menampilkan pesan error yang jelas dan informatif |
| NFR-13 | Alur pendaftaran hingga skrining dapat diselesaikan tanpa panduan teknis |

### 4.4 Reliability & Maintainability
| Kode | Kebutuhan |
|---|---|
| NFR-14 | Sistem menangani error secara global dan mengembalikan respons terstruktur |
| NFR-15 | Kode terstruktur berlapis (controller, service, repository) agar mudah dirawat |
| NFR-16 | Kesalahan input pengguna divalidasi sebelum diproses |

### 4.5 Kompatibilitas & Portabilitas
| Kode | Kebutuhan |
|---|---|
| NFR-17 | Aplikasi berjalan pada peramban modern (Chrome, Firefox, Edge) |
| NFR-18 | Layanan dapat dijalankan secara lokal maupun dalam kontainer (Docker) |
| NFR-19 | Basis data menggunakan PostgreSQL yang portabel antar lingkungan |

### 4.6 Skalabilitas & Performa Data (Fase Lanjutan)
| Kode | Kebutuhan |
|---|---|
| NFR-20 | Sistem menggunakan Redis untuk cache data yang sering diakses (mis. jadwal dokter) guna mengurangi beban database |
| NFR-21 | Sistem mendukung komunikasi real-time via WebSocket untuk notifikasi antrian |

### 4.7 Observability (Fase Lanjutan)
| Kode | Kebutuhan |
|---|---|
| NFR-22 | Sistem mengekspos metrik aplikasi melalui Spring Boot Actuator |
| NFR-23 | Metrik dikumpulkan oleh Prometheus (latensi, jumlah request, error rate) |
| NFR-24 | Metrik divisualisasikan dalam dashboard Grafana |

### 4.8 Deployment & Kualitas Kode (Fase Lanjutan)
| Kode | Kebutuhan |
|---|---|
| NFR-25 | Seluruh layanan dikemas menggunakan Docker dan diorkestrasi via docker-compose |
| NFR-26 | Proses build dan test dijalankan otomatis melalui CI/CD (GitHub Actions) |
| NFR-27 | Backend memiliki unit test dan integration test (JUnit 5, Mockito, Spring Boot Test) |
| NFR-28 | Cakupan pengujian mencakup layer service, repository, dan controller |

---

## Lampiran
- **ERD**: lihat dokumen `erd-rs-medika-sentosa.md` (termasuk tabel chat_session & chat_message untuk chatbot Senta)
- **Diagram Alur Data**: lihat `4-alur-enterprise.mermaid` dan `3-alur-mvp.mermaid`
- **Standar acuan**: ISO/IEC/IEEE 29148:2018 (menggantikan IEEE 830-1998)
