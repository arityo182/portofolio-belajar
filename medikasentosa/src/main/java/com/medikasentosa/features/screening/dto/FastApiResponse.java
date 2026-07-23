package com.medikasentosa.features.screening.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * DTO yang memetakan respons prediksi dari layanan machine learning (FastAPI).
 * Digunakan untuk men-deserialisasi hasil model sebelum disimpan sebagai screening.
 *
 * @author Ari
 * @since 1.0.0
 */
public record FastApiResponse(
    String label,
    Double confidence,
    Map<String, Double> probabilities,   // probabilitas per kelas dari model
    String risk,                         // pesan risiko yang dihasilkan model
    @JsonProperty("gradcamImage") String gradcamImage,   // citra Grad-CAM (base64) untuk visualisasi
    @JsonProperty("latencyMs") Double latencyMs          // latensi inferensi model (ms)
) {}