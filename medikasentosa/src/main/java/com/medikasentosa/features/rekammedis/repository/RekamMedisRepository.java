package com.medikasentosa.features.rekammedis.repository;

import com.medikasentosa.features.rekammedis.entity.RekamMedis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository akses data untuk entity {@link RekamMedis}.
 * Menyediakan kueri turunan untuk mengambil rekam medis per pasien maupun per appointment,
 * serta mengecek keberadaan rekam medis sebuah appointment.
 *
 * @author Ari
 * @since 1.0.0
 */
public interface RekamMedisRepository extends JpaRepository<RekamMedis, Long> {

    /**
     * Mengambil seluruh rekam medis milik seorang pasien.
     *
     * @param pasienId ID pasien
     * @return daftar rekam medis pasien tersebut
     */
    List<RekamMedis> findByPasienId(Long pasienId);

    /**
     * Mengambil seluruh rekam medis yang dibuat oleh seorang dokter.
     *
     * @param dokterId ID dokter
     * @return daftar rekam medis yang dibuat dokter tersebut
     */
    List<RekamMedis> findByDokterIdOrderByCreatedAtDesc(Long dokterId);

    /**
     * Mengecek apakah sebuah appointment sudah memiliki rekam medis.
     *
     * @param appointmentId ID appointment
     * @return {@code true} bila rekam medis sudah ada
     */
    boolean existsByAppointmentId(Long appointmentId);

    /**
     * Mengambil rekam medis milik sebuah appointment.
     *
     * @param appointmentId ID appointment
     * @return rekam medis bila ada, {@link Optional#empty()} bila belum dibuat
     */
    Optional<RekamMedis> findByAppointmentId(Long appointmentId);
}
