package com.medikasentosa.features.auth.dto;

import com.medikasentosa.features.auth.entity.Role;

/**
 * DTO respons data pengguna yang aman dikembalikan ke klien.
 * Sengaja tidak menyertakan password.
 *
 * @author Ari
 * @since 1.0.0
 */
public record UserResponse(

        Long id,
        String nama,
        String email,
        Role role) {
}
