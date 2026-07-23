import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

/**
 * Membuat instance STOMP client terhubung ke backend WebSocket endpoint /ws.
 *
 * @param jwtToken — JWT token dari localStorage untuk autentikasi
 * @returns STOMP Client instance yang sudah dikonfigurasi (belum aktif)
 */
export function createSocketClient(jwtToken: string): Client {
  return new Client({
    webSocketFactory: () => new SockJS("http://localhost:8080/ws"),
    connectHeaders: {
      Authorization: `Bearer ${jwtToken}`,
    },
    reconnectDelay: 5000, // auto-reconnect tiap 5 detik jika putus
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    debug: () => {}, // silent di production
  });
}

/** Singleton client untuk dipakai di seluruh aplikasi. */
let stompClient: Client | null = null;

/** Token terakhir yang dipakai connect (untuk deteksi perubahan). */
let lastToken: string | null = null;

/**
 * Connect atau reconnect STOMP client dengan token terbaru.
 * Aman dipanggil berkali-kali — jika token sama, tidak reconnect.
 * Jika gagal (mis. WebSocket endpoint 403), tidak throw error.
 *
 * @param token JWT token dari auth context
 */
export function connectSocket(token: string): void {
  try {
    if (stompClient && lastToken === token && stompClient.active) return;
    if (stompClient) { stompClient.deactivate(); }
    stompClient = createSocketClient(token);
    lastToken = token;
    stompClient.activate();
  } catch { /* silent fail — WebSocket is non-critical */ }
}

/**
 * Disconnect & reset client (panggil saat logout).
 */
export function disconnectSocket(): void {
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
    lastToken = null;
  }
}

/**
 * Subscribe ke topic notifikasi untuk pasien tertentu.
 * Callback dipanggil setiap kali ada pesan masuk.
 *
 * @param token JWT token
 * @param pasienId ID pasien untuk subscribe ke /topic/pasien/{id}
 * @param onMessage callback saat pesan diterima
 * @returns fungsi untuk unsubscribe
 */
export function subscribePasien(
  token: string,
  pasienId: number,
  onMessage: (payload: Record<string, unknown>) => void
): () => void {
  try {
    connectSocket(token);
    if (!stompClient) return () => {};
    const sub = stompClient.subscribe(`/topic/pasien/${pasienId}`, (msg) => {
      try {
        const body = JSON.parse(msg.body) as Record<string, unknown>;
        onMessage(body);
      } catch { /* ignore */ }
    });
    return () => { try { sub.unsubscribe(); } catch { /* ignore */ } };
  } catch {
    return () => {}; // WebSocket gagal = no-op, halaman tetap jalan
  }
}
