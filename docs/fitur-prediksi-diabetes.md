# Fitur AI Kedua — Prediksi Risiko Diabetes (Blueprint)

Status: Fase 2 (dikerjakan setelah MVP + chatbot Senta).
Tujuan: fitur AI tabular yang ringan (cocok CPU-only), nyambung ke domain RS.

Sudah ditambahkan ke:
- SRS: section 3.7, FR-38 s/d FR-42 (Fase 2)
- ERD: tabel risk_assessment (Fase 2, Kelompok Penunjang Medis)

---

## 1. Kenapa Fitur Ini Cocok

```
✅ Domain kesehatan (nyambung RS, beda dengan credit fraud)
✅ Model RINGAN — Random Forest/XGBoost, latih & inferensi cepat di CPU
✅ Dataset publik bersih & standar (Pima Indians Diabetes)
✅ Extend keahlian ML kamu ke ranah TABULAR (selain CV/X-ray)
✅ Cerita jelas: pasien skrining risiko → rujuk poli terkait
✅ Melengkapi portfolio: CV (osteoporosis) + Tabular (diabetes) + LLM (chatbot)
```

## 2. Dataset

```
Pima Indians Diabetes Dataset
- 768 baris, 8 fitur + 1 target (Outcome: 0/1)
- Fitur: Pregnancies, Glucose, BloodPressure, SkinThickness,
         Insulin, BMI, DiabetesPedigreeFunction, Age
- Tersedia publik (Kaggle / UCI)
- Ringan, cocok untuk latih di laptop CPU
```

## 3. Model

```
Pilihan (semua ringan di CPU):
1. Random Forest    → mudah, interpretable, cepat  ← saran
2. XGBoost          → akurasi lebih tinggi, sedikit lebih berat
3. Logistic Regression → paling ringan, baseline

Preprocessing:
- Handle missing values (0 pada Glucose/BMI = anggap missing → imputasi median)
- Scaling (StandardScaler) untuk LR; RF/XGB tidak wajib scaling
- Train/test split 80/20

Output:
- Prediksi: RENDAH / TINGGI
- Probabilitas: skor 0-1 (predict_proba)
- Feature importance → tampilkan faktor risiko utama
```

## 4. Struktur di ML Service (package-by-feature)

Tambah fitur baru di ML service TANPA ganggu osteoporosis:

```
ml-service/app/
├── core/
│   ├── config.py          (+ path model diabetes)
│   └── model_loader.py    (+ load model diabetes)
└── features/
    ├── osteoporosis/      (sudah ada)
    └── diabetes/          ← BARU
        ├── router.py       → POST /predict/diabetes
        ├── predictor.py    → load model .pkl, predict + proba
        ├── preprocessing.py→ imputasi, scaling, susun fitur
        └── schema.py       → Pydantic request & response
```

Model disimpan: `ml-service/models/diabetes_rf.pkl` (joblib).

## 5. Struktur di Backend (Spring Boot)

```
backend/.../features/riskassessment/
├── controller/   → RiskAssessmentController (POST /api/risk/diabetes)
├── dto/          → RiskRequest (8 fitur), RiskResponse (hasil + proba + rekomendasi)
├── entity/       → RiskAssessment (tabel risk_assessment)
├── repository/   → RiskAssessmentRepository
└── service/      → RiskAssessmentService
                    (forward ke FastAPI /predict/diabetes, simpan hasil, kembalikan)
```

Pola SAMA dengan modul screening: Spring terima input → forward ke FastAPI →
simpan hasil ke DB → kembalikan ke frontend.

## 6. Struktur di Frontend (React)

```
frontend/src/features/diabetes/
├── pages/       → RiskScreening.tsx (form input 8 fitur)
├── components/  → RiskResultCard.tsx (tampilkan risiko + faktor)
├── services/    → diabetesService.ts
└── types.ts     → RiskResult
```

## 7. Alur End-to-End

```
1. Pasien isi form (umur, glukosa, BMI, dll) di React
2. POST /api/risk/diabetes ke Spring Boot
3. Spring forward ke FastAPI POST /predict/diabetes
4. FastAPI: preprocess → model.predict_proba → hasil
5. Spring simpan ke tabel risk_assessment, kembalikan response
6. React tampilkan: RENDAH/TINGGI + probabilitas + rekomendasi
   (mis. "Risiko tinggi — disarankan konsultasi ke Poli Penyakit Dalam")
```

## 8. Nilai Jual Portfolio

```
Dengan fitur ini, portfolio kamu punya 3 jenis AI:
- Computer Vision  → skrining osteoporosis (X-ray)
- Tabular ML       → prediksi risiko diabetes  ← INI
- LLM/NLP          → chatbot Senta

Menunjukkan kamu paham berbagai paradigma ML, bukan cuma satu.
Ini poin kuat untuk recruiter (AI/full-stack).
```

## 9. Estimasi Effort

```
Latih model (notebook)    : 2-3 jam (dataset kecil, model ringan)
ML service integrasi      : setengah hari (pola sama dgn osteoporosis)
Backend modul             : setengah hari (pola sama dgn screening)
Frontend form + hasil     : setengah hari
Total                     : ~2 hari kerja santai
```

## 10. Catatan Etis (penting untuk RS)

```
⚠️ Tambahkan disclaimer di UI:
   "Hasil ini adalah skrining awal berbasis AI, BUKAN diagnosa medis.
    Konsultasikan dengan dokter untuk penilaian menyeluruh."

Ini praktik baik & realistis — model prediksi risiko adalah alat bantu,
bukan pengganti dokter. Recruiter menghargai kesadaran etis ini.
```

---

## Kapan Dikerjakan

```
Sekarang → MVP dulu (Tahap 3-6)
Fase 2   → chatbot Senta, lalu fitur diabetes ini
```

Jangan kerjakan sekarang — ini sudah tercatat di SRS & ERD sebagai Fase 2.
Saat waktunya tiba, blueprint ini jadi panduan lengkap.
