package com.medikasentosa.features.chat.dto;

import java.time.LocalDateTime;

public record SessionInfo(
        Long id,
        String judul,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
