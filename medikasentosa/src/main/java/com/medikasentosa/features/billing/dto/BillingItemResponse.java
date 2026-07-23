package com.medikasentosa.features.billing.dto;

import java.math.BigDecimal;

/** Flat response for billing item. */
public record BillingItemResponse(
        Long id, Long billingId,
        String deskripsi, Integer jumlah,
        BigDecimal hargaSatuan, BigDecimal subtotal) {
}