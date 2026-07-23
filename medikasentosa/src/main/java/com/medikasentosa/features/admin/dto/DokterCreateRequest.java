package com.medikasentosa.features.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO permintaan untuk membuat user dokter baru beserta profil dokternya
 * dalam satu transaksi (FR-36).
 *
 * Berbeda dengan {@code DokterRequest}, DTO ini menyertakan kredensial akun
 * (nama, email, password) sehingga admin tidak perlu membuat user terpisah
 * terlebih dahulu. Backend akan membuat User(role=DOKTER) + profil Dokter
 * secara atomik.
 *
 * @author Ari
 * @since 1.0.0
 */
public record DokterCreateRequest(

        @NotBlank(message = "Nama wajib diisi") String nama,

        @NotBlank(message = "Email wajib diisi")
        @Email(message = "Format email tidak valid") String email,

        @NotBlank(message = "Password wajib diisi")
        @Size(min = 6, message = "Password minimal 6 karakter") String password,

        @NotNull(message = "Poli wajib diisi") Long poliId,

        @NotBlank(message = "No STR wajib diisi") String noStr,

        @NotBlank(message = "Spesialisasi wajib diisi") String spesialisasi,

        @NotBlank(message = "No HP wajib diisi") String noHp) {
}
