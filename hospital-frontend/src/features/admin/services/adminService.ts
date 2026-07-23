/**
 * @module features/admin/services/adminService
 *
 * Service layer untuk fitur admin. Semua panggilan lewat API client terpusat
 * (JWT otomatis). Backend membatasi endpoint `/api/admin/**` ke role ADMIN.
 */
import api from "../../../core/api/client";
import type {
  AdminDokter, DokterCreateRequest, DokterUpdateRequest,
  UserAdmin, DashboardStats, Role, PoliRequest, JadwalRequest,
  PasienCreateRequest, AntrianAdmin,
} from "../types";
import type { Poli } from "../../poli/types";
import type { JadwalDokter } from "../../jadwal/types";
import type { Pasien, PasienRequest } from "../../pasien/types";

/* ================= DASHBOARD ================= */

/**
 * Mengambil statistik dashboard admin (GET /api/admin/dashboard).
 *
 * @returns statistik jumlah pasien, dokter, poli, appointment
 */
export const getDashboard = () =>
  api.get<DashboardStats>("/admin/dashboard");

/* ================= MANAJEMEN POLI (FR-35) ================= */

/**
 * Mengambil semua poli termasuk nonaktif (GET /api/admin/poli).
 *
 * @returns daftar semua poli
 */
export const getAllPoliAdmin = () =>
  api.get<Poli[]>("/admin/poli");

/**
 * Membuat poli baru (POST /api/admin/poli).
 *
 * @param payload data poli baru (nama, deskripsi)
 * @returns poli yang dibuat (HTTP 201)
 */
export const createPoli = (payload: PoliRequest) =>
  api.post<Poli>("/admin/poli", payload);

/**
 * Memperbarui poli (PUT /api/admin/poli/{id}).
 *
 * @param id ID poli
 * @param payload data poli baru
 * @returns poli setelah diperbarui
 */
export const updatePoli = (id: number, payload: PoliRequest) =>
  api.put<Poli>(`/admin/poli/${id}`, payload);

/**
 * Menonaktifkan poli (DELETE /api/admin/poli/{id}, soft delete).
 *
 * @param id ID poli
 */
export const deactivatePoli = (id: number) =>
  api.delete(`/admin/poli/${id}`);

/* ================= MANAJEMEN DOKTER (FR-36) ================= */

/**
 * Mengambil semua dokter termasuk nonaktif (GET /api/admin/dokter).
 *
 * @returns daftar semua dokter
 */
export const getAllDokterAdmin = () =>
  api.get<AdminDokter[]>("/admin/dokter");

/**
 * Membuat user dokter + profil dokter sekaligus (POST /api/admin/dokter).
 *
 * @param payload data user dokter baru (nama, email, password, poliId, dll)
 * @returns dokter yang dibuat (HTTP 201)
 */
export const createDokter = (payload: DokterCreateRequest) =>
  api.post<AdminDokter>("/admin/dokter", payload);

/**
 * Memperbarui profil dokter (PUT /api/admin/dokter/{id}).
 *
 * @param id ID dokter
 * @param payload data dokter baru
 * @returns dokter setelah diperbarui
 */
export const updateDokter = (id: number, payload: DokterUpdateRequest) =>
  api.put<AdminDokter>(`/admin/dokter/${id}`, payload);

/**
 * Menonaktifkan dokter (DELETE /api/admin/dokter/{id}, soft delete).
 *
 * @param id ID dokter
 */
export const deactivateDokter = (id: number) =>
  api.delete(`/admin/dokter/${id}`);

/* ================= MANAJEMEN JADWAL (FR-36) ================= */

/**
 * Mengambil semua jadwal (GET /api/admin/jadwal).
 *
 * @returns daftar semua jadwal
 */
export const getAllJadwalAdmin = () =>
  api.get<JadwalDokter[]>("/admin/jadwal");

/**
 * Membuat jadwal baru (POST /api/admin/jadwal).
 *
 * @param payload data jadwal baru
 * @returns jadwal yang dibuat (HTTP 201)
 */
export const createJadwal = (payload: JadwalRequest) =>
  api.post<JadwalDokter>("/admin/jadwal", payload);

/**
 * Memperbarui jadwal (PUT /api/admin/jadwal/{id}).
 *
 * @param id ID jadwal
 * @param payload data jadwal baru
 * @returns jadwal setelah diperbarui
 */
export const updateJadwal = (id: number, payload: JadwalRequest) =>
  api.put<JadwalDokter>(`/admin/jadwal/${id}`, payload);

/**
 * Menonaktifkan jadwal (DELETE /api/admin/jadwal/{id}, soft delete).
 *
 * @param id ID jadwal
 */
export const deactivateJadwal = (id: number) =>
  api.delete(`/admin/jadwal/${id}`);

/* ================= MANAJEMEN PENGGUNA (FR-04) ================= */

/**
 * Mengambil daftar semua pengguna, opsional difilter per role
 * (GET /api/admin/users?role=...).
 *
 * @param role filter role (opsional)
 * @returns daftar pengguna dengan status aktif
 */
export const getAllUsers = (role?: Role) =>
  api.get<UserAdmin[]>("/admin/users", { params: role ? { role } : {} });

/**
 * Mengaktifkan/nonaktifkan akun pengguna (PATCH /api/admin/users/{id}/status).
 *
 * @param id ID user
 * @param active status aktif baru
 * @returns user setelah diperbarui
 */
export const setUserStatus = (id: number, active: boolean) =>
  api.patch<UserAdmin>(`/admin/users/${id}/status`, null, { params: { active } });

/**
 * Reset password pengguna (PATCH /api/admin/users/{id}/password).
 *
 * Admin mengganti password dokter/pasien tanpa perlu tahu password lama.
 *
 * @param id ID user
 * @param password password baru (min 6 karakter)
 * @returns user setelah diperbarui
 */
export const resetPassword = (id: number, password: string) =>
  api.patch<UserAdmin>(`/admin/users/${id}/password`, { password });

/* ================= MANAJEMEN PASIEN ================= */

/**
 * Mengambil daftar semua pasien (GET /api/admin/pasien).
 *
 * @returns daftar semua pasien
 */
export const getAllPasienAdmin = () =>
  api.get<Pasien[]>("/admin/pasien");

/**
 * Membuat user pasien + profil pasien sekaligus (POST /api/admin/pasien).
 *
 * @param payload data user pasien baru (nama, email, password, nik, dll)
 * @returns pasien yang dibuat (HTTP 201)
 */
export const createPasien = (payload: PasienCreateRequest) =>
  api.post<Pasien>("/admin/pasien", payload);

/**
 * Memperbarui profil pasien (PUT /api/admin/pasien/{id}).
 *
 * @param id ID pasien
 * @param payload data profil pasien
 * @returns pasien setelah diperbarui
 */
export const updatePasien = (id: number, payload: PasienRequest) =>
  api.put<Pasien>(`/admin/pasien/${id}`, payload);

/**
 * Menonaktifkan akun pasien (DELETE /api/admin/pasien/{id}).
 *
 * @param id ID pasien
 */
export const deactivatePasien = (id: number) =>
  api.delete(`/admin/pasien/${id}`);

/* ================= MANAJEMEN ANTRIAN ================= */

/**
 * Mengambil semua antrian untuk papan antrian admin (GET /api/admin/antrian).
 *
 * @returns daftar semua antrian
 */
export const getAllAntrianAdmin = () =>
  api.get<AntrianAdmin[]>("/admin/antrian");
