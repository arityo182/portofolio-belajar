package com.medikasentosa.features.resep.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO permintaan untuk menambahkan resep obat pada sebuah rekam medis.
 *
 * Sejak Fase 2, resep dapat merujuk ke data master obat melalui {@code obatId}.
 * Bila {@code obatId} diisi, {@code namaObat} tidak wajib — backend akan
 * mengambil nama dari tabel obat. Bila {@code obatId} null (fallback),
 * {@code namaObat} wajib diisi.
 *
 * @author Ari
 * @since 1.0.0
 */
public record ResepRequest(

        @NotNull(message = "Rekam medis wajib diisi") Long rekamMedisId,

        /**
         * ID obat dari tabel master obat (opsional, Fase 2).
         * Bila diisi, namaObat akan diambil dari data obat.
         */
        Long obatId,

        /**
         * Nama obat bebas (fallback). Wajib diisi bila {@code obatId} null.
         */
        String namaObat,

        String dosis,

        @NotNull(message = "Jumlah wajib diisi")
        @Min(value = 1, message = "Jumlah minimal 1") Integer jumlah,

        String aturanPakai) {
}
