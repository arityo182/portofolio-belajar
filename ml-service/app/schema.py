"""Skema Pydantic untuk kontrak respons API ML Service osteoporosis.

Modul ini mendefinisikan model respons yang dipakai FastAPI sebagai
`response_model` sehingga dokumentasi Swagger (/docs) menampilkan struktur
JSON yang jelas, tervalidasi, dan konsisten untuk konsumen API
(mis. backend Spring Boot dan frontend React).
"""

from pydantic import BaseModel, Field


class ClassProbabilities(BaseModel):
    """Probabilitas (dalam persen) untuk masing-masing dari 3 kelas.

    Nilai dipakai frontend untuk menggambar bar chart distribusi kelas.

    Attributes:
        Normal: Probabilitas kelas "Normal" dalam persen (0-100).
        Osteopenia: Probabilitas kelas "Osteopenia" dalam persen (0-100).
        Osteoporosis: Probabilitas kelas "Osteoporosis" dalam persen (0-100).
    """

    Normal: float = Field(..., examples=[4.12], description="Probabilitas kelas Normal (%).")
    Osteopenia: float = Field(..., examples=[9.03], description="Probabilitas kelas Osteopenia (%).")
    Osteoporosis: float = Field(..., examples=[86.85], description="Probabilitas kelas Osteoporosis (%).")


class PredictionResponse(BaseModel):
    """Struktur respons untuk endpoint prediksi osteoporosis.

    Merepresentasikan hasil lengkap satu inferensi: label prediksi,
    tingkat keyakinan, distribusi probabilitas 3 kelas, pesan risiko klinis,
    visualisasi Grad-CAM (base64 PNG), dan latensi pemrosesan.

    Attributes:
        label: Label kelas dengan probabilitas tertinggi
            ("Normal" | "Osteopenia" | "Osteoporosis").
        confidence: Keyakinan model terhadap label terpilih, dalam persen (0-100).
        probabilities: Probabilitas seluruh 3 kelas dalam persen.
        risk: Pesan/rekomendasi klinis yang sesuai dengan label.
        gradcamImage: Visualisasi Grad-CAM sebagai data URI PNG base64
            (format: "data:image/png;base64,...").
        latencyMs: Total waktu pemrosesan sisi server dalam milidetik.
    """

    label: str = Field(..., examples=["Osteoporosis"], description="Label kelas prediksi.")
    confidence: float = Field(..., examples=[86.85], description="Keyakinan model terhadap label (%).")
    probabilities: ClassProbabilities = Field(..., description="Probabilitas seluruh 3 kelas (%).")
    risk: str = Field(
        ...,
        examples=["Risiko tinggi — disarankan segera konsultasi dengan dokter."],
        description="Pesan risiko/rekomendasi klinis sesuai label.",
    )
    gradcamImage: str = Field(
        ...,
        examples=["data:image/png;base64,iVBORw0KGgo..."],
        description="Visualisasi Grad-CAM sebagai data URI PNG base64.",
    )
    latencyMs: float = Field(..., examples=[532.14], description="Latensi pemrosesan sisi server (ms).")


class HealthResponse(BaseModel):
    """Struktur respons untuk endpoint health check.

    Attributes:
        status: Status layanan (mis. "UP").
        service: Nama/identitas layanan.
    """

    status: str = Field(..., examples=["UP"], description="Status layanan.")
    service: str = Field(
        ...,
        examples=["ML Service - Osteoporosis"],
        description="Nama layanan.",
    )
