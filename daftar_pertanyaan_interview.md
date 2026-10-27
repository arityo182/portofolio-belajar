# 🎯 Daftar Pertanyaan Wawancara Teknis — RS Medika Sentosa

**Disusun berdasarkan audit kode aktual pada:**
- `Portofolio/` — Sistem Informasi RS Medika Sentosa
- `Skripsi/` — Eksperimen EfficientNetV2 vs ConvNeXt untuk Skrining Osteoporosis

**Pewawancara:** Senior Software Engineer / Tech Lead
**Kandidat:** Ari
**Tanggal:** 3 September 2026

---

## Bagian A: Machine Learning & AI (Skripsi)

### Pertanyaan 1 — Inkonsistensi Preprocessing (Training-Serving Skew)

**Konteks:**
Di notebook training (`Skripsi/Fine Tuning/EfficientNetV2-S.ipynb`, Cell 3), CLAHE dibuat dengan:
```python
clahe = cv2.createCLAHE(clipLimit=3.0, tileGridSize=(8, 8))
```
Tapi di production service (`ml-service/app/preprocessing.py`, baris 35), kamu pakai:
```python
clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
```

**Pertanyaan:**
Model kamu dilatih dengan distribusi piksel hasil CLAHE `clipLimit=3.0`, tapi saat inferensi production menerima distribusi berbeda dari `clipLimit=2.0`. Apa dampak teknis dari inkonsistensi ini terhadap akurasi model di production, dan bagaimana seharusnya kamu menangani masalah training-serving skew seperti ini?

---

### Pertanyaan 2 — Overfitting yang Terlihat Jelas

**Konteks:**
Dari data training EfficientNetV2 Fine-Tuning (`epoch_table_EfficientNetV2S_FineTuning.csv`):

| Epoch | Train Acc | Val Acc  | Train Loss | Val Loss |
|-------|-----------|----------|------------|----------|
| 4     | 93.64%    | 88.67%   | 0.1600     | 0.3267   |
| 14    | 99.39%    | 89.18%   | 0.0187     | 0.7515   |
| 24    | 99.80%    | 87.90%   | 0.0075     | 0.8968   |

Train accuracy naik terus ke 99.8%, tapi val accuracy stagnan di ~89% dan val loss justru **melonjak dari 0.32 ke 0.89**.

**Pertanyaan:**
Jelaskan apa yang terjadi di sini secara teknis. Model checkpoint kamu disimpan dengan `monitor='val_accuracy', save_best_only=True` — di epoch berapa model terbaik kemungkinan tersimpan? Dan langkah konkret apa yang bisa kamu lakukan untuk mengurangi gap overfitting ini?

---

### Pertanyaan 3 — Pemilihan Model untuk Production

**Konteks:**
Dari classification report di notebook:

**EfficientNetV2 Fine-Tuning (Test Acc: 90.5%)**
| Kelas        | Precision | Recall | F1    |
|--------------|-----------|--------|-------|
| Normal       | 0.91      | 0.96   | 0.93  |
| Osteopenia   | 0.85      | 0.85   | 0.85  |
| Osteoporosis | 0.93      | 0.87   | 0.90  |

**ConvNeXt Fine-Tuning (Test Acc: 92%)**
| Kelas        | Precision | Recall | F1    |
|--------------|-----------|--------|-------|
| Normal       | 0.92      | 0.94   | 0.93  |
| Osteopenia   | 0.94      | 0.82   | 0.88  |
| Osteoporosis | 0.90      | 0.94   | 0.92  |

ConvNeXt punya test accuracy lebih tinggi (92% vs 90.5%) dan recall Osteoporosis lebih baik (94% vs 87%), tapi yang di-deploy di production (`ml-service/models/`) adalah EfficientNetV2.

**Pertanyaan:**
Dalam konteks skrining medis osteoporosis, metrik mana yang lebih penting — precision atau recall, dan untuk kelas mana? Dengan pertimbangan itu, justifikasi kenapa kamu memilih EfficientNetV2 daripada ConvNeXt untuk production, padahal ConvNeXt unggul di recall Osteoporosis?

---

### Pertanyaan 4 — Grad-CAM: Validitas dan Keterbatasan

**Konteks:**
Di `ml-service/app/gradcam.py` (baris 118-164), kamu menggunakan Grad-CAM standar (bukan Grad-CAM++), mengekstrak gradient dari layer `top_activation`:
```python
GRADCAM_LAYER = "top_activation"  # di main.py, baris 20
```
Lalu di `apply_otsu_mask()` (baris 166-191), aktivasi < 0.4 dibuang dan heatmap dimask ke area tulang saja.

**Pertanyaan:**
- Mengapa kamu memilih layer `top_activation` dibanding layer konvolusi lebih awal (misalnya di blok 4 atau 5)?
- Threshold hardcoded `0.4` — bagaimana kamu menentukan angka ini? Apakah ada eksperimen atau apakah ini arbitrary?
- Sebagai seorang engineer, apa keterbatasan utama Grad-CAM sebagai metode explainability? Apakah "area yang di-highlight" berarti model benar-benar "melihat" fitur osteoporosis di sana?

---

### Pertanyaan 5 — Augmentasi dan Strategi Splitting Data

**Konteks:**
Di notebook training (Cell 7-8):
```python
# Augmentasi 3x data asli
load_dataset_with_augmentation(data_dir, img_size=(224,224), augment_factor=3)
# Output: Total data setelah augmentasi: 5841

# Split 70/20/10
X_train, X_temp, y_train, y_temp = train_test_split(images, labels, test_size=0.3, random_state=42)
X_val, X_test, y_val, y_test = train_test_split(X_temp, y_temp, test_size=0.33, random_state=42)
# Output: Train: 4088 | Val: 1174 | Test: 579
```

**Pertanyaan:**
Augmentasi dilakukan **sebelum** splitting data. Apakah ada masalah dengan urutan ini? Pikirkan: kalau satu gambar asli di-augmentasi 3x, dan hasil augmentasinya tersebar ke train DAN validation/test, apa yang terjadi pada evaluasi model kamu?

---

## Bagian B: Backend (Spring Boot)

### Pertanyaan 6 — Race Condition pada Booking Appointment

**Konteks:**
Di `AppointmentService.java` (baris 120-125), validasi kuota dilakukan dengan:
```java
// 6. Kuota belum penuh
long terisi = appointmentRepository.countByJadwalAndTanggal(jadwal, request.tanggal());
if (terisi >= jadwal.getKuota()) {
    throw new IllegalArgumentException("Kuota jadwal pada tanggal ini sudah penuh");
}
```

**Pertanyaan:**
Bayangkan 2 request booking masuk **bersamaan** untuk jadwal yang sama, dan kuota tersisa 1. Kedua thread membaca `countByJadwalAndTanggal` = 9 (kuota 10), keduanya lolos validasi, keduanya `save()`. Sekarang ada 11 appointment untuk kuota 10. Bagaimana kamu mengatasi race condition ini di Spring Boot + PostgreSQL? Sebutkan minimal 2 pendekatan.

---

### Pertanyaan 7 — Kelemahan JWT: Akun Dinonaktifkan tapi Token Masih Valid

**Konteks:**
Di `AuthService.java` (baris 95-98), login memvalidasi `user.getIsActive()`:
```java
if (Boolean.FALSE.equals(user.getIsActive())) {
    throw new ResourceNotFoundException("Akun Anda dinonaktifkan. Hubungi admin.");
}
```
Tapi di `JwtFilter.java` (baris 28-64), saat memvalidasi token untuk request berikutnya, **tidak ada pengecekan `isActive`** — hanya validasi token valid dan email match:
```java
if (jwtService.isTokenValid(token, userDetails)) {
    // langsung set authentication — tanpa cek isActive
    UsernamePasswordAuthenticationToken authToken = ...
    SecurityContextHolder.getContext().setAuthentication(authToken);
}
```

**Pertanyaan:**
Seorang admin menonaktifkan akun user nakal. Tapi user itu masih punya JWT valid (belum expired, 24 jam). Apa yang terjadi? Bagaimana cara memperbaikinya? Dan apakah ada trade-off antara security dan performance di solusimu?

---

### Pertanyaan 8 — Generate Nomor Rekam Medis: Race Condition

**Konteks:**
Di `PasienService.java` (baris 153-162):
```java
private String generateNoRekamMedis() {
    int year = LocalDate.now().getYear();
    long seq = pasienRepository.count() + 1;
    String candidate = String.format("RM-%d-%06d", year, seq);
    while (pasienRepository.existsByNoRekamMedis(candidate)) {
        seq++;
        candidate = String.format("RM-%d-%06d", year, seq);
    }
    return candidate;
}
```

**Pertanyaan:**
Mekanisme ini menggunakan `count() + 1` sebagai starting sequence lalu loop cek `existsByNoRekamMedis`. Apa masalah concurrency yang bisa terjadi jika 2 pasien mendaftar bersamaan? Dan apa solusi yang lebih robust — sebutkan setidaknya 2 alternatif.

---

### Pertanyaan 9 — Login Error: Penggunaan Exception yang Salah

**Konteks:**
Di `AuthService.java` (baris 83-89):
```java
User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new ResourceNotFoundException("Email atau password salah!"));

if (!passwordEncoder.matches(request.password(), user.getPassword())) {
    throw new ResourceNotFoundException("Email atau password salah");
}
```

**Pertanyaan:**
Kamu melempar `ResourceNotFoundException` (yang di-handle jadi HTTP 404) untuk kasus login gagal. Apakah ini tepat secara semantik? HTTP 404 berarti "resource tidak ditemukan" — apakah itu benar untuk kasus wrong credentials? Dari sisi keamanan, ada satu hal yang kamu lakukan dengan **benar** di sini meskipun exception-nya salah — apa itu?

---

### Pertanyaan 10 — Soft Delete dan Konsekuensi Query

**Konteks:**
Poli, Dokter, dan Jadwal menggunakan pola soft delete dengan field `isActive`. Tapi di beberapa query (seperti `GET /api/poli` di `PoliController`), data dikembalikan tanpa filter `isActive`.

**Pertanyaan:**
Apa risiko jika endpoint publik `GET /api/poli` mengembalikan poli yang sudah di-soft-delete (`isActive=false`)? Bagaimana dampaknya ke alur booking pasien di frontend? Dan bagaimana cara menanganinya tanpa mengubah endpoint yang sudah ada?

---

## Bagian C: Frontend (React + JavaScript)

### Pertanyaan 11 — Keamanan Token di localStorage

**Konteks:**
Di `AuthContext.jsx` (baris 45-94):
```javascript
const [token, setToken] = useState(() => localStorage.getItem("token"));

const login = (newToken, newUser) => {
    localStorage.setItem("token", newToken);
    localStorage.setItem("user", JSON.stringify(newUser));
};
```

**Pertanyaan:**
JWT token disimpan di `localStorage`. Apa kerentanan keamanan utama dari pendekatan ini dibanding `httpOnly cookie`? Dalam konteks aplikasi rumah sakit yang menyimpan data medis sensitif, apakah ini acceptable? Apa alternatifnya dan apa trade-off-nya?

---

### Pertanyaan 12 — Route Guard Bypass

**Konteks:**
`AdminRoute.jsx` mengecek role di frontend:
```javascript
if (user?.role !== "ADMIN") return <Navigate to="/" replace />;
```

**Pertanyaan:**
Seorang user biasa mengubah `localStorage` dan mengganti `role` jadi `"ADMIN"` di browser DevTools. Apa yang terjadi? Apakah mereka bisa mengakses data admin? Jelaskan mengapa frontend route guard saja **tidak cukup** dan bagaimana backend kamu sudah (atau belum) menangani ini.

---

## Bagian D: Arsitektur & DevOps

### Pertanyaan 13 — Mengapa Microservice, Bukan Monolith?

**Konteks:**
Arsitektur kamu memisahkan 4 service: Spring Boot backend, React frontend, ML service (FastAPI), dan Chatbot service (FastAPI). Total 7-8 container di Docker Compose.

**Pertanyaan:**
Untuk skala MVP rumah sakit ini (single server, < 100 concurrent users), apakah arsitektur microservice ini justified? Apa overhead operasional yang kamu tanggung? Kapan monolith + embedded Python (misalnya via GraalPy atau subprocess) lebih tepat? Jawab dengan jujur — bukan jawaban ideal, tapi jawaban realistis.

---

### Pertanyaan 14 — Redis Terpasang tapi Tidak Digunakan

**Konteks:**
Di `docker-compose.yml`, Redis berjalan sebagai service. Tapi di `application.properties` (baris 33):
```properties
spring.cache.type=${SPRING_CACHE_TYPE:simple}
```
Cache type `simple` = in-memory HashMap, bukan Redis.

**Pertanyaan:**
Redis sudah running di production tapi tidak dimanfaatkan — itu berarti membuang resource. Jika kamu benar-benar menggunakan Redis, data apa yang paling tepat untuk di-cache di aplikasi ini? Dan bagaimana strategi cache invalidation-nya?

---

### Pertanyaan 15 — CORS Hardcoded

**Konteks:**
Di `SecurityConfig.java` (baris 65-76):
```java
config.setAllowedOrigins(List.of("http://localhost:5173"));
```

**Pertanyaan:**
Jika kamu deploy ke production (misal `https://medika.example.com`), apa yang terjadi dengan CORS ini? Bagaimana cara membuatnya configurable tanpa rebuild aplikasi?

---

## Tips Belajar

Untuk setiap pertanyaan di atas, siapkan jawaban dengan struktur:

1. **Identifikasi Masalah** — Apa yang salah/kurang dan mengapa itu penting
2. **Dampak Teknis** — Apa konsekuensi nyata jika tidak diperbaiki
3. **Solusi Konkret** — Kode atau pendekatan spesifik untuk memperbaikinya
4. **Trade-off** — Setiap solusi punya kelebihan dan kekurangan

---

> Ketika kamu sudah siap untuk memulai sesi wawancara interaktif, buka chat ini lagi dan bilang **"Saya siap mulai interview"**. Saya akan mengajukan pertanyaan satu per satu, mengevaluasi jawaban kamu, lalu di akhir sesi mengekspor semua ke `hasil_interview.doc`.
