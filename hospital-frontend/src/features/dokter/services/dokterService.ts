/**
 * @module features/dokter/services/dokterService
 *
 * Service layer untuk fitur dokter. Mengambil data dokter dari backend
 * Spring Boot lewat API client terpusat (JWT otomatis). Tidak membuat
 * instance Axios baru.
 */
import api from "../../../core/api/client";
import type { Dokter } from "../types";

/**
 * Mengambil seluruh daftar dokter (endpoint `GET /api/dokter`).
 *
 * @returns Promise Axios berisi array {@link Dokter}
 * @throws AxiosError - bila token tidak valid (401) atau server error
 *
 * @example
 * const res = await getAllDokter();
 * console.log(res.data.length);
 */
export const getAllDokter = () => {
  return api.get<Dokter[]>("/dokter");
};

/**
 * Mengambil detail satu dokter berdasarkan ID (endpoint `GET /api/dokter/{id}`).
 *
 * @param id - ID dokter yang dicari
 * @returns Promise Axios berisi satu {@link Dokter}
 * @throws AxiosError - bila dokter tidak ditemukan (404) atau token tidak valid (401)
 *
 * @example
 * const res = await getDokterById(1);
 * console.log(res.data.spesialisasi); // "Ortopedi"
 */
export const getDokterById = (id: number) => {
  return api.get<Dokter>(`/dokter/${id}`);
};

/**
 * Mengambil profil dokter berdasarkan ID user
 * (endpoint `GET /api/dokter/user/{userId}`).
 *
 * Dipakai untuk panel dokter: resolve userId (dari login) menjadi dokterId.
 *
 * @param userId - ID akun user (dari `useAuth().user.id`)
 * @returns Promise Axios berisi satu {@link Dokter}
 */
export const getDokterByUserId = (userId: number) => {
  return api.get<Dokter>(`/dokter/user/${userId}`);
};

/**
 * Mengambil daftar dokter pada sebuah poli
 * (endpoint `GET /api/dokter/poli/{poliId}`).
 *
 * Dipakai pada alur booking: setelah pasien memilih poli, tampilkan
 * dokter-dokter yang bertugas di poli tersebut.
 *
 * @param poliId - ID poli yang dipilih
 * @returns Promise Axios berisi array {@link Dokter} pada poli tersebut
 * @throws AxiosError - bila poli tidak ditemukan (404) atau token tidak valid (401)
 *
 * @example
 * const res = await getDokterByPoli(1);
 * res.data.forEach((d) => console.log(d.nama));
 */
export const getDokterByPoli = (poliId: number) => {
  return api.get<Dokter[]>(`/dokter/poli/${poliId}`);
};
