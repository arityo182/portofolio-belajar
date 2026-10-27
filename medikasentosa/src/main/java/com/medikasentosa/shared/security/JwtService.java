package com.medikasentosa.shared.security;

import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.auth.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.function.Function;

/**
 * Pembuatan dan validasi JWT.
 *
 * <p>Access token berumur pendek dan membawa klaim {@code type=access} serta
 * {@code ver} (token version). Refresh token berumur panjang, hanya dikirim
 * melalui cookie HttpOnly, dan membawa klaim {@code type=refresh}. Setiap
 * perubahan versi token pada user (logout, reset password, akun dinonaktifkan)
 * membatalkan seluruh token lama.</p>
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private static final Set<String> WEAK_SECRETS = Set.of(
            "dev_jwt_secret_change_me",
            "dev_jwt_secret_change_me_only_for_local_development_32b",
            "dev_jwt_secret_change_me_in_production_use_openssl_rand_base64_48",
            "secret",
            "changeme",
            "CHANGE_ME",
            "CHANGE_ME_USE_openssl_rand_base64_48",
            "your-secret-key",
            "jwt-secret"
    );

    private final UserRepository userRepository;
    private final Environment environment;

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms:900000}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    public JwtService(UserRepository userRepository, Environment environment) {
        this.userRepository = userRepository;
        this.environment = environment;
    }

    @PostConstruct
    void validateSecret() {
        boolean production = environment.acceptsProfiles(Profiles.of("production", "prod"));
        boolean tooShort = secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32;
        boolean weak = secret != null && WEAK_SECRETS.contains(secret.trim());

        if (tooShort || weak) {
            String message = "JWT_SECRET tidak aman: gunakan minimal 32 byte acak "
                    + "(contoh: openssl rand -base64 48).";
            if (production) {
                throw new IllegalStateException(message);
            }
            log.warn("{} Mode development mengizinkan secret lemah, JANGAN dipakai di produksi.", message);
        }

        if (accessExpirationMs <= 0 || accessExpirationMs > 3_600_000L) {
            log.warn("Access token lifetime {} ms tidak lazim; disarankan <= 15 menit.", accessExpirationMs);
        }
        if (refreshExpirationMs <= accessExpirationMs) {
            throw new IllegalStateException("Refresh token harus lebih lama dari access token");
        }
    }

    public String generateToken(String email) {
        User user = loadUser(email);
        return generateAccessToken(user);
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("type", "access")
                .claim("ver", versionOf(user))
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessExpirationMs))
                .signWith(getSignKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("type", "refresh")
                .claim("ver", versionOf(user))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshExpirationMs))
                .signWith(getSignKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public int extractTokenVersion(String token) {
        Integer version = extractClaim(token, claims -> claims.get("ver", Integer.class));
        return version == null ? 0 : version;
    }

    /**
     * Validasi access token terhadap user di database: tipe token, email,
     * expiry, status aktif, dan token version.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = parseClaims(token);
            if (!"access".equals(claims.get("type", String.class))) {
                return false;
            }
            String email = claims.getSubject();
            if (email == null || !email.equals(userDetails.getUsername()) || isExpired(claims)) {
                return false;
            }
            User user = loadUser(email);
            if (Boolean.FALSE.equals(user.getIsActive())) {
                return false;
            }
            Integer version = claims.get("ver", Integer.class);
            return (version == null ? 0 : version) == versionOf(user);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Validasi refresh token dan kembalikan user pemiliknya.
     *
     * @throws JwtException bila token tidak valid, kedaluwarsa, atau sudah dicabut
     */
    public User validateRefreshToken(String token) {
        Claims claims = parseClaims(token);
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new JwtException("Token bukan refresh token");
        }
        if (isExpired(claims)) {
            throw new JwtException("Refresh token kedaluwarsa");
        }
        User user = loadUser(claims.getSubject());
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new JwtException("Akun tidak aktif");
        }
        Integer version = claims.get("ver", Integer.class);
        if ((version == null ? 0 : version) != versionOf(user)) {
            throw new JwtException("Refresh token sudah dicabut");
        }
        return user;
    }

    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private User loadUser(String email) {
        if (email == null) {
            throw new JwtException("Token tidak memiliki subject");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new JwtException("User token tidak ditemukan"));
    }

    private int versionOf(User user) {
        return user.getTokenVersion() == null ? 0 : user.getTokenVersion();
    }

    private boolean isExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration == null || expiration.before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(getSignKey()).build()
                .parseSignedClaims(token).getPayload();
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(parseClaims(token));
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
