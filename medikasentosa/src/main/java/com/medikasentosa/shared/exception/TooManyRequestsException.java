package com.medikasentosa.shared.exception;

/** Dilempar saat rate limit login terlampaui. */
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
