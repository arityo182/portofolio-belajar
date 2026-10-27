package com.medikasentosa.features.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
        @NotBlank @Size(max = 2000, message = "Pesan maksimal 2000 karakter") String message,
        Long sessionId
) {}
