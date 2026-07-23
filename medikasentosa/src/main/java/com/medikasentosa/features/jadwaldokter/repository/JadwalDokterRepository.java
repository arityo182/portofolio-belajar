package com.medikasentosa.features.jadwaldokter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.jadwaldokter.entity.JadwalDokter;

import java.util.List;

public interface JadwalDokterRepository extends JpaRepository<JadwalDokter, Long> {

    List<JadwalDokter> findByDokter(Dokter dokter);

    List<JadwalDokter> findByDokterAndIsActiveTrue(Dokter dokter);
}
