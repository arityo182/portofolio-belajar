package com.medikasentosa.features.appointment.repository;

import com.medikasentosa.features.appointment.entity.Appointment;
import com.medikasentosa.features.appointment.entity.StatusAppointment;
import com.medikasentosa.features.jadwaldokter.entity.JadwalDokter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
     * Menghitung jumlah appointment aktif (bukan BATAL) pada sebuah jadwal di
     * tanggal tertentu. Appointment yang dibatalkan tidak boleh menghabiskan
     * kuota sehingga slotnya dapat dipesan pasien lain.
     *
     * @param jadwal  jadwal praktik yang dihitung
     * @param tanggal tanggal janji temu
     * @return jumlah appointment aktif yang cocok
     */
    @Query("""
            select count(a) from Appointment a
            where a.jadwal = :jadwal
              and a.tanggal = :tanggal
              and a.status <> com.medikasentosa.features.appointment.entity.StatusAppointment.BATAL
            """)
    long countActiveByJadwalAndTanggal(@Param("jadwal") JadwalDokter jadwal,
                                       @Param("tanggal") LocalDate tanggal);

    /**
     * Mengecek apakah seorang pasien sudah memiliki appointment aktif (bukan
     * BATAL) pada jadwal dan tanggal yang sama. Appointment yang dibatalkan
     * tidak menghalangi pasien untuk memesan ulang.
     *
     * @param pasienId ID pasien
     * @param jadwal   jadwal praktik
     * @param tanggal  tanggal janji temu
     * @return {@code true} bila sudah ada appointment aktif yang cocok
     */
    @Query("""
            select case when count(a) > 0 then true else false end from Appointment a
            where a.pasien.id = :pasienId
              and a.jadwal = :jadwal
              and a.tanggal = :tanggal
              and a.status <> com.medikasentosa.features.appointment.entity.StatusAppointment.BATAL
            """)
    boolean existsActiveByPasienIdAndJadwalAndTanggal(@Param("pasienId") Long pasienId,
                                                      @Param("jadwal") JadwalDokter jadwal,
                                                      @Param("tanggal") LocalDate tanggal);

    /**
     * Mengambil seluruh appointment milik seorang pasien.
     *
     * @param pasienId ID pasien
     * @return daftar appointment pasien tersebut
     */
    List<Appointment> findByPasienId(Long pasienId);

    /** Apakah dokter pernah menangani pasien ini (dasar akses clinical). */
    boolean existsByPasienIdAndDokterId(Long pasienId, Long dokterId);

    Page<Appointment> findByPasienId(Long pasienId, Pageable pageable);

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
