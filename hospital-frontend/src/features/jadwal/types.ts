/**
 * @module features/jadwal/types
 *
 * Tipe data untuk fitur jadwal praktik dokter. Bentuknya mengikuti
 * `JadwalDokterResponse` pada backend Spring Boot (endpoint `/api/jadwal`).
 */

/**
 * Hari praktik dalam seminggu — selaras dengan enum `Hari` di backend
 * (diserialisasi sebagai string, mis. `"SENIN"`).
 */
export type Hari =
  | "SENIN"
  | "SELASA"
  | "RABU"
  | "KAMIS"
  | "JUMAT"
  | "SABTU"
  | "MINGGU";

/**
 * Data satu jadwal praktik dokter yang dikembalikan backend.
 * Selaras dengan record `JadwalDokterResponse`.
 */
export interface JadwalDokter {
  /** ID unik jadwal */
  id: number;
  /** ID dokter pemilik jadwal */
  dokterId: number;
  /** Nama dokter pemilik jadwal */
  namaDokter: string;
  /** Hari praktik (SENIN..MINGGU) */
  hari: Hari;
  /** Jam mulai praktik, format "HH:mm:ss", mis. "08:00:00" */
  jamMulai: string;
  /** Jam selesai praktik, format "HH:mm:ss", mis. "12:00:00" */
  jamSelesai: string;
  /** Kuota maksimum pasien per sesi */
  kuota: number;
  /** Status keaktifan jadwal; `false` berarti jadwal dinonaktifkan */
  isActive: boolean;
}
