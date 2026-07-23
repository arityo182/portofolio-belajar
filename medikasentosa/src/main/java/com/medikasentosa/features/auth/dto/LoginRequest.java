package com.medikasentosa.features.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO permintaan login berisi kredensial pengguna (email dan password).
 *
 * @author Ari
 * @since 1.0.0
 */
public record LoginRequest(
        @NotBlank(message = "Email wajib diisi") @Email(message = "Format email tidak valid") String email,

        @NotBlank(message = "Password wajib diisi") String password) {
}
