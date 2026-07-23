/**
 * @module features/pasien/types
 *
 * Tipe data untuk fitur pasien. Bentuknya mengikuti `PasienResponse` dan
 * `PasienRequest` pada backend Spring Boot (endpoint `/api/pasien`).
 */

/** Jenis kelamin pasien — selaras dengan enum `JenisKelamin` backend. */
export type JenisKelamin = "L" | "P";

/**
 * Payload pembuatan/pembaruan profil pasien (POST/PUT `/api/pasien`).
 * Selaras dengan record `PasienRequest`. Nomor rekam medis TIDAK dikirim
 * karena digenerate otomatis oleh backend.
 */
export interface PasienRequest {
  /** ID akun user yang terhubung ke profil pasien (dari `useAuth().user.id`) */
  userId: number;
  /** Nomor Induk Kependudukan (wajib) */
  nik: string;
  /** Tanggal lahir, format "yyyy-MM-dd" (wajib) */
  tanggalLahir: string;
  /** Jenis kelamin, "L" atau "P" (wajib) */
  jenisKelamin: JenisKelamin;
  /** Alamat tempat tinggal (wajib, tidak boleh kosong) */
  alamat: string;
  /** Nomor HP (wajib, tidak boleh kosong) */
  noHp: string;
  /** Golongan darah (opsional, mis. "O", "A", "B", "AB") */
  golDarah?: string | null;
}

/**
 * Data profil pasien yang dikembalikan backend.
 * Selaras dengan record `PasienResponse`. Perhatikan: `id` di sini adalah
 * ID pasien (dipakai sebagai `pasienId` pada booking), sedangkan `userId`
 * adalah ID akun login.
 */
export interface Pasien {
  /** ID pasien (dipakai sebagai `pasienId` saat booking) */
  id: number;
  /** ID akun user yang terhubung ke profil pasien ini */
  userId: number;
  /** Nama lengkap pasien (dari akun user) */
  nama: string;
  /** Email pasien (dari akun user) */
  email: string;
  /** Nomor rekam medis unik, mis. "RM-2026-000001" */
  noRekamMedis: string;
  /** Nomor Induk Kependudukan */
  nik: string;
  /** Tanggal lahir, format "yyyy-MM-dd" */
  tanggalLahir: string;
  /** Jenis kelamin (L/P) */
  jenisKelamin: JenisKelamin;
  /** Alamat tempat tinggal */
  alamat: string | null;
  /** Nomor HP */
  noHp: string;
  /** Golongan darah (boleh kosong) */
  golDarah: string | null;
}
