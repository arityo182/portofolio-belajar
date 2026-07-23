package com.medikasentosa.features.jadwaloperasi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO permintaan untuk membuat jadwal operasi baru (FR-31).
 *
 * @author Ari
 * @since 1.0.0
 */
public record JadwalOperasiRequest(

        @NotNull(message = "Pasien wajib diisi") Long pasienId,

        @NotNull(message = "Dokter wajib diisi") Long dokterId,

        @NotNull(message = "Tanggal operasi wajib diisi")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate tanggalOperasi,

        @NotNull(message = "Jam mulai wajib diisi")
        @JsonFormat(pattern = "HH:mm") LocalTime jamMulai,

        @NotBlank(message = "Jenis operasi wajib diisi") String jenisOperasi,

        @NotBlank(message = "Ruang operasi wajib diisi") String ruangOperasi,

        String catatan) {
}