package com.medikasentosa.shared.security;

import com.medikasentosa.shared.exception.TooManyRequestsException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter endpoint chat (in-memory, fixed window).
 *
 * <p>Membatasi jumlah pesan per pengguna (login) atau per IP (chat publik)
 * agar endpoint yang memanggil LLM tidak dapat dipakai untuk flooding.
 * Untuk deployment multi-instance, ganti dengan penghitung berbasis Redis.</p>
 */
@Component
public class ChatRateLimiter {

    static final int MAX_REQUESTS = 30;
    static final Duration WINDOW = Duration.ofMinutes(1);

    private record Window(int count, long startMillis) {
        boolean expired(long now) {
            return now - startMillis > WINDOW.toMillis();
        }
    }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public void checkAllowed(String key) {
        long now = System.currentTimeMillis();
        Window window = windows.compute(key, (k, old) ->
                old == null || old.expired(now) ? new Window(1, now)
                        : new Window(old.count() + 1, old.startMillis()));
        if (window.count() > MAX_REQUESTS) {
            throw new TooManyRequestsException(
                    "Terlalu banyak pesan. Coba lagi dalam satu menit.");
        }
        cleanupIfNeeded(now);
    }

    private void cleanupIfNeeded(long now) {
        if (windows.size() < 10_000) {
            return;
        }
        windows.entrySet().removeIf(entry -> entry.getValue().expired(now));
    }
}
