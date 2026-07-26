/**
 * Service layer untuk chat publik (tanpa login).
 * Menggunakan fetch langsung (tanpa JWT) ke endpoint /api/chat/public/message.
 */
import axios from "axios";
import type { FloatingMessage, PublicChatResponse } from "../types";

/** Axios instance tanpa interceptor JWT (endpoint publik). */
const publicApi = axios.create({
  baseURL: "/api",
  headers: { "Content-Type": "application/json" },
});

/**
 * Kirim pesan ke Senta sebagai pengguna publik (tanpa login).
 * Tidak menyimpan ke DB.
 *
 * @param message - Teks pertanyaan user
 * @param history - Riwayat percakapan di sesi lokal (maks 10 terakhir)
 * @returns string respons dari Senta
 */
export async function sendPublicMessage(
  message: string,
  history: FloatingMessage[]
): Promise<string> {
  const recent = history.slice(-10).map((m) => ({
    role: m.role,
    content: m.content,
  }));

  const { data } = await publicApi.post<PublicChatResponse>(
    "/chat/public/message",
    { message, sessionHistory: recent }
  );
  return data.response;
}
