package com.medikasentosa.features.dokter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.poli.entity.Poli;

import java.util.List;

public interface DokterRepository extends JpaRepository<Dokter, Long> {

    List<Dokter> findByIsActiveTrue();

    List<Dokter> findByPoliAndIsActiveTrue(Poli poli);

    boolean existsByUserId(Long userId);

    // Ambil dokter berdasarkan ID user (untuk panel dokter)
    java.util.Optional<com.medikasentosa.features.dokter.entity.Dokter> findByUserId(Long userId);
}
