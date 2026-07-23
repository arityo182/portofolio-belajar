package com.medikasentosa.features.laboratorium.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO permintaan untuk membuat order pemeriksaan laboratorium
 * dari sebuah rekam medis (FR-27).
 *
 * @author Ari
 * @since 1.0.0
 */
public record LabOrderRequest(

        @NotNull(message = "Rekam medis wajib diisi") Long rekamMedisId,

        @NotBlank(message = "Jenis pemeriksaan wajib diisi") String jenisPemeriksaan,

        String catatan) {
}