/**
 * @module features/dokterpanel/services/dokterService
 *
 * Service layer untuk panel dokter. Mengambil data appointment pasien
 * milik dokter yang login, serta membuat rekam medis & resep.
 */
import api from "../../../core/api/client";
import { getDokterByUserId } from "../../dokter/services/dokterService";
/** Menerjemahkan userId (dari auth) menjadi dokterId. */
export const getDokterIdByUserId = (userId) => getDokterByUserId(userId);
/**
 * Mengambil daftar appointment milik dokter (GET /api/appointment/dokter/{dokterId}).
 *
 * @param dokterId ID dokter
 * @returns daftar appointment dokter tersebut
 */
export const getAppointmentsByDokter = (dokterId) => {
    // Pakai endpoint appointment by dokter yang baru ditambahkan
    return api.get(`/appointment/dokter/${dokterId}`);
};
/**
 * Memperbarui status appointment (PATCH /api/appointment/{id}/status).
 *
 * @param id ID appointment
 * @param status status baru
 */
export const updateAppointmentStatus = (id, status) => api.patch(`/appointment/${id}/status`, null, { params: { status } });
/**
 * Membuat rekam medis dari appointment (POST /api/rekam-medis).
 *
 * @param payload data rekam medis (appointmentId, diagnosa, tindakan, catatan)
 */
export const createRekamMedis = (payload) => api.post("/rekam-medis", payload);
/**
 * Menambahkan resep obat pada rekam medis (POST /api/resep).
 *
 * @param payload data resep (rekamMedisId, namaObat, dosis, jumlah, aturanPakai)
 */
export const createResep = (payload) => api.post("/resep", payload);
/**
 * Mengambil resep milik sebuah rekam medis (GET /api/resep/rekam-medis/{id}).
 *
 * @param rekamMedisId ID rekam medis
 */
export const getResepByRekamMedis = (rekamMedisId) => api.get(`/resep/rekam-medis/${rekamMedisId}`);
/**
 * Mengambil rekam medis yang dibuat oleh dokter
 * (GET /api/rekam-medis/dokter/{dokterId}).
 *
 * @param dokterId ID dokter
 * @returns daftar rekam medis yang dibuat dokter tersebut
 */
export const getRekamMedisByDokter = (dokterId) => api.get(`/rekam-medis/dokter/${dokterId}`);
