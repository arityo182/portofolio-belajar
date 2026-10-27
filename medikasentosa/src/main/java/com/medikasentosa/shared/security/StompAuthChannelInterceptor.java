package com.medikasentosa.shared.security;

import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Autentikasi dan otorisasi STOMP.
 *
 * <p>Handshake HTTP tidak membawa identitas (dan tidak boleh dipercaya untuk
 * otorisasi), sehingga autentikasi dilakukan pada frame CONNECT: header
 * {@code Authorization: Bearer <access token>} divalidasi dengan JWT yang sama
 * seperti REST. Setelah itu setiap SUBSCRIBE diperiksa terhadap kepemilikan
 * resource sehingga pasien tidak dapat menguping notifikasi pasien lain.</p>
 *
 * <p>Frame SEND dari client ditolak seluruhnya karena aplikasi hanya memakai
 * WebSocket satu arah (server → client).</p>
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String TOPIC_PASIEN = "/topic/pasien/";
    private static final String TOPIC_DOKTER = "/topic/dokter/";
    private static final String TOPIC_APPOINTMENT = "/topic/appointment/";
    private static final String TOPIC_ANTRIAN = "/topic/antrian/";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthorizationService authorization;

    public StompAuthChannelInterceptor(JwtService jwtService,
                                       CustomUserDetailsService userDetailsService,
                                       AuthorizationService authorization) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authorization = authorization;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (StompCommand.CONNECT.equals(command)) {
            accessor.setUser(authenticate(accessor));
            return message;
        }

        java.security.Principal principal = accessor.getUser();
        if (!(principal instanceof Authentication authentication) || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Koneksi WebSocket belum terautentikasi");
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {
            authorizeSubscription(accessor.getDestination(), authentication);
        }

        // Aplikasi tidak memakai pesan client → server. Tolak agar tidak ada
        // permukaan serangan tambahan (mis. spam atau injeksi ke broker).
        if (StompCommand.SEND.equals(command)) {
            throw new AccessDeniedException("Pengiriman pesan melalui WebSocket tidak diizinkan");
        }

        return message;
    }

    private Authentication authenticate(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new AccessDeniedException("Header Authorization WebSocket tidak ditemukan");
        }
        String token = header.substring("Bearer ".length()).trim();
        try {
            String email = jwtService.extractEmail(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (!jwtService.isTokenValid(token, userDetails)) {
                throw new AccessDeniedException("Token WebSocket tidak valid");
            }
            return new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AccessDeniedException("Token WebSocket tidak valid");
        }
    }

    private void authorizeSubscription(String destination, Authentication authentication) {
        if (destination == null || destination.isBlank()) {
            throw new AccessDeniedException("Destinasi subscribe tidak valid");
        }

        SecurityContext previous = SecurityContextHolder.getContext();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        try {
            if (destination.startsWith(TOPIC_PASIEN)) {
                authorization.requirePasienAccess(parseId(destination, TOPIC_PASIEN));
                return;
            }
            if (destination.startsWith(TOPIC_DOKTER)) {
                authorization.requireDokterSelfOrAdmin(parseId(destination, TOPIC_DOKTER));
                return;
            }
            if (destination.startsWith(TOPIC_APPOINTMENT)) {
                authorization.requireAppointmentAccess(parseId(destination, TOPIC_APPOINTMENT));
                return;
            }
            if (destination.startsWith(TOPIC_ANTRIAN)) {
                // Antrian adalah informasi operasional; hanya staf/admin.
                authorization.requireStaff();
                String remainder = destination.substring(TOPIC_ANTRIAN.length());
                if (remainder.isBlank()) {
                    throw new AccessDeniedException("Destinasi antrian tidak valid");
                }
                return;
            }
            throw new AccessDeniedException("Destinasi subscribe tidak diizinkan");
        } finally {
            SecurityContextHolder.setContext(previous);
        }
    }

    private Long parseId(String destination, String prefix) {
        String raw = destination.substring(prefix.length());
        try {
            return Long.valueOf(raw);
        } catch (NumberFormatException e) {
            throw new AccessDeniedException("Destinasi subscribe tidak valid");
        }
    }
}
