package com.medikasentosa.features.admin.dto;

import com.medikasentosa.features.auth.entity.Role;

/**
 * DTO respons data user untuk halaman manajemen pengguna admin.
 *
 * Menyertakan status aktif/nonaktif akun yang tidak ada di {@code UserResponse}
 * reguler (yang dipakai untuk data user yang sedang login).
 *
 * @author Ari
 * @since 1.0.0
 */
public record UserAdminResponse(
        Long id,
        String nama,
        String email,
        Role role,
        Boolean isActive) {
}
