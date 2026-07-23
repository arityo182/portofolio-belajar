package com.medikasentosa.features.billing.dto;

import com.medikasentosa.features.billing.entity.StatusBilling;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Flat response for billing + items. */
public record BillingResponse(
        Long id, Long pasienId, String namaPasien,
        BigDecimal totalHarga, StatusBilling status,
        LocalDate tanggalTagihan, String catatan,
        List<BillingItemResponse> items) {
}