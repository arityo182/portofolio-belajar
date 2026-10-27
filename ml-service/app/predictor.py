"""Pemuatan model dan inferensi klasifikasi osteoporosis (3 kelas).

Modul ini memuat model EfficientNetV2 hasil fine-tuning sekali saat import
(model loader tingkat modul) dan menyediakan fungsi `predict` untuk melakukan
inferensi pada citra X-ray yang telah dipreprocessing.
"""

import hashlib
import hmac
import os
import tensorflow as tf
import numpy as np

# ── MODEL LOADER ──────────────────────────────────────────────────────────
# Model dimuat sekali saat modul di-import (bukan per-request) agar inferensi
# cepat. `model` diekspor dan dipakai ulang oleh endpoint serta Grad-CAM.
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODEL_PATH = os.getenv("MODEL_PATH", os.path.join(BASE_DIR, "models", "EfficientNetV2_FineTuning.keras"))
MODEL_SHA256 = os.getenv("MODEL_SHA256", "").strip().lower()


def _verify_model_checksum(path: str) -> None:
    """Memverifikasi checksum model bila MODEL_SHA256 dikonfigurasi.

    Pemeriksaan ini mendeteksi model yang tertukar atau dimodifikasi di
    penyimpanan. Bila env tidak diisi, verifikasi dilewati (kompatibilitas
    deployment lama).

    Raises:
        RuntimeError: Bila checksum tidak cocok.
    """
    if not MODEL_SHA256:
        return
    digest = hashlib.sha256()
    with open(path, "rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    if not hmac.compare_digest(digest.hexdigest(), MODEL_SHA256):
        raise RuntimeError(
            "Checksum model tidak cocok. Periksa MODEL_PATH/MODEL_SHA256."
        )


_verify_model_checksum(MODEL_PATH)
model = tf.keras.models.load_model(MODEL_PATH)

# LABEL — urutan indeks harus sama dengan urutan output model.
LABELS = ["Normal", "Osteopenia", "Osteoporosis"]

RISK_MESSAGES = {
    "Normal": "Kepadatan tulang normal — pertahankan pola hidup sehat.",
    "Osteopenia": "Kepadatan tulang mulai menurun — disarankan konsultasi & pencegahan.",
    "Osteoporosis": "Risiko tinggi — disarankan segera konsultasi dengan dokter.",
}

def predict(img_array: np.ndarray) -> dict:
    """Inferensi klasifikasi osteoporosis dari citra terpreprocessing.

    Menjalankan model EfficientNetV2 untuk menghasilkan probabilitas 3 kelas,
    menentukan label dengan probabilitas tertinggi, serta merangkum keyakinan,
    distribusi probabilitas, dan pesan risiko klinis.

    Args:
        img_array: Array float32 berbentuk (1, 224, 224, 3) hasil dari
            `preprocessing.preprocess`.

    Returns:
        dict: Hasil inferensi berisi kunci:
            - "label" (str): kelas prediksi.
            - "confidence" (float): keyakinan label terpilih dalam persen.
            - "probabilities" (dict): probabilitas seluruh 3 kelas dalam persen.
            - "risk" (str): pesan risiko klinis sesuai label.
            - "_class_index" (int): indeks kelas terpilih; dipakai internal
              oleh Grad-CAM dan tidak ikut dikembalikan ke klien.
    """

    # preds = array probabilitas 3 kelas, misal [0.04, 0.09, 0.87]
    preds = model.predict(img_array, verbose=0)[0]

    # index probabilitas tertinggi
    idx = int(np.argmax(preds))
    label = LABELS[idx]
    confidence = round(float(preds[idx]) * 100, 2)

    # semua probabilitas untuk bar chart 3 kelas di frontend
    all_probs = {
        LABELS[i]: round(float(preds[i]) * 100, 2)
        for i in range(len(LABELS))
    }

    return {
        "label": label,
        "confidence": confidence,
        "probabilities": all_probs,
        "risk": RISK_MESSAGES[label],
        "_class_index": idx,
    }