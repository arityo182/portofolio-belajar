/**
 * @module core/api/client
 *
 * HTTP client terpusat berbasis Axios untuk seluruh komunikasi frontend
 * dengan backend Spring Boot (base URL `/api`).
 *
 * Access token disimpan HANYA di memori (bukan localStorage) sehingga tidak
 * dapat dibaca oleh skrip XSS. Sesi bertahan setelah reload melalui refresh
 * token HttpOnly yang dikirim otomatis oleh browser (`withCredentials`).
 *
 * Bila access token kedaluwarsa (401), client mencoba satu kali refresh dan
 * mengulang request. Bila refresh gagal, event `auth:expired` dipancarkan agar
 * AuthContext membersihkan state dan route guard mengarahkan ke login.
 *
 * @example
 * import api from "../../../core/api/client";
 * const res = await api.get("/screening/history"); // token otomatis terpasang
 */
import axios from "axios";

export const AUTH_EXPIRED_EVENT = "auth:expired";

let accessToken = null;
let refreshPromise = null;

/** Menyimpan access token di memori (bukan storage). */
export function setAccessToken(token) {
    accessToken = token || null;
}

/** Mengambil access token yang sedang aktif. */
export function getAccessToken() {
    return accessToken;
}

// Di Docker: Nginx proxy /api → backend:8080
// Di local dev: Vite proxy /api → localhost:8080 (via vite.config.js)
const api = axios.create({
    baseURL: "/api",
    withCredentials: true,
    headers: { "Content-Type": "application/json" },
});

// Sisipkan access token otomatis di setiap request.
api.interceptors.request.use((config) => {
    if (accessToken) {
        config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
});

/**
 * Menukar refresh cookie menjadi access token baru.
 * Single-flight: request 401 yang bersamaan berbagi satu proses refresh.
 *
 * @returns {Promise<{token: string, user: object}>} data sesi baru
 */
export function refreshSession() {
    if (!refreshPromise) {
        refreshPromise = api
            .post("/auth/refresh", null, { _skipAuthRefresh: true })
            .then((response) => {
                setAccessToken(response.data.token);
                return response.data;
            })
            .catch((error) => {
                setAccessToken(null);
                window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
                throw error;
            })
            .finally(() => {
                refreshPromise = null;
            });
    }
    return refreshPromise;
}

// Jika 401, coba refresh sekali lalu ulangi request semula.
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const original = error.config;
        const status = error.response?.status;
        const url = original?.url || "";

        const isAuthEndpoint =
            url.includes("/auth/login") ||
            url.includes("/auth/register") ||
            url.includes("/auth/refresh") ||
            url.includes("/auth/logout");

        if (!original || status !== 401 || original._retry || isAuthEndpoint) {
            return Promise.reject(error);
        }

        original._retry = true;
        try {
            const session = await refreshSession();
            original.headers = original.headers || {};
            original.headers.Authorization = `Bearer ${session.token}`;
            return api(original);
        } catch {
            return Promise.reject(error);
        }
    }
);

export default api;
