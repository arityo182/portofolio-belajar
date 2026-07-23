package com.medikasentosa.features.antrian.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medikasentosa.features.appointment.entity.StatusAppointment;

import java.time.LocalDate;

/**
 * DTO respons data antrian yang dikembalikan ke klien.
 * Menggabungkan informasi ringkas appointment terkait (pasien, dokter, tanggal,
 * dan status) agar mudah ditampilkan pada papan antrian.
 *
 * @author Ari
 * @since 1.0.0
 */
public record AntrianResponse(
        Long id, Long appointmentId, Integer nomorUrut,
        String namaPasien, String namaDokter, String namaPoli,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate tanggal,
        StatusAppointment status) {
}
