package com.medikasentosa.features.antrian.repository;

import com.medikasentosa.features.antrian.entity.Antrian;
import com.medikasentosa.features.jadwaldokter.entity.JadwalDokter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository akses data untuk entity {@link Antrian}.
 * Menyediakan kueri turunan untuk mengecek keberadaan antrian sebuah appointment
 * serta menghitung antrian existing pada jadwal dan tanggal tertentu (untuk
 * menentukan nomor urut berikutnya).
 *
 * @author Ari
 * @since 1.0.0
 */
public interface AntrianRepository extends JpaRepository<Antrian, Long> {

    /**
     * Mengecek apakah sebuah appointment sudah memiliki antrian.
     *
     * @param appointmentId ID appointment
     * @return {@code true} bila antrian sudah ada
     */
    boolean existsByAppointmentId(Long appointmentId);

    /**
     * Mengambil antrian milik sebuah appointment.
     *
     * @param appointmentId ID appointment
     * @return antrian bila ada, {@link Optional#empty()} bila belum digenerate
     */
    Optional<Antrian> findByAppointmentId(Long appointmentId);

    /**
     * Menghitung jumlah antrian yang sudah ada pada jadwal dan tanggal appointment
     * tertentu (dipakai untuk menentukan nomor urut antrian berikutnya).
     *
     * @param jadwal  jadwal praktik dari appointment terkait
     * @param tanggal tanggal appointment terkait
     * @return jumlah antrian yang cocok
     */
    long countByAppointmentJadwalAndAppointmentTanggal(JadwalDokter jadwal, LocalDate tanggal);
}
