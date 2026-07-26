package com.medikasentosa.features.chat.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long sessionId,
        String sessionJudul,
        String role,
        String content,
        LocalDateTime createdAt
) {}
