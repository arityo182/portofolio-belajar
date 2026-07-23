package com.medikasentosa.features.jadwaldokter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medikasentosa.features.jadwaldokter.entity.Hari;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

/**
 * DTO permintaan untuk membuat atau memperbarui jadwal praktik dokter.
 *
 * @author Ari
 * @since 1.0.0
 */
public record JadwalDokterRequest(

        @NotNull(message = "Dokter wajib diisi") Long dokterId,

        @NotNull(message = "Hari wajib diisi") Hari hari,

        @NotNull(message = "Jam mulai wajib diisi")
        @JsonFormat(pattern = "HH:mm:ss") LocalTime jamMulai,

        @NotNull(message = "Jam selesai wajib diisi")
        @JsonFormat(pattern = "HH:mm:ss") LocalTime jamSelesai,

        @NotNull(message = "Kuota wajib diisi")
        @Min(value = 1, message = "Kuota minimal 1") Integer kuota) {
}
