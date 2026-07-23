package com.medikasentosa.features.pembayaran.dto;

import com.medikasentosa.features.pembayaran.entity.MetodePembayaran;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PembayaranRequest(
        @NotNull Long billingId,
        @NotNull MetodePembayaran metodePembayaran,
        @NotNull @Min(0) BigDecimal jumlahBayar,
        String buktiTransfer) {
}