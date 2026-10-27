package com.medikasentosa.features.auth.controller;

import com.medikasentosa.features.auth.dto.AuthResponse;
import com.medikasentosa.features.auth.dto.LoginRequest;
import com.medikasentosa.features.auth.dto.RegisterRequest;
import com.medikasentosa.features.auth.dto.UserResponse;
import com.medikasentosa.features.auth.service.AuthService;
import com.medikasentosa.shared.exception.InvalidTokenException;
import com.medikasentosa.shared.security.ClientIpResolver;
import com.medikasentosa.shared.security.JwtService;
import com.medikasentosa.shared.security.LoginRateLimiter;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * Controller autentikasi.
 *
 * <p>Access token (Bearer) berumur pendek dikembalikan di body dan disimpan
 * frontend hanya di memori. Refresh token dikirim sebagai cookie HttpOnly,
 * Secure (di produksi), SameSite=Lax, sehingga tidak dapat dibaca JavaScript.</p>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API registrasi, login, refresh, dan logout")
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter rateLimiter;
    private final JwtService jwtService;

    @Value("${app.auth.refresh-cookie-name:medika_refresh}")
    private String refreshCookieName;

    @Value("${app.auth.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${app.auth.cookie-same-site:Lax}")
    private String cookieSameSite;

    public AuthController(AuthService authService, LoginRateLimiter rateLimiter, JwtService jwtService) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @Operation(summary = "Daftar pengguna baru",
               description = "Membuat akun baru dengan email unik. Mengembalikan access token "
                       + "dan memasang refresh token pada cookie HttpOnly.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User berhasil dibuat"),
        @ApiResponse(responseCode = "400", description = "Data registrasi tidak valid"),
        @ApiResponse(responseCode = "409", description = "Email sudah terdaftar")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 HttpServletResponse response) {
        AuthResponse auth = authService.register(request);
        issueRefreshCookie(response, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(auth);
    }

    @PostMapping("/login")
    @Operation(summary = "Login pengguna",
               description = "Memvalidasi email & password lalu mengembalikan access token. "
                       + "Refresh token dipasang sebagai cookie HttpOnly.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login berhasil"),
        @ApiResponse(responseCode = "400", description = "Data login tidak valid"),
        @ApiResponse(responseCode = "401", description = "Email atau password salah / akun nonaktif"),
        @ApiResponse(responseCode = "429", description = "Terlalu banyak percobaan login")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest,
                                              HttpServletResponse response) {
        String key = clientKey(httpRequest, request.email());
        rateLimiter.checkAllowed(key);
        try {
            AuthResponse auth = authService.login(request);
            rateLimiter.reset(key);
            issueRefreshCookie(response, auth);
            return ResponseEntity.ok(auth);
        } catch (RuntimeException e) {
            rateLimiter.recordFailure(key);
            throw e;
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Tukar refresh token menjadi access token baru",
               description = "Membaca refresh token dari cookie HttpOnly, memvalidasi token version, "
                       + "lalu merotasi refresh token dan mengembalikan access token baru.")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "${app.auth.refresh-cookie-name:medika_refresh}", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidTokenException("Refresh token tidak tersedia");
        }
        try {
            AuthResponse auth = authService.refresh(refreshToken);
            issueRefreshCookie(response, auth);
            return ResponseEntity.ok(auth);
        } catch (JwtException | IllegalArgumentException e) {
            clearRefreshCookie(response);
            throw new InvalidTokenException("Refresh token tidak valid atau sudah kedaluwarsa");
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout dan cabut seluruh sesi",
               description = "Menaikkan token version user sehingga semua access/refresh token "
                       + "sebelumnya tidak valid, lalu menghapus cookie refresh.")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "${app.auth.refresh-cookie-name:medika_refresh}", required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(refreshToken);
        clearRefreshCookie(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Profil user yang sedang login")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.me(authentication.getName()));
    }

    // ─── Cookie helpers ───────────────────────────────────────────────────

    private void issueRefreshCookie(HttpServletResponse response, AuthResponse auth) {
        String refreshToken = authService.createRefreshToken(auth.user().email());
        ResponseCookie cookie = ResponseCookie.from(refreshCookieName, refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/api/auth")
                .maxAge(Duration.ofMillis(jwtService.getRefreshExpirationMs()))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String clientKey(HttpServletRequest request, String email) {
        String ip = ClientIpResolver.resolve(request);
        return ip + "|" + (email == null ? "" : email.toLowerCase());
    }
}
