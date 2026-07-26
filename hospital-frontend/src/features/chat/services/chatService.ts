/**
 * Service layer untuk komunikasi dengan backend Chat API.
 * Semua request menggunakan JWT token dari core/api/client.
 */
import api from "../../../core/api/client";
import type {
  ChatMessage,
  ChatSession,
  SendMessageRequest,
  ChatHistoryResponse,
} from "../types";

/** Kirim pesan ke Senta. sessionId = null untuk buat sesi baru. */
export async function sendMessage(
  req: SendMessageRequest
): Promise<ChatMessage> {
  const { data } = await api.post<ChatMessage>("/chat/message", req);
  return data;
}

/** Ambil daftar sesi chat user yang login. */
export async function getSessions(): Promise<ChatSession[]> {
  const { data } = await api.get<ChatSession[]>("/chat/sessions");
  return data;
}

/** Ambil history percakapan satu sesi. */
export async function getHistory(
  sessionId: number
): Promise<ChatHistoryResponse> {
  const { data } = await api.get<ChatHistoryResponse>(
    `/chat/sessions/${sessionId}/messages`
  );
  return data;
}

/** Hapus sesi chat. */
export async function deleteSession(sessionId: number): Promise<void> {
  await api.delete(`/chat/sessions/${sessionId}`);
}
