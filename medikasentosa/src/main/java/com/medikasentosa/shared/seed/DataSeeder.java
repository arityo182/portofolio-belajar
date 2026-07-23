package com.medikasentosa.shared.seed;

import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inisialisasi data awal sistem — membuat akun admin default saat startup
 * bila belum ada akun admin di database.
 *
 * Akun admin default:
 * - Email: admin@medikasentosa.id
 * - Password: admin123
 *
 * Ganti password ini segera setelah deploy ke production!
 *
 * @author Ari
 * @since 1.0.0
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Buat admin default hanya jika belum ada user dengan role ADMIN
        if (userRepository.countByRole(Role.ADMIN) == 0) {
            User admin = User.builder()
                    .nama("Administrator")
                    .email("admin@medikasentosa.id")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
            System.out.println("[DataSeeder] Akun admin default dibuat: admin@medikasentosa.id / admin123");
            System.out.println("[DataSeeder] !!! GANTI PASSWORD INI SEGERA DI PRODUCTION !!!");
        }
    }
}
