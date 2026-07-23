/**
 * @module features/dokter/types
 *
 * Tipe data untuk fitur dokter. Bentuknya mengikuti `DokterResponse`
 * pada backend Spring Boot (endpoint `/api/dokter`).
 */

/**
 * Data satu dokter yang dikembalikan backend.
 * Selaras dengan record `DokterResponse` — data akun (nama, email) sudah
 * di-flatten dari relasi user, dan poli sudah menyertakan nama poli.
 */
export interface Dokter {
  /** ID unik dokter */
  id: number;
  /** ID akun user yang terhubung ke dokter ini */
  userId: number;
  /** Nama lengkap dokter (dari akun user) */
  nama: string;
  /** Email dokter (dari akun user) */
  email: string;
  /** ID poli tempat dokter bertugas */
  poliId: number;
  /** Nama poli tempat dokter bertugas, mis. "Poli Tulang" */
  namaPoli: string;
  /** Nomor Surat Tanda Registrasi dokter */
  noStr: string;
  /** Spesialisasi dokter, mis. "Ortopedi" */
  spesialisasi: string;
  /** Nomor HP dokter */
  noHp: string;
}
