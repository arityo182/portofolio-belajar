package com.medikasentosa.shared.dto;

import java.time.Instant;

/**
 * DTO respons error standar yang dikembalikan saat terjadi kesalahan,
 * berisi kode status, jenis error, pesan, dan waktu kejadian.
 *
 * @author Ari
 * @since 1.0.0
 */
public record ErrorResponse(

        int status,
        String error,
        String message,
        Instant timestamp) {
}
