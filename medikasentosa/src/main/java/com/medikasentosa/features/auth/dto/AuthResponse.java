package com.medikasentosa.features.auth.dto;

/**
 * DTO respons autentikasi yang mengembalikan token JWT beserta data pengguna
 * setelah registrasi atau login berhasil.
 *
 * @author Ari
 * @since 1.0.0
 */
public record AuthResponse(
        String token,        // token JWT untuk otorisasi request berikutnya
        UserResponse user) {
}
