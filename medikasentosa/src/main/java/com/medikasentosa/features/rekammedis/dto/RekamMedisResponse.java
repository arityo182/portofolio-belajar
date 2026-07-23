package com.medikasentosa.features.rekammedis.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO respons data rekam medis dalam bentuk datar (flat) yang dikembalikan ke klien.
 * Menggabungkan informasi appointment, pasien, dan dokter agar mudah ditampilkan.
 *
 * @author Ari
 * @since 1.0.0
 */
public record RekamMedisResponse(
        Long id,
        Long appointmentId,
        Long pasienId,
        String namaPasien,
        String noRekamMedis,
        Long dokterId,
        String namaDokter,
        String diagnosa,
        String tindakan,
        String catatan,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate tanggal,
        LocalDateTime createdAt) {
}
