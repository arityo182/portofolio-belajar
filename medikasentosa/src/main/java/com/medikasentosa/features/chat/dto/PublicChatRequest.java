package com.medikasentosa.features.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * Request untuk chat publik (tanpa login).
 * Tidak perlu sessionId — history dikirim inline oleh frontend.
 */
public record PublicChatRequest(
        @NotBlank @Size(max = 2000, message = "Pesan maksimal 2000 karakter") String message,
        @Size(max = 20, message = "Riwayat maksimal 20 pesan") List<Map<String, String>> sessionHistory
) {}
