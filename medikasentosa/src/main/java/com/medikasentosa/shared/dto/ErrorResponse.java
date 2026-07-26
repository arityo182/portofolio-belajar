package com.medikasentosa.shared.dto;

import java.time.Instant;

/**
 * DTO respons error standar — dengan request ID untuk tracing.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        String requestId,
        Instant timestamp) {

    /** Factory tanpa requestId (fallback). */
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, "N/A", Instant.now());
    }
}
