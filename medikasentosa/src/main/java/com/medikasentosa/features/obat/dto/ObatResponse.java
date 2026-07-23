package com.medikasentosa.features.obat.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO respons data obat yang dikembalikan ke klien.
 *
 * @author Ari
 * @since 1.0.0
 */
public record ObatResponse(
        Long id, String nama, String kategori, String satuan,
        Integer stok, BigDecimal harga, String deskripsi,
        LocalDate tanggalProduksi, LocalDate tanggalKadaluarsa,
        Boolean isActive) {
}