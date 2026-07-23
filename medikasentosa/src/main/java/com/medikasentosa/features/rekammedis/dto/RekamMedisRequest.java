package com.medikasentosa.features.rekammedis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO permintaan untuk membuat rekam medis.
 * Pasien dan dokter tidak diikutkan karena diturunkan otomatis dari appointment terkait.
 *
 * @author Ari
 * @since 1.0.0
 */
public record RekamMedisRequest(

        @NotNull(message = "Appointment wajib diisi") Long appointmentId,

        @NotBlank(message = "Diagnosa wajib diisi") String diagnosa,

        @NotBlank(message = "Tindakan wajib diisi") String tindakan,

        String catatan) {
}
