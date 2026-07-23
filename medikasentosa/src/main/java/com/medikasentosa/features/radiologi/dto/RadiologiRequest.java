package com.medikasentosa.features.radiologi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO permintaan untuk membuat order radiologi dari sebuah rekam medis (FR-29).
 *
 * @author Ari
 * @since 1.0.0
 */
public record RadiologiRequest(

        @NotNull(message = "Rekam medis wajib diisi") Long rekamMedisId,

        @NotBlank(message = "Jenis radiologi wajib diisi") String jenisRadiologi,

        String catatanDokter) {
}