/**
 * @module core/api/client
 *
 * HTTP client terpusat berbasis Axios untuk seluruh komunikasi frontend
 * dengan backend Spring Boot (base URL `http://localhost:8080/api`).
 *
 * Instance ini dilengkapi request interceptor yang secara otomatis
 * menyisipkan JWT token (dari `localStorage`) ke header `Authorization`
 * pada setiap request, sehingga service layer tidak perlu menangani
 * token secara manual.
 *
 * @example
 * import api from "../../../core/api/client";
 * const res = await api.get("/screening/history"); // token otomatis terpasang
 */
import axios from "axios";

// Di Docker: Nginx proxy /api → backend:8080
// Di local dev: Vite proxy /api → localhost:8080 (via vite.config.ts)
const api = axios.create({
  baseURL: "/api",
  headers: { "Content-Type": "application/json" },
});

// Sisipkan JWT token otomatis di setiap request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
