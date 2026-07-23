package com.medikasentosa.features.rawatinap.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO permintaan untuk mencatat pasien masuk rawat inap.
 *
 * @author Ari
 * @since 1.0.0
 */
public record RawatInapRequest(

        @NotNull(message = "Pasien wajib diisi") Long pasienId,

        @NotNull(message = "Dokter wajib diisi") Long dokterId,

        @NotNull(message = "Kamar wajib diisi") Long kamarId,

        @NotNull(message = "Tanggal masuk wajib diisi")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate tanggalMasuk,

        @NotBlank(message = "Diagnosa awal wajib diisi") String diagnosaAwal,

        String catatan) {
}