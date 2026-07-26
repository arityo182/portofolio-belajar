/** Role pesan dalam chat. */
export type MessageRole = "USER" | "ASSISTANT";

/** Data satu sesi chat. */
export interface ChatSession {
  id: number;
  judul: string;
  createdAt: string;
  updatedAt: string;
}

/** Satu pesan dalam chat. */
export interface ChatMessage {
  id: number;
  sessionId: number;
  sessionJudul: string;
  role: MessageRole;
  content: string;
  createdAt: string;
}

/** Request kirim pesan baru. */
export interface SendMessageRequest {
  message: string;
  sessionId: number | null;
}

/** Response dari backend saat kirim pesan. */
export type SendMessageResponse = ChatMessage;

/** History lengkap satu sesi. */
export interface ChatHistoryResponse {
  session: ChatSession;
  messages: ChatMessage[];
}

/** Data context RS dari backend (untuk dipassing ke Senta). */
export interface ContextData {
  poli: Array<{ id: number; nama: string; deskripsi: string }>;
  dokter: Array<{ id: number; nama: string; spesialisasi: string; namaPoli: string }>;
  jadwal: Array<{ id: number; namaDokter: string; hari: string; jamMulai: string; jamSelesai: string; kuota: number }>;
  rekamMedisAktif: { diagnosa: string; tindakan: string } | null;
}

/** Pesan ringan untuk floating chat widget (tidak perlu sessionId). */
export interface FloatingMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

/** Response dari endpoint publik chat. */
export interface PublicChatResponse {
  response: string;
}
