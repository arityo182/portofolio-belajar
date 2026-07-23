package com.medikasentosa.features.pasien.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medikasentosa.features.pasien.entity.Pasien;

import java.util.Optional;

public interface PasienRepository extends JpaRepository<Pasien, Long> {

    Optional<Pasien> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByNoRekamMedis(String noRekamMedis);
}
