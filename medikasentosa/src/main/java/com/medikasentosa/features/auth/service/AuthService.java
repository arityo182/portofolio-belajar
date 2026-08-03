package com.medikasentosa.features.auth.service;

import com.medikasentosa.features.auth.dto.*;
import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import com.medikasentosa.features.auth.repository.UserRepository;
import com.medikasentosa.shared.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private JwtService jwtService;

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
        log.info("Registering new user with email: {}", request.email());

        // Validasi cek email sudah dipakai atau belum
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed: email {} already exists", request.email());
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
     * @throws ResourceNotFoundException bila email tidak ditemukan atau password salah
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        // Validasi email apakah sudah ada atau belum
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Email atau password salah!"));

        // Validasi apakah password cocok
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Login failed: invalid password for email {}", request.email());
            throw new ResourceNotFoundException("Email atau password salah");
        }

        // Validasi akun masih aktif
        if (Boolean.FALSE.equals(user.getIsActive())) {
            log.warn("Login failed: account disabled for email {}", request.email());
            throw new ResourceNotFoundException("Akun Anda dinonaktifkan. Hubungi admin.");
        }

        // Generate token
        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token, toUserResponse(user));
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
