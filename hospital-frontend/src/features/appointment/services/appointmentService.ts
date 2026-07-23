/**
 * @module features/appointment/services/appointmentService
 *
 * Service layer untuk fitur appointment (janji temu) dan nomor antrian.
 * Semua panggilan lewat API client terpusat (JWT otomatis) — tidak ada
 * instance Axios baru.
 */
import api from "../../../core/api/client";
import type {
  AppointmentRequest,
  AppointmentResponse,
  AntrianResponse,
  StatusAppointment,
} from "../types";

/**
 * Membuat appointment baru (endpoint `POST /api/appointment`).
 *
 * Backend memvalidasi secara berurutan: pasien & jadwal ada (404),
 * jadwal aktif (400), hari tanggal cocok dengan jadwal (400), tidak
 * double-booking (409), dan kuota belum penuh (400). Bila sukses, nomor
 * antrian di-generate otomatis dan sudah termuat pada `nomorAntrian`.
 *
 * @param payload - data booking (pasienId, jadwalId, tanggal, keluhan)
 * @returns Promise Axios berisi {@link AppointmentResponse} (HTTP 201)
 * @throws AxiosError - 400 (jadwal nonaktif/hari tak cocok/kuota penuh),
 *   404 (pasien/jadwal tak ada), atau 409 (double-booking)
 *
 * @example
 * const res = await createAppointment({ pasienId: 1, jadwalId: 1, tanggal: "2026-07-13", keluhan: "Nyeri lutut" });
 * console.log(res.data.nomorAntrian); // mis. 1
 */
export const createAppointment = (payload: AppointmentRequest) => {
  return api.post<AppointmentResponse>("/appointment", payload);
};

/**
 * Mengambil seluruh appointment milik seorang pasien
 * (endpoint `GET /api/appointment/pasien/{pasienId}`).
 *
 * @param pasienId - ID pasien
 * @returns Promise Axios berisi array {@link AppointmentResponse}
 * @throws AxiosError - 401 bila token invalid, atau server error
 *
 * @example
 * const res = await getAppointmentsByPasien(1);
 * res.data.forEach((a) => console.log(a.namaDokter, a.status));
 */
export const getAppointmentsByPasien = (pasienId: number) => {
  return api.get<AppointmentResponse[]>(`/appointment/pasien/${pasienId}`);
};

/**
 * Mengambil detail satu appointment berdasarkan ID
 * (endpoint `GET /api/appointment/{id}`).
 *
 * @param id - ID appointment
 * @returns Promise Axios berisi {@link AppointmentResponse}
 * @throws AxiosError - 404 bila appointment tidak ditemukan
 *
 * @example
 * const res = await getAppointmentById(1);
 * console.log(res.data.status);
 */
export const getAppointmentById = (id: number) => {
  return api.get<AppointmentResponse>(`/appointment/${id}`);
};

/**
 * Memperbarui status sebuah appointment
 * (endpoint `PATCH /api/appointment/{id}/status?status=...`).
 *
 * Status dikirim sebagai query parameter, sesuai controller backend.
 *
 * @param id - ID appointment
 * @param status - status baru (MENUNGGU | DIPERIKSA | SELESAI | BATAL)
 * @returns Promise Axios berisi {@link AppointmentResponse} terbaru
 * @throws AxiosError - 404 bila appointment tidak ditemukan, 400 bila status invalid
 *
 * @example
 * // Membatalkan janji temu:
 * await updateAppointmentStatus(1, "BATAL");
 */
export const updateAppointmentStatus = (
  id: number,
  status: StatusAppointment
) => {
  return api.patch<AppointmentResponse>(
    `/appointment/${id}/status`,
    null,
    { params: { status } }
  );
};

/**
 * Mengambil nomor antrian sebuah appointment
 * (endpoint `GET /api/antrian/appointment/{appointmentId}`).
 *
 * Fungsi fallback: dipakai bila `nomorAntrian` belum termuat pada
 * {@link AppointmentResponse} (mis. antrian belum digenerate saat itu).
 *
 * @param appointmentId - ID appointment
 * @returns Promise Axios berisi {@link AntrianResponse}
 * @throws AxiosError - 404 bila antrian belum ada
 *
 * @example
 * const res = await getAntrianByAppointment(1);
 * console.log(res.data.nomorUrut);
 */
export const getAntrianByAppointment = (appointmentId: number) => {
  return api.get<AntrianResponse>(`/antrian/appointment/${appointmentId}`);
};
