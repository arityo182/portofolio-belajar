package com.medikasentosa.features.resep.repository;

import com.medikasentosa.features.resep.entity.Resep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository akses data untuk entity {@link Resep}.
 * Menyediakan kueri turunan untuk mengambil seluruh resep milik sebuah rekam medis.
 *
 * @author Ari
 * @since 1.0.0
 */
public interface ResepRepository extends JpaRepository<Resep, Long> {

    /**
     * Mengambil seluruh resep milik sebuah rekam medis.
     *
     * @param rekamMedisId ID rekam medis
     * @return daftar resep rekam medis tersebut
     */
    List<Resep> findByRekamMedisId(Long rekamMedisId);
}
