# 📚 Dokumentasi RS Medika Sentosa

Daftar dokumen lengkap project.

---

## Diagram & Desain

| # | File | Keterangan |
|---|---|---|
| 1 | [ERD MVP](1-erd-mvp.mermaid) | Entity Relationship Diagram — Fitur MVP (auth, booking, rekam medis, screening) |
| 2 | [ERD Enterprise](2-erd-enterprise.mermaid) | ERD lengkap — Semua fitur (termasuk lab, radiologi, billing, chatbot) |
| 3 | [Alur Data](3-alur-mvp-enterprise.mermaid) | Diagram alur data dari MVP sampai Enterprise (2 diagram dalam 1 file) |

## Dokumen Teknis

| # | File | Keterangan |
|---|---|---|
| 4 | [SRS](4-srs-rs-medika-sentosa.md) | Software Requirements Specification — ISO/IEC/IEEE 29148:2018 |

## Deployment & DevOps

| # | File | Keterangan |
|---|---|---|
| 5 | [Docker Local](5-docker-local.md) | Cara jalankan seluruh stack di mesin lokal |
| 6 | [Docker AWS](6-docker-aws.md) | Deploy ke AWS EC2 dengan Docker Compose |
| 7 | [CI/CD](7-cicd-github-actions.md) | GitHub Actions — otomasi build, test, deploy |
| 8 | [LocalStack](8-docker-localstack.md) | Simulasi AWS S3 untuk development |

## Blueprint Fitur

| # | File | Keterangan |
|---|---|---|
| 9 | [Prediksi Diabetes](9-fitur-prediksi-diabetes.md) | Blueprint fitur AI kedua — prediksi risiko diabetes dari data tabular |

---

## Struktur Folder

```
docs/
├── README.md                            ← index (file ini)
├── 1-erd-mvp.mermaid                    ← ERD MVP
├── 2-erd-enterprise.mermaid             ← ERD Enterprise
├── 3-alur-mvp-enterprise.mermaid        ← Alur Data (MVP + Enterprise)
├── 4-srs-rs-medika-sentosa.md           ← SRS (ISO/IEEE 29148)
├── 5-docker-local.md                    ← Run Docker lokal
├── 6-docker-aws.md                      ← Deploy AWS EC2
├── 7-cicd-github-actions.md             ← GitHub Actions CI/CD
├── 8-docker-localstack.md               ← LocalStack S3
└── 9-fitur-prediksi-diabetes.md         ← Blueprint Diabetes AI
```
