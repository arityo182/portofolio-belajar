/**
 * @module features/poli/services/poliService
 *
 * Service layer untuk fitur poli. Mengambil data poli dari backend
 * Spring Boot lewat API client terpusat (JWT disisipkan otomatis oleh
 * interceptor). Tidak membuat instance Axios baru.
 */
import api from "../../../core/api/client";
import type { Poli } from "../types";

/**
 * Mengambil seluruh daftar poli dari backend (endpoint `GET /api/poli`).
 *
 * Backend mengembalikan semua poli termasuk yang nonaktif, sehingga
 * penyaringan poli aktif (mis. `poli.isActive`) dilakukan di pemanggil
 * bila diperlukan.
 *
 * @returns Promise Axios berisi array {@link Poli}
 * @throws AxiosError - bila token tidak valid (401) atau server error
 *
 * @example
 * const res = await getAllPoli();
 * const aktif = res.data.filter((p) => p.isActive);
 */
export const getAllPoli = () => {
  return api.get<Poli[]>("/poli");
};

/**
 * Mengambil detail satu poli berdasarkan ID (endpoint `GET /api/poli/{id}`).
 *
 * @param id - ID poli yang dicari
 * @returns Promise Axios berisi satu {@link Poli}
 * @throws AxiosError - bila poli tidak ditemukan (404) atau token tidak valid (401)
 *
 * @example
 * const res = await getPoliById(1);
 * console.log(res.data.nama); // "Poli Tulang"
 */
export const getPoliById = (id: number) => {
  return api.get<Poli>(`/poli/${id}`);
};
