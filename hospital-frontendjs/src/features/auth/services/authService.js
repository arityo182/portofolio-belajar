/**
 * @module features/auth/services/authService
 *
 * Service layer untuk autentikasi pengguna (registrasi & login) ke
 * endpoint `/auth/*` pada backend Spring Boot. Setiap fungsi
 * mengembalikan `AuthResponse` berisi JWT token dan data user yang
 * kemudian disimpan oleh {@link module:core/context/AuthContext}.
 */
import api from "../../../core/api/client";
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
export const registerUser = (data) => api.post("/auth/register", data);
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
export const loginUser = (data) => api.post("/auth/login", data);
