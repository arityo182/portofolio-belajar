package com.medikasentosa.features.chat.controller;

import com.medikasentosa.features.chat.dto.*;
import com.medikasentosa.features.chat.service.ChatService;
import com.medikasentosa.shared.security.AuthorizationService;
import com.medikasentosa.shared.security.ChatRateLimiter;
import com.medikasentosa.shared.security.ClientIpResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat - Senta AI", description = "Chatbot asisten virtual RS Medika Sentosa")
public class ChatController {

    private final ChatService chatService;
    private final AuthorizationService authorization;
    private final ChatRateLimiter rateLimiter;

    public ChatController(ChatService chatService,
                          AuthorizationService authorization,
                          ChatRateLimiter rateLimiter) {
        this.chatService = chatService;
        this.authorization = authorization;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/message")
    @Operation(summary = "Kirim pesan ke chatbot Senta")
    public ResponseEntity<ChatMessageResponse> sendMessage(@Valid @RequestBody ChatMessageRequest request) {
        Long userId = authorization.currentUserId();
        rateLimiter.checkAllowed("user-" + userId);
        return ResponseEntity.ok(chatService.sendMessage(userId, request));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Daftar sesi chat user")
    public ResponseEntity<List<SessionInfo>> getSessions() {
        return ResponseEntity.ok(chatService.getSessions(authorization.currentUserId()));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "History percakapan satu sesi")
    public ResponseEntity<ChatHistoryResponse> getHistory(@PathVariable Long sessionId) {
        return ResponseEntity.ok(chatService.getHistory(sessionId, authorization.currentUserId()));
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "Hapus sesi chat")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
        chatService.deleteSession(sessionId, authorization.currentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/public/message")
    @Operation(summary = "Chat publik — tanpa login, tanpa simpan ke DB")
    public ResponseEntity<PublicChatResponse> sendPublicMessage(
            @Valid @RequestBody PublicChatRequest request,
            HttpServletRequest httpRequest) {
        rateLimiter.checkAllowed("ip-" + ClientIpResolver.resolve(httpRequest));
        return ResponseEntity.ok(chatService.sendPublicMessage(request));
    }
}
