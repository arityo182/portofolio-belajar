package com.medikasentosa.features.chat.service;

import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.auth.repository.UserRepository;
import com.medikasentosa.features.chat.dto.*;
import com.medikasentosa.features.chat.entity.ChatMessage;
import com.medikasentosa.features.chat.entity.ChatSession;
import com.medikasentosa.features.chat.repository.ChatMessageRepository;
import com.medikasentosa.features.chat.repository.ChatSessionRepository;
import com.medikasentosa.features.dokter.repository.DokterRepository;
import com.medikasentosa.features.jadwaldokter.repository.JadwalDokterRepository;
import com.medikasentosa.features.pasien.repository.PasienRepository;
import com.medikasentosa.features.poli.repository.PoliRepository;
import com.medikasentosa.features.rekammedis.repository.RekamMedisRepository;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final UserRepository userRepo;
    private final PoliRepository poliRepo;
    private final DokterRepository dokterRepo;
    private final JadwalDokterRepository jadwalRepo;
    private final RekamMedisRepository rekamMedisRepo;
    private final PasienRepository pasienRepo;
    private final RestTemplate restTemplate;

    @Value("${app.chatbot.url:http://localhost:8002}")
    private String chatbotUrl;

    @Value("${app.chatbot.api-key:}")
    private String chatbotApiKey;

    public ChatService(ChatSessionRepository sessionRepo, ChatMessageRepository messageRepo,
                       UserRepository userRepo, PoliRepository poliRepo, DokterRepository dokterRepo,
                       JadwalDokterRepository jadwalRepo, RekamMedisRepository rekamMedisRepo,
                       PasienRepository pasienRepo, RestTemplate restTemplate) {
        this.sessionRepo = sessionRepo;
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
        this.poliRepo = poliRepo;
        this.dokterRepo = dokterRepo;
        this.jadwalRepo = jadwalRepo;
        this.rekamMedisRepo = rekamMedisRepo;
        this.pasienRepo = pasienRepo;
        this.restTemplate = restTemplate;
    }

    /**
     * Kirim pesan ke Senta → simpan USER + ASSISTANT → return response.
     */
    public ChatMessageResponse sendMessage(Long userId, ChatMessageRequest req) {
        log.info("Sending chat message for userId={}", userId);
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        // 1. Ambil atau buat session
        ChatSession session;
        if (req.sessionId() != null) {
            session = sessionRepo.findById(req.sessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sesi chat tidak ditemukan"));
            if (!session.getUser().getId().equals(userId)) {
                throw new ResourceNotFoundException("Sesi chat bukan milik user ini");
            }
        } else {
            session = new ChatSession();
            session.setUser(user);
            session.setJudul(truncate(req.message(), 50));
            session = sessionRepo.save(session);
        }

        // 2. Simpan pesan USER
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSession(session);
        userMsg.setRole(ChatMessage.MessageRole.USER);
        userMsg.setContent(req.message());
        messageRepo.save(userMsg);

        // 3. Ambil history terakhir (dibatasi agar prompt tidak membengkak)
        List<ChatMessage> history = messageRepo.findBySessionIdOrderByCreatedAtAsc(session.getId());
        List<Map<String, String>> sessionHistory = sanitizeHistory(history);

        // 4. Build context dari DB
        ContextData context = buildContext(userId);

        // 5. Panggil chatbot-service dengan identitas pemilik sesi untuk rate limit
        String sentaResponse = callChatbot(
                req.message(), sessionHistory, context, "user-" + userId);

        // 6. Simpan pesan ASSISTANT
        ChatMessage botMsg = new ChatMessage();
        botMsg.setSession(session);
        botMsg.setRole(ChatMessage.MessageRole.ASSISTANT);
        botMsg.setContent(sentaResponse);
        messageRepo.save(botMsg);

        // 7. Update session
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepo.save(session);

        return new ChatMessageResponse(botMsg.getId(), session.getId(),
                session.getJudul(), "ASSISTANT", sentaResponse, botMsg.getCreatedAt());
    }

    /** Daftar sesi chat user. */
    public List<SessionInfo> getSessions(Long userId) {
        return sessionRepo.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(s -> new SessionInfo(s.getId(), s.getJudul(), s.getCreatedAt(), s.getUpdatedAt()))
                .toList();
    }

    /** History lengkap satu sesi. */
    public ChatHistoryResponse getHistory(Long sessionId, Long userId) {
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesi tidak ditemukan"));
        if (!session.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Sesi bukan milik user ini");
        }
        List<ChatMessageResponse> msgs = messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(m -> new ChatMessageResponse(m.getId(), sessionId, session.getJudul(),
                        m.getRole().name(), m.getContent(), m.getCreatedAt()))
                .toList();
        return new ChatHistoryResponse(
                new SessionInfo(session.getId(), session.getJudul(), session.getCreatedAt(), session.getUpdatedAt()),
                msgs);
    }

    /** Hapus sesi + semua message. */
    public void deleteSession(Long sessionId, Long userId) {
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesi tidak ditemukan"));
        if (!session.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Sesi bukan milik user ini");
        }
        sessionRepo.delete(session);
    }

    // ─── Private helpers ───

    private ContextData buildContext(Long userId) {
        var poli = poliRepo.findByIsActiveTrue().stream()
                .map(p -> Map.<String, Object>of("id", p.getId(), "nama", safe(p.getNama()),
                        "deskripsi", safe(p.getDeskripsi())))
                .toList();
        var dokter = dokterRepo.findAll().stream()
                .map(d -> Map.<String, Object>of(
                        "id", d.getId(), "nama", safe(d.getUser().getNama()),
                        "spesialisasi", safe(d.getSpesialisasi()),
                        "namaPoli", d.getPoli() != null ? safe(d.getPoli().getNama()) : ""))
                .toList();
        var jadwal = jadwalRepo.findByIsActiveTrue().stream()
                .map(j -> Map.<String, Object>of(
                        "id", j.getId(),
                        "namaDokter", j.getDokter() != null ? safe(j.getDokter().getUser().getNama()) : "",
                        "hari", j.getHari() != null ? j.getHari().name() : "",
                        "jamMulai", j.getJamMulai() != null ? j.getJamMulai().toString() : "",
                        "jamSelesai", j.getJamSelesai() != null ? j.getJamSelesai().toString() : "",
                        "kuota", j.getKuota() != null ? j.getKuota() : 0))
                .toList();
        Map<String, Object> rm = null;
        var pasienOpt = pasienRepo.findByUserId(userId);
        if (pasienOpt.isPresent()) {
            var rekamMedis = rekamMedisRepo.findByPasienId(pasienOpt.get().getId());
            if (!rekamMedis.isEmpty()) {
                var r = rekamMedis.get(0); // terbaru (ordered by DB or sort manually)
                rm = Map.of("diagnosa", r.getDiagnosa() != null ? r.getDiagnosa() : "",
                        "tindakan", r.getTindakan() != null ? r.getTindakan() : "");
            }
        }
        return new ContextData(poli, dokter, jadwal, rm);
    }

    private String callChatbot(String message, List<Map<String, String>> history,
                               ContextData context, String clientKey) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("message", message);
            body.put("sessionHistory", history);
            body.put("context", context);
            body.put("stream", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (chatbotApiKey != null && !chatbotApiKey.isBlank()) {
                headers.set("X-Internal-API-Key", chatbotApiKey);
            }
            if (clientKey != null && !clientKey.isBlank()) {
                headers.set("X-Client-Key", clientKey);
            }
            var entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    chatbotUrl + "/chat/message", entity, Map.class);

            if (response.getBody() != null && response.getBody().get("response") != null) {
                return (String) response.getBody().get("response");
            }
        } catch (Exception e) {
            return "Maaf, Senta sedang tidak tersedia saat ini. Silakan coba lagi nanti.";
        }
        return "Maaf, Senta tidak dapat merespon. Silakan hubungi petugas RS.";
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }

    /** Null-safe string untuk nilai yang dikirim ke chatbot (Map.of menolak null). */
    private String safe(String value) {
        return value != null ? value : "";
    }

    /**
     * Batasi riwayat yang dikirim ke chatbot: maksimum 20 pesan terakhir dan
     * maksimum 2000 karakter per pesan (sama dengan validasi chatbot-service).
     */
    private List<Map<String, String>> sanitizeHistory(List<ChatMessage> history) {
        int from = Math.max(0, history.size() - 20);
        return history.subList(from, history.size()).stream()
                .map(m -> Map.of(
                        "role", "ASSISTANT".equalsIgnoreCase(m.getRole().name())
                                ? "assistant" : "user",
                        "content", truncate(m.getContent(), 2000)))
                .collect(Collectors.toList());
    }

    /**
     * Kirim pesan publik (tanpa login) ke chatbot Senta.
     * Tidak menyimpan sesi/pesan ke database.
     * Context hanya berisi data RS umum (poli, dokter, jadwal) — tanpa rekam medis.
     */
    public PublicChatResponse sendPublicMessage(PublicChatRequest req) {
        // Build context publik (tanpa data personal)
        ContextData context = buildPublicContext();

        // Sanitasi riwayat dari client (tidak dipercaya) sebelum diteruskan.
        List<Map<String, String>> history = sanitizeRawHistory(req.sessionHistory());

        // Panggil chatbot-service (tanpa data pasien; key publik dibatasi global)
        String response = callChatbot(req.message(), history, context, "public");

        return new PublicChatResponse(response);
    }

    private ContextData buildPublicContext() {
        var poli = poliRepo.findByIsActiveTrue().stream()
                .map(p -> Map.<String, Object>of("id", p.getId(), "nama", safe(p.getNama()),
                        "deskripsi", safe(p.getDeskripsi())))
                .toList();
        var dokter = dokterRepo.findAll().stream()
                .map(d -> Map.<String, Object>of(
                        "id", d.getId(), "nama", safe(d.getUser().getNama()),
                        "spesialisasi", safe(d.getSpesialisasi()),
                        "namaPoli", d.getPoli() != null ? safe(d.getPoli().getNama()) : ""))
                .toList();
        var jadwal = jadwalRepo.findByIsActiveTrue().stream()
                .map(j -> Map.<String, Object>of(
                        "id", j.getId(),
                        "namaDokter", j.getDokter() != null ? safe(j.getDokter().getUser().getNama()) : "",
                        "hari", j.getHari() != null ? j.getHari().name() : "",
                        "jamMulai", j.getJamMulai() != null ? j.getJamMulai().toString() : "",
                        "jamSelesai", j.getJamSelesai() != null ? j.getJamSelesai().toString() : "",
                        "kuota", j.getKuota() != null ? j.getKuota() : 0))
                .toList();
        return new ContextData(poli, dokter, jadwal, null);
    }

    /** Sanitasi riwayat publik: hanya role user/assistant, maks 20 item. */
    private List<Map<String, String>> sanitizeRawHistory(List<Map<String, String>> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        int from = Math.max(0, raw.size() - 20);
        return raw.subList(from, raw.size()).stream()
                .filter(item -> item != null)
                .map(item -> {
                    String role = item.getOrDefault("role", "user");
                    if (!"assistant".equalsIgnoreCase(role)) {
                        role = "user";
                    }
                    return Map.of(
                            "role", role,
                            "content", truncate(item.getOrDefault("content", ""), 2000));
                })
                .collect(Collectors.toList());
    }
}
