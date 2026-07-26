package com.medikasentosa.features.chat.dto;

import java.util.List;

public record ChatHistoryResponse(
        SessionInfo session,
        List<ChatMessageResponse> messages
) {}
