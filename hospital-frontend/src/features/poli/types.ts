/**
 * @module features/poli/types
 *
 * Tipe data untuk fitur poli (poliklinik). Bentuknya mengikuti
 * `PoliResponse` pada backend Spring Boot (endpoint `/api/poli`).
 */

/**
 * Data satu poli/poliklinik yang dikembalikan backend.
 * Selaras dengan record `PoliResponse` (id, nama, deskripsi, isActive).
 */
export interface Poli {
  /** ID unik poli */
  id: number;
  /** Nama poli, mis. "Poli Tulang" */
  nama: string;
  /** Deskripsi/keterangan poli (boleh kosong) */
  deskripsi: string | null;
  /** Status keaktifan poli; `false` berarti poli dinonaktifkan (soft delete) */
  isActive: boolean;
}
