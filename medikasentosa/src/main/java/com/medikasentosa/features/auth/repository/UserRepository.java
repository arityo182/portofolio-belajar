package com.medikasentosa.features.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // Untuk login
    Optional<User> findByEmail(String email);

    // untuk cek saat register
    boolean existsByEmail(String email);

    // Ambil semua user berdasarkan role (untuk admin kelola pengguna)
    List<User> findByRole(Role role);

    // Hitung jumlah user per role (untuk dashboard admin)
    long countByRole(Role role);
}