package com.medikasentosa.features.chat.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * Request untuk chat publik (tanpa login).
 * Tidak perlu sessionId — history dikirim inline oleh frontend.
 */
public record PublicChatRequest(
        @NotBlank String message,
        List<Map<String, String>> sessionHistory
) {}
