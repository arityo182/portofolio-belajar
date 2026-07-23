package com.medikasentosa.features.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO permintaan untuk membuat janji temu (appointment).
 * Dokter tidak diikutkan karena diturunkan otomatis dari jadwal yang dipilih.
 *
 * @author Ari
 * @since 1.0.0
 */
public record AppointmentRequest(

        @NotNull(message = "Pasien wajib diisi") Long pasienId,

        @NotNull(message = "Jadwal wajib diisi") Long jadwalId,

        @NotNull(message = "Tanggal wajib diisi")
        @FutureOrPresent(message = "Tanggal tidak boleh di masa lalu")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate tanggal,

        String keluhan) {
}
