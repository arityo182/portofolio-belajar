package com.medikasentosa.features.laboratorium.repository;

import com.medikasentosa.features.laboratorium.entity.LabHasil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository akses data untuk entity {@link LabHasil}.
 *
 * @author Ari
 * @since 1.0.0
 */
public interface LabHasilRepository extends JpaRepository<LabHasil, Long> {

    /**
     * Mengambil hasil laboratorium berdasarkan ID order.
     *
     * @param labOrderId ID order laboratorium
     * @return hasil lab bila ada, {@link Optional#empty()} bila belum diisi
     */
    Optional<LabHasil> findByLabOrderId(Long labOrderId);

    /**
     * Mengecek apakah sebuah order laboratorium sudah memiliki hasil.
     *
     * @param labOrderId ID order laboratorium
     * @return {@code true} bila hasil sudah ada
     */
    boolean existsByLabOrderId(Long labOrderId);
}