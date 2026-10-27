package com.medikasentosa.shared.exception;

/**
 * Exception ketika layanan eksternal (mis. ML service / chatbot) sedang tidak
 * dapat dihubungi atau mengembalikan respons yang tidak valid.
 *
 * <p>Dipetakan ke HTTP 503 agar frontend dapat menampilkan pesan "coba lagi
 * nanti" tanpa menganggapnya sebagai bug internal server.</p>
 */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
