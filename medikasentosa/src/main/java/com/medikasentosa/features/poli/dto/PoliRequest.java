package com.medikasentosa.features.poli.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO permintaan untuk membuat atau memperbarui data poli (nama dan deskripsi).
 *
 * @author Ari
 * @since 1.0.0
 */
public record PoliRequest(

        @NotBlank(message = "Nama poli wajib diisi") String nama,

        String deskripsi) {
}
