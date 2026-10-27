/**
 * Service layer untuk komunikasi dengan backend Chat API.
 * Semua request menggunakan JWT token dari core/api/client.
 */
import api from "../../../core/api/client";
/** Kirim pesan ke Senta. sessionId = null untuk buat sesi baru. */
export async function sendMessage(req) {
    const { data } = await api.post("/chat/message", req);
    return data;
}
/** Ambil daftar sesi chat user yang login. */
export async function getSessions() {
    const { data } = await api.get("/chat/sessions");
    return data;
}
/** Ambil history percakapan satu sesi. */
export async function getHistory(sessionId) {
    const { data } = await api.get(`/chat/sessions/${sessionId}/messages`);
    return data;
}
/** Hapus sesi chat. */
export async function deleteSession(sessionId) {
    await api.delete(`/chat/sessions/${sessionId}`);
}
