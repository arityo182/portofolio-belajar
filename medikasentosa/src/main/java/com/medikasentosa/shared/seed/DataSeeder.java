package com.medikasentosa.shared.seed;

import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inisialisasi akun admin awal saat startup bila belum ada akun ADMIN.
 *
 * <p>Password admin diambil dari environment {@code ADMIN_PASSWORD}. Di profil
 * production variabel ini wajib diisi dan mengikuti kebijakan password
 * (minimal 12 karakter, mengandung huruf dan angka). Di development, bila
 * variabel tidak diisi, akun admin tidak dibuat dan aplikasi tetap berjalan —
 * tidak ada password default yang diketahui publik.</p>
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String ADMIN_EMAIL = "admin@medikasentosa.id";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      Environment environment) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        boolean production = environment.acceptsProfiles(Profiles.of("production", "prod"));

        // Jangan minta ADMIN_PASSWORD pada setiap restart bila admin sudah ada.
        if (userRepository.countByRole(Role.ADMIN) > 0) {
            return;
        }

        String adminPassword = System.getenv("ADMIN_PASSWORD");

        if (adminPassword == null || adminPassword.isBlank()) {
            if (production) {
                throw new IllegalStateException(
                        "ADMIN_PASSWORD wajib diisi di production untuk membuat akun admin awal");
            }
            log.warn("[DataSeeder] ADMIN_PASSWORD tidak diisi — akun admin awal tidak dibuat.");
            return;
        }

        if (adminPassword.length() < 12
                || !adminPassword.matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
            throw new IllegalStateException(
                    "ADMIN_PASSWORD minimal 12 karakter serta mengandung huruf dan angka");
        }

        // Buat admin default hanya jika belum ada user dengan role ADMIN
        User admin = User.builder()
                .nama("Administrator")
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .isActive(true)
                .build();
        userRepository.save(admin);
        log.info("[DataSeeder] Akun admin awal dibuat: {}", ADMIN_EMAIL);
        log.warn("[DataSeeder] Segera ganti password admin melalui alur reset password.");
    }
}
