package com.medikasentosa.features.rawatinap.dto;

import com.medikasentosa.features.rawatinap.entity.TipeKamar;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO permintaan untuk membuat atau memperbarui data kamar.
 *
 * @author Ari
 * @since 1.0.0
 */
public record KamarRequest(

        @NotBlank(message = "Nomor kamar wajib diisi") String nomorKamar,

        @NotNull(message = "Tipe kamar wajib diisi") TipeKamar tipeKamar,

        @NotNull(message = "Kapasitas wajib diisi")
        @Min(value = 1, message = "Kapasitas minimal 1") Integer kapasitas,

        @NotNull(message = "Harga per malam wajib diisi")
        @Min(value = 0, message = "Harga minimal 0") BigDecimal hargaPerMalam) {
}