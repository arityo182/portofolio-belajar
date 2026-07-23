/**
 * @module features/rekammedis/services/resepService
 *
 * Service layer untuk fitur resep obat. Mengambil data resep dari backend
 * Spring Boot lewat API client terpusat (JWT otomatis). Tidak membuat
 * instance Axios baru.
 */
import api from "../../../core/api/client";
import type { Resep } from "../types";

/**
 * Mengambil seluruh resep milik sebuah rekam medis
 * (endpoint `GET /api/resep/rekam-medis/{rekamMedisId}`).
 *
 * Dipakai pada kartu rekam medis untuk menampilkan daftar obat resep
 * terkait (FR-14, FR-15).
 *
 * @param rekamMedisId - ID rekam medis
 * @returns Promise Axios berisi array {@link Resep}
 * @throws AxiosError - 401 bila token invalid, atau server error
 *
 * @example
 * const res = await getResepByRekamMedis(1);
 * res.data.forEach((r) => console.log(r.namaObat));
 */
export const getResepByRekamMedis = (rekamMedisId: number) => {
  return api.get<Resep[]>(`/resep/rekam-medis/${rekamMedisId}`);
};