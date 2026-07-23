/**
 * @module features/rekammedis/services/rekamMedisService
 *
 * Service layer untuk fitur rekam medis. Mengambil data rekam medis dari
 * backend Spring Boot lewat API client terpusat (JWT otomatis). Tidak
 * membuat instance Axios baru.
 */
import api from "../../../core/api/client";
import type { RekamMedis } from "../types";

/**
 * Mengambil seluruh riwayat rekam medis milik seorang pasien
 * (endpoint `GET /api/rekam-medis/pasien/{pasienId}`).
 *
 * Dipakai pada halaman "Riwayat Rekam Medis" pasien (FR-15).
 *
 * @param pasienId - ID pasien
 * @returns Promise Axios berisi array {@link RekamMedis}
 * @throws AxiosError - 401 bila token invalid, atau server error
 *
 * @example
 * const res = await getRekamMedisByPasien(1);
 * res.data.forEach((r) => console.log(r.diagnosa));
 */
export const getRekamMedisByPasien = (pasienId: number) => {
  return api.get<RekamMedis[]>(`/rekam-medis/pasien/${pasienId}`);
};

/**
 * Mengambil detail satu rekam medis berdasarkan ID
 * (endpoint `GET /api/rekam-medis/{id}`).
 *
 * @param id - ID rekam medis
 * @returns Promise Axios berisi satu {@link RekamMedis}
 * @throws AxiosError - 404 bila rekam medis tidak ditemukan
 *
 * @example
 * const res = await getRekamMedisById(1);
 * console.log(res.data.diagnosa);
 */
export const getRekamMedisById = (id: number) => {
  return api.get<RekamMedis>(`/rekam-medis/${id}`);
};