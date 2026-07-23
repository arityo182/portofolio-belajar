package com.medikasentosa.features.auth.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO permintaan registrasi berisi data pengguna baru (nama, email, password)
 * beserta aturan validasinya.
 *
 * @author Ari
 * @since 1.0.0
 */
public record RegisterRequest(
        @NotBlank(message = "Nama wajib diisi") String nama,

        @NotBlank(message = "Email wajib diisi") @Email(message = "Format email tidak valid") String email,

        @NotBlank(message = "Password wajib diisi") @Size(min = 6, message = "Password minimal 6 karakter") String password) {
}
