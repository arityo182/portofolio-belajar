/**
 * @module features/pasien/services/pasienService
 *
 * Service layer untuk fitur pasien. Mengambil dan mengelola data profil
 * pasien dari backend Spring Boot lewat API client terpusat (JWT otomatis).
 * Tidak membuat instance Axios baru.
 *
 * Pada Fitur 2, fungsi utama yang dipakai adalah {@link getPasienByUserId}
 * untuk menerjemahkan `userId` (dari sesi login) menjadi `pasienId` yang
 * dibutuhkan saat membuat appointment. Pada Fitur 3, fungsi {@link createPasien}
 * dan {@link updatePasien} dipakai untuk melengkapi/mengubah profil pasien.
 */
import api from "../../../core/api/client";
import type { Pasien, PasienRequest } from "../types";

/**
 * Mengambil profil pasien berdasarkan ID akun user
 * (endpoint `GET /api/pasien/user/{userId}`).
 *
 * Dipakai untuk memperoleh `pasienId` dari user yang sedang login: ambil
 * `user.id` dari context auth, lalu resolve ke profil pasien di sini.
 *
 * @param userId - ID akun user (dari `useAuth().user.id`)
 * @returns Promise Axios berisi satu {@link Pasien}
 * @throws AxiosError - 404 bila user belum punya profil pasien, atau 401 bila token invalid
 *
 * @example
 * const { user } = useAuth();
 * const res = await getPasienByUserId(user!.id);
 * const pasienId = res.data.id;
 */
export const getPasienByUserId = (userId: number) => {
  return api.get<Pasien>(`/pasien/user/${userId}`);
};

/**
 * Mengambil detail satu pasien berdasarkan ID pasien
 * (endpoint `GET /api/pasien/{id}`).
 *
 * @param id - ID pasien
 * @returns Promise Axios berisi satu {@link Pasien}
 * @throws AxiosError - 404 bila pasien tidak ditemukan, atau 401 bila token invalid
 *
 * @example
 * const res = await getPasienById(1);
 * console.log(res.data.noRekamMedis);
 */
export const getPasienById = (id: number) => {
  return api.get<Pasien>(`/pasien/${id}`);
};

/**
 * Membuat profil pasien baru (endpoint `POST /api/pasien`).
 *
 * Dipakai bila user yang sudah login belum memiliki profil pasien (response
 * 404 pada {@link getPasienByUserId}). Nomor rekam medis digenerate otomatis
 * oleh backend sehingga tidak perlu dikirim.
 *
 * @param payload - data profil pasien baru (userId, nik, tanggalLahir, dll)
 * @returns Promise Axios berisi {@link Pasien} yang baru dibuat (HTTP 201)
 * @throws AxiosError - 400 bila data tidak valid, 404 bila user tak ada,
 *   409 bila user sudah terdaftar sebagai pasien
 *
 * @example
 * await createPasien({ userId: 1, nik: "317...", tanggalLahir: "1990-05-12", ... });
 */
export const createPasien = (payload: PasienRequest) => {
  return api.post<Pasien>("/pasien", payload);
};

/**
 * Memperbarui profil pasien yang sudah ada (endpoint `PUT /api/pasien/{id}`).
 *
 * Seluruh field {@link PasienRequest} wajib dikirim (semantik PUT = replace).
 * `userId` diambil dari user yang sedang login agar tidak dapat diubah klien.
 *
 * @param id - ID pasien yang akan diperbarui
 * @param payload - data profil pasien baru (field wajib diisi)
 * @returns Promise Axios berisi {@link Pasien} setelah diperbarui (HTTP 200)
 * @throws AxiosError - 400 bila data tidak valid, 404 bila pasien tak ada
 *
 * @example
 * await updatePasien(1, { userId: 1, nik: "317...", tanggalLahir: "1990-05-12", ... });
 */
export const updatePasien = (id: number, payload: PasienRequest) => {
  return api.put<Pasien>(`/pasien/${id}`, payload);
};
