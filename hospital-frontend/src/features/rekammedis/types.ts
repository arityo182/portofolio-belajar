/**
 * @module features/rekammedis/types
 *
 * Tipe data untuk fitur rekam medis & resep. Bentuknya mengikuti
 * `RekamMedisResponse` dan `ResepResponse` pada backend Spring Boot
 * (endpoint `/api/rekam-medis` dan `/api/resep`).
 */

/**
 * Data satu rekam medis yang dikembalikan backend (flat).
 * Selaras dengan record `RekamMedisResponse`.
 */
export interface RekamMedis {
  /** ID unik rekam medis */
  id: number;
  /** ID appointment sumber rekam medis */
  appointmentId: number;
  /** ID pasien pemilik rekam medis */
  pasienId: number;
  /** Nama pasien */
  namaPasien: string;
  /** Nomor rekam medis pasien, mis. "RM-2026-000001" */
  noRekamMedis: string;
  /** ID dokter yang membuat rekam medis */
  dokterId: number;
  /** Nama dokter */
  namaDokter: string;
  /** Diagnosa dokter */
  diagnosa: string;
  /** Tindakan yang dilakukan */
  tindakan: string;
  /** Catatan tambahan (boleh kosong) */
  catatan: string | null;
  /** Tanggal pemeriksaan, format "yyyy-MM-dd" */
  tanggal: string;
  /** Waktu rekam medis dibuat (ISO datetime) */
  createdAt: string;
}

/**
 * Data satu resep obat yang dikembalikan backend.
 * Selaras dengan record `ResepResponse`.
 *
 * Sejak Fase 2, resep menyertakan {@code obatId} yang merujuk ke
 * data master obat (null untuk resep lama).
 */
export interface Resep {
  /** ID unik resep */
  id: number;
  /** ID rekam medis pemilik resep */
  rekamMedisId: number;
  /** ID obat dari tabel master (null = resep lama belum ada relasi) */
  obatId: number | null;
  /** Nama obat (dari tabel obat jika ada relasi, atau fallback teks bebas) */
  namaObat: string;
  /** Dosis obat, mis. "500mg" */
  dosis: string;
  /** Jumlah obat */
  jumlah: number;
  /** Aturan pakai, mis. "3x sehari setelah makan" */
  aturanPakai: string;
}