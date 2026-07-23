package com.medikasentosa.features.jadwaloperasi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medikasentosa.features.jadwaloperasi.entity.StatusOperasi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO respons data jadwal operasi dalam bentuk datar (flat).
 *
 * @author Ari
 * @since 1.0.0
 */
public record JadwalOperasiResponse(
        Long id, Long pasienId, String namaPasien,
        Long dokterId, String namaDokter, String namaPoli,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate tanggalOperasi,
        @JsonFormat(pattern = "HH:mm") LocalTime jamMulai,
        String jenisOperasi, String ruangOperasi,
        StatusOperasi status, String catatan,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt) {
}