package com.medikasentosa.features.screening.dto;

import java.time.Instant;
import java.util.Map;

/**
 * DTO respons hasil screening yang dikembalikan ke klien,
 * berisi ringkasan prediksi model beserta waktu pembuatannya.
 *
 * @author Ari
 * @since 1.0.0
 */
public record ScreeningResponse(

        Long id,
        String label,
        Double confidence,
        Map<String, Double> probabilities,
        String risk,
        String gradcamImage,
        Long latencyMs,
        Instant createdAt) {
}
