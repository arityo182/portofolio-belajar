/**
 * @module features/auth/services/authService
 *
 * Service layer untuk autentikasi pengguna (registrasi & login) ke
 * endpoint `/auth/*` pada backend Spring Boot. Setiap fungsi
 * mengembalikan `AuthResponse` berisi JWT token dan data user yang
 * kemudian disimpan oleh {@link module:core/context/AuthContext}.
 */
import api from "../../../core/api/client";
import type { User } from "../../../core/types";

/**
 * Payload untuk registrasi akun baru.
 */
export interface RegisterPayload {
  /** Nama lengkap pengguna */
  nama: string;
  /** Email pengguna (dipakai sebagai identitas login) */
  email: string;
  /** Password mentah (di-hash di sisi backend) */
  password: string;
}

/**
 * Payload untuk login pengguna.
 */
export interface LoginPayload {
  /** Email terdaftar */
  email: string;
  /** Password mentah */
  password: string;
}

/**
 * Respons autentikasi dari backend setelah login/register berhasil.
 */
export interface AuthResponse {
  /** JWT token yang dipakai untuk request selanjutnya */
  token: string;
  /** Data user yang sedang login */
  user: User;
}

/**
 * Mendaftarkan akun pengguna baru ke backend.
 *
 * @param data - data registrasi (nama, email, password)
 * @returns Promise Axios berisi {@link AuthResponse} (token + user)
 * @throws AxiosError - jika email sudah terpakai atau validasi gagal (mis. 400/409)
 *
 * @example
 * const res = await registerUser({ nama: "Budi", email: "budi@mail.com", password: "rahasia" });
 * login(res.data.token, res.data.user);
 */
export const registerUser = (data: RegisterPayload) =>
  api.post<AuthResponse>("/auth/register", data);

/**
 * Melakukan login pengguna dan memperoleh JWT token.
 *
 * @param data - kredensial login (email, password)
 * @returns Promise Axios berisi {@link AuthResponse} (token + user)
 * @throws AxiosError - jika kredensial salah (mis. 401)
 *
 * @example
 * const res = await loginUser({ email: "budi@mail.com", password: "rahasia" });
 * console.log(res.data.user.nama);
 */
export const loginUser = (data: LoginPayload) =>
  api.post<AuthResponse>("/auth/login", data);
