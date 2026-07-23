package com.medikasentosa.features.laboratorium.repository;

import com.medikasentosa.features.laboratorium.entity.LabOrder;
import com.medikasentosa.features.laboratorium.entity.StatusLab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository akses data untuk entity {@link LabOrder}.
 *
 * @author Ari
 * @since 1.0.0
 */
public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {

    /**
     * Mengambil seluruh order laboratorium milik sebuah rekam medis.
     *
     * @param rekamMedisId ID rekam medis
     * @return daftar order lab rekam medis tersebut
     */
    List<LabOrder> findByRekamMedisId(Long rekamMedisId);

    /**
     * Mengambil seluruh order laboratorium dengan status tertentu.
     *
     * @param status status yang difilter
     * @return daftar order lab dengan status tersebut
     */
    List<LabOrder> findByStatus(StatusLab status);
}