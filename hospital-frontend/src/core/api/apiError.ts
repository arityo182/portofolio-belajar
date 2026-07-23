/**
 * @module core/api/apiError
 *
 * Utilitas kecil untuk menerjemahkan error (umumnya {@link AxiosError})
 * menjadi pesan berbahasa Indonesia yang ramah pengguna. Dipakai lintas
 * fitur agar penanganan error konsisten.
 */
import { AxiosError } from "axios";

/**
 * Menerjemahkan error menjadi pesan yang layak ditampilkan ke pengguna.
 *
 * Prioritas: pesan `message` dari body error backend (mis. `ErrorResponse`),
 * lalu pemetaan khusus untuk 401 (belum login) dan kegagalan jaringan,
 * dan terakhir pesan generik.
 *
 * @param err - error yang ditangkap (biasanya {@link AxiosError})
 * @param fallback - pesan generik bila tidak ada informasi lain
 * @returns pesan error yang siap ditampilkan
 *
 * @example
 * try { await createAppointment(payload); }
 * catch (e) { setError(toErrorMessage(e)); } // "Kuota jadwal pada tanggal ini sudah penuh"
 */
export function toErrorMessage(
  err: unknown,
  fallback = "Terjadi kesalahan. Coba lagi nanti."
): string {
  if (err instanceof AxiosError) {
    const data = err.response?.data as { message?: string } | undefined;
    if (data?.message) return data.message;
    if (err.response?.status === 401) {
      return "Sesi tidak valid atau belum login. Silakan masuk terlebih dahulu.";
    }
    if (err.code === "ERR_NETWORK") {
      return "Tidak dapat terhubung ke server. Pastikan backend aktif.";
    }
  }
  return fallback;
}
