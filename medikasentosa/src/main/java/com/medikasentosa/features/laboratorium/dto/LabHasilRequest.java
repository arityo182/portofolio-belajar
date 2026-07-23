package com.medikasentosa.features.laboratorium.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO permintaan untuk mengisi hasil pemeriksaan laboratorium (FR-28).
 *
 * Saat hasil diisi, status order laboratorium otomatis berubah menjadi SELESAI.
 *
 * @author Ari
 * @since 1.0.0
 */
public record LabHasilRequest(

        @NotNull(message = "Order laboratorium wajib diisi") Long labOrderId,

        @NotBlank(message = "Hasil pemeriksaan wajib diisi") String hasilText,

        String nilaiNormal,

        String interpretasi) {
}