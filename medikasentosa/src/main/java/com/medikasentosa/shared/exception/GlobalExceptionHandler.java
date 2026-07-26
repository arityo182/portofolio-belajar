package com.medikasentosa.shared.exception;

import com.medikasentosa.shared.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Data tidak ditemukan 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    // Duplikasi data 409
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    // Argumen tidak valid 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    // Validasi DTO (@Valid) 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst().map(err -> err.getDefaultMessage())
                .orElse("Data tidak valid");
        return build(HttpStatus.BAD_REQUEST, message, req);
    }

    // Method tidak diizinkan 405
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Method tidak didukung untuk endpoint ini", req);
    }

    // Forbidden / akses ditolak 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Akses ditolak — Anda tidak memiliki izin", req);
    }

    // Generic fallback 500 — tangkap semua error yang tidak terduga
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("[{}] Unhandled exception: {}", getRequestId(req), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Terjadi kesalahan server. Silakan coba lagi.", req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        String requestId = getRequestId(req);
        ErrorResponse body = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                requestId,
                Instant.now());
        return ResponseEntity.status(status).body(body);
    }

    private String getRequestId(HttpServletRequest req) {
        Object attr = req.getAttribute("requestId");
        return attr != null ? attr.toString() : "N/A";
    }
}
