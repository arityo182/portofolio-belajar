package com.medikasentosa.shared.exception;

/** Dilempar saat access/refresh token tidak valid atau sudah dicabut. */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
