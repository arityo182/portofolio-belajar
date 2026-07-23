package com.medikasentosa.features.appointment.repository;

import com.medikasentosa.features.appointment.entity.Appointment;
import com.medikasentosa.features.appointment.entity.StatusAppointment;
import com.medikasentosa.features.jadwaldokter.entity.JadwalDokter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository akses data untuk entity {@link Appointment}.
 * Menyediakan kueri turunan untuk validasi kuota, duplikasi booking,
 * serta pengambilan appointment berdasarkan pasien maupun jadwal.
 *
 * @author Ari
 * @since 1.0.0
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Menghitung jumlah appointment pada sebuah jadwal di tanggal tertentu
     * (dipakai untuk validasi kuota).
     *
     * @param jadwal  jadwal praktik yang dihitung
     * @param tanggal tanggal janji temu
     * @return jumlah appointment yang cocok
     */
    long countByJadwalAndTanggal(JadwalDokter jadwal, LocalDate tanggal);

    /**
     * Mengecek apakah seorang pasien sudah memiliki appointment pada jadwal
     * dan tanggal yang sama (dipakai untuk mencegah duplikasi booking).
     *
     * @param pasienId ID pasien
     * @param jadwal   jadwal praktik
     * @param tanggal  tanggal janji temu
     * @return {@code true} bila sudah ada appointment yang cocok
     */
    boolean existsByPasienIdAndJadwalAndTanggal(Long pasienId, JadwalDokter jadwal, LocalDate tanggal);

    /**
     * Mengambil seluruh appointment milik seorang pasien.
     *
     * @param pasienId ID pasien
     * @return daftar appointment pasien tersebut
     */
    List<Appointment> findByPasienId(Long pasienId);

    // Ambil semua appointment milik seorang dokter (untuk panel dokter)
    List<Appointment> findByDokterId(Long dokterId);

    // Ambil semua appointment milik seorang dokter pada tanggal tertentu (untuk panel dokter hari ini)
    List<Appointment> findByDokterIdAndTanggalOrderByCreatedAtAsc(Long dokterId, LocalDate tanggal);

    // Ambil semua appointment milik seorang dokter dengan status tertentu
    List<Appointment> findByDokterIdAndStatusOrderByCreatedAtAsc(Long dokterId, StatusAppointment status);

    /**
     * Mengambil seluruh appointment pada jadwal dan tanggal tertentu,
     * terurut berdasarkan waktu pembuatan menaik (untuk penomoran antrian).
     *
     * @param jadwal  jadwal praktik
     * @param tanggal tanggal janji temu
     * @return daftar appointment terurut dari yang paling awal dibuat
     */
    List<Appointment> findByJadwalAndTanggalOrderByCreatedAtAsc(JadwalDokter jadwal, LocalDate tanggal);
}
