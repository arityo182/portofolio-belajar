package com.medikasentosa.features.pasien.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medikasentosa.features.pasien.entity.JenisKelamin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO permintaan untuk membuat atau memperbarui data profil pasien.
 * Nomor rekam medis tidak diikutkan karena digenerate otomatis oleh sistem.
 *
 * @author Ari
 * @since 1.0.0
 */
public record PasienRequest(

        @NotNull(message = "User wajib diisi") Long userId,

        @NotBlank(message = "NIK wajib diisi") String nik,

        @NotNull(message = "Tanggal lahir wajib diisi")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate tanggalLahir,

        @NotNull(message = "Jenis kelamin wajib diisi") JenisKelamin jenisKelamin,

        @NotBlank(message = "Alamat wajib diisi") String alamat,

        @NotBlank(message = "No HP wajib diisi") String noHp,

        String golDarah) {
}
