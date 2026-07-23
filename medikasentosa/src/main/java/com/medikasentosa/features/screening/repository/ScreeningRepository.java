package com.medikasentosa.features.screening.repository;

import com.medikasentosa.features.screening.entity.Screening;
import com.medikasentosa.features.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    // ambil riwayat skrining milik user tertentu, terbaru di atas
    List<Screening> findByUserOrderByCreatedAtDesc(User user);
}