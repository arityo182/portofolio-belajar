package com.medikasentosa.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Konfigurasi STOMP over WebSocket untuk notifikasi real-time.
 *
 * <p>Client dapat subscribe ke topic (broadcast) dan mengirim pesan ke
 * app (client → server). SockJS disediakan sebagai fallback untuk browser
 * yang tidak mendukung WebSocket native.</p>
 *
 * <p>Ini adalah infrastruktur tambahan — semua endpoint REST yang sudah
 * ada tidak diubah behavior-nya. WebSocket hanya menambah efek samping
 * notifikasi real-time.</p>
 *
 * @author Ari
 * @since 1.0.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix untuk broadcast dari server ke client
        registry.enableSimpleBroker("/topic");
        // Prefix untuk pesan dari client ke server
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket dengan fallback SockJS
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173")
                .withSockJS();
    }
}