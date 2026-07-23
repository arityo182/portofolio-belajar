package com.medikasentosa.features.radiologi.repository;

import com.medikasentosa.features.radiologi.entity.Radiologi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository akses data untuk entity {@link Radiologi}.
 *
 * @author Ari
 * @since 1.0.0
 */
public interface RadiologiRepository extends JpaRepository<Radiologi, Long> {

    /**
     * Mengambil seluruh order radiologi milik sebuah rekam medis.
     *
     * @param rekamMedisId ID rekam medis
     * @return daftar order radiologi rekam medis tersebut
     */
    List<Radiologi> findByRekamMedisId(Long rekamMedisId);
}