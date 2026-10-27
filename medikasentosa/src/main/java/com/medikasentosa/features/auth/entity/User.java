package com.medikasentosa.features.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Entity yang merepresentasikan pengguna (user) sistem RS Medika Sentosa.
 * Dipetakan ke tabel {@code users} dan menyimpan kredensial serta peran pengguna.
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "users")   // "user" kadang reserved word di Postgres, pakai "users"
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;   // disimpan dalam bentuk hash (BCrypt), bukan plain

    @Enumerated(EnumType.STRING)   // simpan "PASIEN" sebagai teks, bukan angka
    @Column(nullable = false)
    private Role role;

    /** Status aktif akun; `false` = akun dinonaktifkan admin (tidak bisa login). */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Versi token sesi. Dinaikkan saat logout, reset password, atau akun
     * dinonaktifkan sehingga seluruh access/refresh token lama langsung invalid.
     */
    @Builder.Default
    @Column(name = "token_version")
    private Integer tokenVersion = 0;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PostLoad
    void ensureTokenVersion() {
        if (this.tokenVersion == null) {
            this.tokenVersion = 0;
        }
    }

    @PrePersist
    void onCreate() {
        // Pastikan tokenVersion terisi untuk baris lama/baru, lalu set waktu buat.
        if (this.tokenVersion == null) {
            this.tokenVersion = 0;
        }
        this.createdAt = Instant.now();
    }
}