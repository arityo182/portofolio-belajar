package com.medikasentosa.features.pembayaran.dto;

import com.medikasentosa.features.pembayaran.entity.MetodePembayaran;
import com.medikasentosa.features.pembayaran.entity.StatusPembayaran;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PembayaranResponse(
        Long id, Long billingId, String namaPasien,
        BigDecimal totalBilling, MetodePembayaran metodePembayaran,
        BigDecimal jumlahBayar, StatusPembayaran status,
        String buktiTransfer, LocalDateTime tanggalBayar, String catatan) {
}