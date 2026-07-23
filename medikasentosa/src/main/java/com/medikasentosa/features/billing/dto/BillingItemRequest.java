package com.medikasentosa.features.billing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** DTO for adding an item to billing. */
public record BillingItemRequest(
        @NotBlank String deskripsi,
        @NotNull @Min(1) Integer jumlah,
        @NotNull @Min(0) BigDecimal hargaSatuan) {
}