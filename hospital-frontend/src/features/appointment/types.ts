/**
 * @module features/appointment/types
 *
 * Tipe data untuk fitur appointment (janji temu) dan nomor antrian.
 * Bentuknya mengikuti `AppointmentRequest`/`AppointmentResponse` dan
 * `AntrianResponse` pada backend Spring Boot.
 */
import type { Hari } from "../jadwal/types";

/**
 * Status siklus hidup appointment — selaras dengan enum
 * `StatusAppointment` backend (diserialisasi sebagai string).
 */
export type StatusAppointment = "MENUNGGU" | "DIPERIKSA" | "SELESAI" | "BATAL";

/**
 * Payload pembuatan appointment (POST `/api/appointment`).
 * Selaras dengan record `AppointmentRequest`. Dokter TIDAK dikirim —
 * backend menurunkannya dari jadwal yang dipilih.
 */
export interface AppointmentRequest {
  /** ID pasien pembuat janji temu */
  pasienId: number;
  /** ID jadwal praktik yang dipilih */
  jadwalId: number;
  /** Tanggal janji temu, format "yyyy-MM-dd" (harus hari ini / masa depan) */
  tanggal: string;
  /** Keluhan pasien (opsional) */
  keluhan?: string;
}

/**
 * Data appointment yang dikembalikan backend dalam bentuk datar (flat).
 * Selaras dengan record `AppointmentResponse`.
 */
export interface AppointmentResponse {
  /** ID appointment */
  id: number;
  /** ID pasien */
  pasienId: number;
  /** Nama pasien */
  namaPasien: string;
  /** Nomor rekam medis pasien */
  noRekamMedis: string;
  /** ID dokter (diturunkan dari jadwal) */
  dokterId: number;
  /** Nama dokter */
  namaDokter: string;
  /** Nama poli dokter */
  namaPoli: string;
  /** ID jadwal praktik */
  jadwalId: number;
  /** Hari praktik (SENIN..MINGGU) */
  hari: Hari;
  /** Jam mulai praktik, format "HH:mm:ss" */
  jamMulai: string;
  /** Jam selesai praktik, format "HH:mm:ss" */
  jamSelesai: string;
  /** Tanggal janji temu, format "yyyy-MM-dd" */
  tanggal: string;
  /** Status appointment saat ini */
  status: StatusAppointment;
  /** Keluhan pasien (boleh kosong) */
  keluhan: string | null;
  /**
   * Nomor antrian; `null` bila antrian belum digenerate. Pada backend
   * saat ini antrian auto-generate ketika appointment dibuat, sehingga
   * biasanya sudah terisi pada response POST.
   */
  nomorAntrian: number | null;
}

/**
 * Data antrian yang dikembalikan backend.
 * Selaras dengan record `AntrianResponse`.
 */
export interface AntrianResponse {
  /** ID antrian */
  id: number;
  /** ID appointment terkait */
  appointmentId: number;
  /** Nomor urut antrian */
  nomorUrut: number;
  /** Nama pasien */
  namaPasien: string;
  /** Nama dokter */
  namaDokter: string;
  /** Tanggal, format "yyyy-MM-dd" */
  tanggal: string;
  /** Status appointment terkait */
  status: StatusAppointment;
}
