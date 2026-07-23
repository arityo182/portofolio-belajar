package com.medikasentosa.features.jadwaloperasi.repository;

import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.jadwaloperasi.entity.JadwalOperasi;
import com.medikasentosa.features.jadwaloperasi.entity.StatusOperasi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository akses data untuk entity {@link JadwalOperasi}.
 *
 * @author Ari
 * @since 1.0.0
 */
public interface JadwalOperasiRepository extends JpaRepository<JadwalOperasi, Long> {

    List<JadwalOperasi> findByPasienId(Long pasienId);

    List<JadwalOperasi> findByDokter(Dokter dokter);

    List<JadwalOperasi> findByTanggalOperasi(LocalDate tanggal);

    List<JadwalOperasi> findByStatus(StatusOperasi status);
}