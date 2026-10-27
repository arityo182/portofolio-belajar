package com.medikasentosa.features.auth.service;

import com.medikasentosa.features.auth.dto.*;
import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import com.medikasentosa.features.auth.repository.UserRepository;
import com.medikasentosa.shared.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service untuk logika bisnis autentikasi pengguna.
 * Bertanggung jawab atas registrasi user baru, proses login,
 * hashing password, dan pembuatan token JWT.
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /**
     * Hash BCrypt dummy untuk menyamakan waktu respons saat email tidak ada,
     * sehingga penyerang tidak dapat melakukan enumerasi akun lewat timing.
     */
    private static final String DUMMY_BCRYPT =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Mendaftarkan pengguna baru: memvalidasi keunikan email, meng-hash password,
     * menyimpan user dengan role PASIEN, lalu membuat token JWT.
     *
     * @param request data registrasi (nama, email, password)
     * @return token JWT beserta data user yang baru dibuat
     * @throws DuplicateResourceException bila email sudah terdaftar
     */
    // Register
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user");

        // Validasi cek email sudah dipakai atau belum
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed: email already exists");
            throw new DuplicateResourceException("Email sudah terdaftar");
        }

        // Membuat user
        User user = User.builder().nama(request.nama())
                .email(request.email()).password(passwordEncoder.encode(request.password()))
                .role(Role.PASIEN).isActive(true).build();

        userRepository.save(user);

        // Generate token setelah berhasil save user
        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token, toUserResponse(user));
    }

    /**
     * Memproses login: mencari user berdasarkan email dan memverifikasi password,
     * kemudian membuat token JWT bila kredensial cocok.
     *
     * @param request kredensial login (email, password)
     * @return token JWT beserta data user
     * @throws BadCredentialsException bila email tidak ditemukan atau password salah
     * @throws DisabledException       bila akun dinonaktifkan
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt");

        // Validasi email apakah sudah ada atau belum. Tetap jalankan BCrypt
        // dummy saat email tidak ada agar waktu respons tidak membocorkan
        // keberadaan akun.
        User user = userRepository.findByEmail(request.email()).orElse(null);
        if (user == null) {
            passwordEncoder.matches(request.password(), DUMMY_BCRYPT);
            log.warn("Login failed: unknown email");
            throw new BadCredentialsException("Email atau password salah");
        }

        // Validasi apakah password cocok
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Login failed: invalid password");
            throw new BadCredentialsException("Email atau password salah");
        }

        // Validasi akun masih aktif
        if (Boolean.FALSE.equals(user.getIsActive())) {
            log.warn("Login failed: account disabled");
            throw new DisabledException("Akun Anda dinonaktifkan. Hubungi admin.");
        }

        // Generate token
        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token, toUserResponse(user));
    }

    /** Membuat refresh token untuk email pemilik sesi. */
    public String createRefreshToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
        return jwtService.generateRefreshToken(user);
    }

    /**
     * Menukar refresh token (dari cookie HttpOnly) menjadi access token baru.
     * Refresh token dirotasi oleh controller.
     */
    public AuthResponse refresh(String refreshToken) {
        User user = jwtService.validateRefreshToken(refreshToken);
        return new AuthResponse(jwtService.generateAccessToken(user), toUserResponse(user));
    }

    /**
     * Logout: mencabut seluruh token user dengan menaikkan token version.
     * Token yang tidak valid diabaikan (cookie tetap dihapus oleh controller).
     */
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        try {
            User user = jwtService.validateRefreshToken(refreshToken);
            revokeAllSessions(user);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Logout dengan refresh token tidak valid diabaikan");
        }
    }

    /** Menaikkan token version sehingga semua access & refresh token lama invalid. */
    @Transactional
    public void revokeAllSessions(User user) {
        int current = user.getTokenVersion() == null ? 0 : user.getTokenVersion();
        user.setTokenVersion(current + 1);
        userRepository.save(user);
    }

    /** Profil user yang sedang login. */
    public UserResponse me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
        return toUserResponse(user);
    }

    public String createRefreshToken(User user) {
        return jwtService.generateRefreshToken(user);
    }

    // Helper user entity userresponse
    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getNama(),
                user.getEmail(),
                user.getRole());
    }
}
