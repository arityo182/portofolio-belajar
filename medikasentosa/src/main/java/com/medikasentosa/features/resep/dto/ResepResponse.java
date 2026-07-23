package com.medikasentosa.features.resep.dto;

/**
 * DTO respons data resep obat yang dikembalikan ke klien.
 *
 * Sejak Fase 2, respons menyertakan {@code obatId} yang merujuk ke data
 * master obat ({@code /api/obat}), atau {@code null} untuk resep lama
 * yang belum memiliki relasi tersebut. Field {@code namaObat} selalu berisi
 * nama obat yang akurat (dari tabel obat jika tersedia, atau fallback).
 *
 * @author Ari
 * @since 1.0.0
 */
public record ResepResponse(
        Long id,
        Long rekamMedisId,
        Long obatId,
        String namaObat,
        String dosis,
        Integer jumlah,
        String aturanPakai) {
}
