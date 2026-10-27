package com.medikasentosa.features.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medikasentosa.features.pasien.entity.JenisKelamin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO permintaan untuk membuat user pasien baru beserta profil pasiennya
 * dalam satu transaksi (manajemen pasien oleh admin).
 *
 * Berbeda dengan {@code PasienRequest}, DTO ini menyertakan kredensial akun
 * (nama, email, password) sehingga admin tidak perlu minta pasien daftar
 * sendiri via /register. Password mengikuti kebijakan yang sama dengan
 * registrasi.
 *
 * @author Ari
 * @since 1.0.0
 */
public record PasienCreateRequest(

        @NotBlank(message = "Nama wajib diisi") String nama,

        @NotBlank(message = "Email wajib diisi")
        @Email(message = "Format email tidak valid") String email,

        @NotBlank(message = "Password wajib diisi")
        @Size(min = 12, max = 128, message = "Password minimal 12 karakter")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "Password harus mengandung minimal satu huruf dan satu angka")
        String password,

        @NotBlank(message = "NIK wajib diisi") String nik,

        @NotNull(message = "Tanggal lahir wajib diisi")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate tanggalLahir,

        @NotNull(message = "Jenis kelamin wajib diisi") JenisKelamin jenisKelamin,

        @NotBlank(message = "Alamat wajib diisi") String alamat,

        @NotBlank(message = "No HP wajib diisi") String noHp,

        String golDarah) {
}
