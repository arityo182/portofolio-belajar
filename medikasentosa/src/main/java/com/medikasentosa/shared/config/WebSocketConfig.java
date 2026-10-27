package com.medikasentosa.shared.config;

import com.medikasentosa.shared.security.StompAuthChannelInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;

/**
 * Konfigurasi STOMP over WebSocket untuk notifikasi real-time.
 *
 * <p>Handshake hanya mengizinkan origin yang dikonfigurasi. Autentikasi dan
 * otorisasi dilakukan pada level frame STOMP oleh
 * {@link StompAuthChannelInterceptor} — handshake itu sendiri tidak membawa
 * identitas yang dapat dipercaya.</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthInterceptor;

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    public WebSocketConfig(StompAuthChannelInterceptor stompAuthInterceptor) {
        this.stompAuthInterceptor = stompAuthInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix untuk broadcast dari server ke client
        registry.enableSimpleBroker("/topic");
        // Prefix untuk pesan dari client ke server
        registry.setApplicationDestinationPrefixes("/app");
        // Pertahankan urutan pesan agar notifikasi status tidak datang terbalik.
        registry.setPreservePublishOrder(true);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toArray(String[]::new);

        registry.addEndpoint("/ws")
                .setAllowedOrigins(origins)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthInterceptor);
    }
}
