/**
 * @module features/screening/services/screeningService
 *
 * Service layer untuk fitur skrining osteoporosis. Bertugas mengirim
 * citra X-ray ke backend Spring Boot dan mengambil riwayat skrining
 * milik user yang sedang login. Backend Spring Boot meneruskan citra
 * ke layanan ML (FastAPI), menyimpan hasil, lalu mengembalikannya.
 */
import api from "../../../core/api/client";
import type { ScreeningResult } from "../types";

/**
 * Upload citra X-ray untuk skrining osteoporosis.
 *
 * Dikirim sebagai `multipart/form-data` dengan key `"file"` — key ini
 * HARUS sama dengan `@RequestParam("file")` pada controller Spring Boot.
 * JWT token disisipkan otomatis oleh interceptor pada API client.
 *
 * @param file - berkas gambar X-ray (JPG/PNG). Validasi tipe & ukuran
 *   (maks 10MB) dilakukan di pemanggil sebelum fungsi ini dipanggil.
 * @returns Promise Axios berisi {@link ScreeningResult} (label, confidence,
 *   distribusi probabilitas, dan citra Grad-CAM)
 * @throws AxiosError - jika upload gagal, server tidak aktif, atau token
 *   tidak valid (401)
 *
 * @example
 * const res = await uploadScreening(file);
 * console.log(res.data.label); // "Osteoporosis"
 */
export const uploadScreening = (file: File) => {
  const formData = new FormData();
  formData.append("file", file); // key "file" HARUS sama dengan @RequestParam("file") di Spring

  return api.post<ScreeningResult>("/screening/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

/**
 * Mengambil riwayat skrining milik user yang sedang login.
 *
 * User diidentifikasi lewat JWT token yang otomatis terpasang oleh
 * interceptor API client.
 *
 * @returns Promise Axios berisi array {@link ScreeningResult}, terurut
 *   sesuai respons backend
 * @throws AxiosError - jika token tidak valid (401) atau server error
 *
 * @example
 * const res = await getScreeningHistory();
 * res.data.forEach((r) => console.log(r.label, r.createdAt));
 */
export const getScreeningHistory = () => {
  return api.get<ScreeningResult[]>("/screening/history");
};
