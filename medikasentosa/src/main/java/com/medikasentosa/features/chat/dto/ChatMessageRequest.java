package com.medikasentosa.features.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
        @NotBlank String message,
        Long sessionId
) {}
