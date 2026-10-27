package com.medikasentosa.shared.security;

import jakarta.servlet.http.HttpServletRequest;

import java.util.regex.Pattern;

/**
 * Menentukan alamat IP klien untuk rate limit dan audit log.
 *
 * <p>Header {@code X-Forwarded-For} tidak dipercaya karena dapat diisi bebas
 * oleh klien dan proxy menambahkan nilai baru di belakang rantai. Nginx pada
 * deployment ini menimpa {@code X-Real-IP} dengan alamat koneksi sebenarnya,
 * sehingga header tersebut yang dipakai — dengan validasi bentuk IP agar nilai
 * aneh tidak masuk ke log. Bila header tidak ada (mis. development langsung
 * ke backend), gunakan {@code getRemoteAddr()}.</p>
 */
public final class ClientIpResolver {

    private static final Pattern IP_LITERAL =
            Pattern.compile("^[0-9a-fA-F:.]{3,45}$");

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request) {
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null) {
            String candidate = realIp.trim();
            if (IP_LITERAL.matcher(candidate).matches()) {
                return candidate;
            }
        }
        return request.getRemoteAddr();
    }
}
