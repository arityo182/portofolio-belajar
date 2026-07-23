package com.medikasentosa.features.dokter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO permintaan untuk membuat atau memperbarui data dokter.
 *
 * @author Ari
 * @since 1.0.0
 */
public record DokterRequest(

        @NotNull(message = "User wajib diisi") Long userId,

        @NotNull(message = "Poli wajib diisi") Long poliId,

        @NotBlank(message = "No STR wajib diisi") String noStr,

        @NotBlank(message = "Spesialisasi wajib diisi") String spesialisasi,

        @NotBlank(message = "No HP wajib diisi") String noHp) {
}
