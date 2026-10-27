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

        @NotBlank(message = "Password wajib diisi")
        @Size(min = 12, max = 128, message = "Password minimal 12 karakter")
        @jakarta.validation.constraints.Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "Password harus mengandung minimal satu huruf dan satu angka")
        String password) {
}
