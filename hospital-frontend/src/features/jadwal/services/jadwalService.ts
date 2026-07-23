/**
 * @module features/jadwal/services/jadwalService
 *
 * Service layer untuk fitur jadwal praktik dokter. Mengambil data jadwal
 * dari backend Spring Boot lewat API client terpusat (JWT otomatis).
 * Tidak membuat instance Axios baru.
 */
import api from "../../../core/api/client";
import type { JadwalDokter } from "../types";

/**
 * Mengambil seluruh daftar jadwal praktik (endpoint `GET /api/jadwal`).
 *
 * @returns Promise Axios berisi array {@link JadwalDokter}
 * @throws AxiosError - bila token tidak valid (401) atau server error
 *
 * @example
 * const res = await getAllJadwal();
 * console.log(res.data.length);
 */
export const getAllJadwal = () => {
  return api.get<JadwalDokter[]>("/jadwal");
};

/**
 * Mengambil jadwal praktik AKTIF milik seorang dokter
 * (endpoint `GET /api/jadwal/dokter/{dokterId}`).
 *
 * Backend hanya mengembalikan jadwal yang masih aktif, sehingga cocok
 * untuk ditampilkan pada alur pemilihan jadwal booking.
 *
 * @param dokterId - ID dokter yang dipilih
 * @returns Promise Axios berisi array {@link JadwalDokter} aktif dokter tersebut
 * @throws AxiosError - bila dokter tidak ditemukan (404) atau token tidak valid (401)
 *
 * @example
 * const res = await getJadwalByDokter(1);
 * res.data.forEach((j) => console.log(j.hari, j.jamMulai));
 */
export const getJadwalByDokter = (dokterId: number) => {
  return api.get<JadwalDokter[]>(`/jadwal/dokter/${dokterId}`);
};
