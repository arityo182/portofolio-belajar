package com.medikasentosa.features.pasien.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medikasentosa.features.pasien.entity.JenisKelamin;

import java.time.LocalDate;

/**
 * DTO respons data pasien yang dikembalikan ke klien.
 *
 * @author Ari
 * @since 1.0.0
 */
public record PasienResponse(
        Long id,
        Long userId,
        String nama,
        String email,
        String noRekamMedis,
        String nik,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate tanggalLahir,
        JenisKelamin jenisKelamin,
        String alamat,
        String noHp,
        String golDarah) {

}
