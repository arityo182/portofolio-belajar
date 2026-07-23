/**
 * @module features/admin/types
 *
 * Tipe data untuk fitur admin. Bentuknya mengikuti DTO backend
 * (AdminController di `/api/admin/**`).
 */
import type { Hari } from "../jadwal/types";

/** Role pengguna — selaras dengan enum `Role` backend. */
export type Role = "PASIEN" | "DOKTER" | "ADMIN";

/** Data dokter dari endpoint admin (sama struktur dengan DokterResponse). */
export interface AdminDokter {
  id: number;
  userId: number;
  nama: string;
  email: string;
  poliId: number;
  namaPoli: string;
  noStr: string;
  spesialisasi: string;
  noHp: string;
}

/** Payload buat user dokter + profil sekaligus (POST /api/admin/dokter). */
export interface DokterCreateRequest {
  nama: string;
  email: string;
  password: string;
  poliId: number;
  noStr: string;
  spesialisasi: string;
  noHp: string;
}

/** Payload update profil dokter (PUT /api/admin/dokter/{id}). */
export interface DokterUpdateRequest {
  userId: number;
  poliId: number;
  noStr: string;
  spesialisasi: string;
  noHp: string;
}

/** Data user untuk manajemen pengguna admin. */
export interface UserAdmin {
  id: number;
  nama: string;
  email: string;
  role: Role;
  isActive: boolean;
}

/** Statistik dashboard admin. */
export interface DashboardStats {
  totalPasien: number;
  totalDokter: number;
  totalPoli: number;
  totalAppointment: number;
}

/** Payload buat/update poli. */
export interface PoliRequest {
  nama: string;
  deskripsi?: string;
}

/** Payload buat/update jadwal. */
export interface JadwalRequest {
  dokterId: number;
  hari: Hari;
  jamMulai: string;
  jamSelesai: string;
  kuota: number;
}

/** Payload buat user pasien + profil sekaligus (POST /api/admin/pasien). */
export interface PasienCreateRequest {
  nama: string;
  email: string;
  password: string;
  nik: string;
  tanggalLahir: string;
  jenisKelamin: "L" | "P";
  alamat: string;
  noHp: string;
  golDarah?: string | null;
}

/** Data antrian untuk papan antrian admin. */
export interface AntrianAdmin {
  id: number; appointmentId: number; nomorUrut: number;
  namaPasien: string; namaDokter: string; namaPoli: string;
  tanggal: string; status: "MENUNGGU" | "DIPERIKSA" | "SELESAI" | "BATAL";
}
