package com.medikasentosa.features.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medikasentosa.features.appointment.entity.StatusAppointment;
import com.medikasentosa.features.jadwaldokter.entity.Hari;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO respons data janji temu (appointment) dalam bentuk datar (flat) yang
 * dikembalikan ke klien. Menggabungkan informasi pasien, dokter, poli, dan jadwal
 * agar mudah ditampilkan tanpa perlu request tambahan.
 * <p>
 * Field {@code nomorAntrian} bernilai {@code null} bila antrian untuk appointment
 * ini belum digenerate.
 *
 * @author Ari
 * @since 1.0.0
 */
public record AppointmentResponse(
        Long id,
        Long pasienId,
        String namaPasien,
        String noRekamMedis,
        Long dokterId,
        String namaDokter,
        String namaPoli,
        Long jadwalId,
        Hari hari,
        @JsonFormat(pattern = "HH:mm:ss") LocalTime jamMulai,
        @JsonFormat(pattern = "HH:mm:ss") LocalTime jamSelesai,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate tanggal,
        StatusAppointment status,
        String keluhan,
        Integer nomorAntrian) {
}
