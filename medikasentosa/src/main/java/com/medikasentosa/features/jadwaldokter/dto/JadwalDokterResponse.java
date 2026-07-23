package com.medikasentosa.features.jadwaldokter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medikasentosa.features.jadwaldokter.entity.Hari;

import java.time.LocalTime;

/**
 * DTO respons data jadwal praktik dokter yang dikembalikan ke klien.
 *
 * @author Ari
 * @since 1.0.0
 */
public record JadwalDokterResponse(
        Long id,
        Long dokterId,
        String namaDokter,
        Hari hari,
        @JsonFormat(pattern = "HH:mm:ss") LocalTime jamMulai,
        @JsonFormat(pattern = "HH:mm:ss") LocalTime jamSelesai,
        Integer kuota,
        Boolean isActive) {

}
