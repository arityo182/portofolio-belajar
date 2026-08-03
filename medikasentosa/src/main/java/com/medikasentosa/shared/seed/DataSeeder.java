package com.medikasentosa.shared.seed;

import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String adminPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "admin123");

        // Buat admin default hanya jika belum ada user dengan role ADMIN
        if (userRepository.countByRole(Role.ADMIN) == 0) {
            User admin = User.builder()
                    .nama("Administrator")
                    .email("admin@medikasentosa.id")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
            log.info("[DataSeeder] Akun admin default dibuat: admin@medikasentosa.id");
            log.warn("[DataSeeder] !!! GANTI PASSWORD INI SEGERA DI PRODUCTION !!!");
        }
    }
}
