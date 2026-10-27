package com.medikasentosa.features.jadwaldokter.repository;

import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.jadwaldokter.entity.Hari;
import com.medikasentosa.features.jadwaldokter.entity.JadwalDokter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface JadwalDokterRepository extends JpaRepository<JadwalDokter, Long> {

    List<JadwalDokter> findByDokter(Dokter dokter);

    List<JadwalDokter> findByDokterAndIsActiveTrue(Dokter dokter);

    List<JadwalDokter> findByIsActiveTrue();

    /**
     * Mengambil satu jadwal dengan row lock (SELECT ... FOR UPDATE).
     * Dipakai untuk menyerialkan pembuatan appointment/antrian pada jadwal yang
     * sama sehingga kuota dan nomor urut tidak bentrok saat request bersamaan.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from JadwalDokter j where j.id = :id")
    Optional<JadwalDokter> findByIdForUpdate(@Param("id") Long id);

    /**
     * Mengecek jadwal aktif lain milik dokter yang sama pada hari yang sama
     * dengan rentang jam yang beririsan.
     *
     * @param dokterId  ID dokter
     * @param hari      hari praktik
     * @param jamMulai  jam mulai jadwal baru
     * @param jamSelesai jam selesai jadwal baru
     * @param excludeId ID jadwal yang sedang diperbarui (null saat create)
     * @return {@code true} bila ada jadwal yang beririsan
     */
    @Query("""
            select case when count(j) > 0 then true else false end from JadwalDokter j
            where j.dokter.id = :dokterId
              and j.hari = :hari
              and j.isActive = true
              and j.jamMulai < :jamSelesai
              and j.jamSelesai > :jamMulai
              and (:excludeId is null or j.id <> :excludeId)
            """)
    boolean existsOverlappingSchedule(@Param("dokterId") Long dokterId,
                                      @Param("hari") Hari hari,
                                      @Param("jamMulai") LocalTime jamMulai,
                                      @Param("jamSelesai") LocalTime jamSelesai,
                                      @Param("excludeId") Long excludeId);
}
