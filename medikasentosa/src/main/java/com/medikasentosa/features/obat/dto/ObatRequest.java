package com.medikasentosa.features.obat.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO permintaan untuk membuat atau memperbarui data obat.
 *
 * @author Ari
 * @since 1.0.0
 */
public record ObatRequest(

        @NotBlank(message = "Nama obat wajib diisi") String nama,

        @NotBlank(message = "Kategori wajib diisi") String kategori,

        @NotBlank(message = "Satuan wajib diisi") String satuan,

        @NotNull(message = "Stok wajib diisi")
        @Min(value = 0, message = "Stok minimal 0") Integer stok,

        @NotNull(message = "Harga wajib diisi")
        @Min(value = 0, message = "Harga minimal 0") BigDecimal harga,

        String deskripsi,

        LocalDate tanggalProduksi,

        LocalDate tanggalKadaluarsa) {
}