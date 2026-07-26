package com.medikasentosa.features.chat.controller;

import com.medikasentosa.features.chat.dto.*;
import com.medikasentosa.features.chat.service.ChatService;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.auth.repository.UserRepository;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import com.medikasentosa.shared.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat - Senta AI", description = "Chatbot asisten virtual RS Medika Sentosa")
public class ChatController {

    private final ChatService chatService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public ChatController(ChatService chatService, JwtService jwtService, UserRepository userRepository) {
        this.chatService = chatService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/message")
    @Operation(summary = "Kirim pesan ke chatbot Senta")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ChatMessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(extractUserId(authHeader), request));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Daftar sesi chat user")
    public ResponseEntity<List<SessionInfo>> getSessions(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(chatService.getSessions(extractUserId(authHeader)));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "History percakapan satu sesi")
    public ResponseEntity<ChatHistoryResponse> getHistory(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(chatService.getHistory(sessionId, extractUserId(authHeader)));
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "Hapus sesi chat")
    public ResponseEntity<Void> deleteSession(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long sessionId) {
        chatService.deleteSession(sessionId, extractUserId(authHeader));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/public/message")
    @Operation(summary = "Chat publik — tanpa login, tanpa simpan ke DB")
    public ResponseEntity<PublicChatResponse> sendPublicMessage(
            @Valid @RequestBody PublicChatRequest request) {
        return ResponseEntity.ok(chatService.sendPublicMessage(request));
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
        return user.getId();
    }
}
