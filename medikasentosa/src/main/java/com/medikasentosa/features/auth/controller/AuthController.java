package com.medikasentosa.features.auth.controller;

import com.medikasentosa.features.auth.dto.AuthResponse;
import com.medikasentosa.features.auth.dto.LoginRequest;
import com.medikasentosa.features.auth.dto.RegisterRequest;
import com.medikasentosa.features.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller untuk manajemen autentikasi pengguna.
 * Menangani registrasi akun baru dan proses login yang mengembalikan token JWT.
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API registrasi & login pengguna")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Mendaftarkan pengguna baru dan mengembalikan token JWT beserta data user.
     *
     * @param request data registrasi (nama, email, password)
     * @return data user yang berhasil dibuat beserta token JWT (HTTP 201)
     */
    @PostMapping("/register")
    @Operation(summary = "Daftar pengguna baru",
               description = "Membuat akun baru dengan email unik dan mengembalikan token JWT.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User berhasil dibuat"),
        @ApiResponse(responseCode = "400", description = "Data registrasi tidak valid"),
        @ApiResponse(responseCode = "409", description = "Email sudah terdaftar")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201
    }

    /**
     * Memproses login pengguna dan mengembalikan token JWT bila kredensial valid.
     *
     * @param request kredensial login (email, password)
     * @return data user beserta token JWT (HTTP 200)
     */
    @PostMapping("/login")
    @Operation(summary = "Login pengguna",
               description = "Memvalidasi email & password lalu mengembalikan token JWT.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login berhasil"),
        @ApiResponse(responseCode = "400", description = "Data login tidak valid"),
        @ApiResponse(responseCode = "404", description = "Email atau password salah")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response); // 200
    }
}