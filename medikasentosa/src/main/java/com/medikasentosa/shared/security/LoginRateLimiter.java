package com.medikasentosa.shared.security;

import com.medikasentosa.shared.exception.TooManyRequestsException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter login in-memory (fixed window).
 *
 * <p>Membatasi 5 kegagalan per 15 menit per kombinasi email + IP. Untuk
 * deployment multi-instance, ganti dengan penghitung berbasis Redis agar
 * konsisten di semua node.</p>
 */
@Component
public class LoginRateLimiter {

    static final int MAX_FAILURES = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);

    private record Attempt(int failures, long windowStartMillis) {
        Attempt increment() {
            return new Attempt(failures + 1, windowStartMillis);
        }

        boolean expired(long now) {
            return now - windowStartMillis > WINDOW.toMillis();
        }
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    /** Tolak request bila kuota kegagalan untuk key ini sudah habis. */
    public void checkAllowed(String key) {
        long now = System.currentTimeMillis();
        Attempt attempt = attempts.get(key);
        if (attempt != null && !attempt.expired(now) && attempt.failures() >= MAX_FAILURES) {
            throw new TooManyRequestsException(
                    "Terlalu banyak percobaan login. Coba lagi dalam 15 menit.");
        }
        cleanupIfNeeded(now);
    }

    public void recordFailure(String key) {
        long now = System.currentTimeMillis();
        attempts.compute(key, (k, old) ->
                old == null || old.expired(now) ? new Attempt(1, now) : old.increment());
    }

    public void reset(String key) {
        attempts.remove(key);
    }

    private void cleanupIfNeeded(long now) {
        if (attempts.size() < 10_000) {
            return;
        }
        attempts.entrySet().removeIf(entry -> entry.getValue().expired(now));
    }
}
