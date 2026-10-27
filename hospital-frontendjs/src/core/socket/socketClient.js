import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const WS_BASE = import.meta.env.VITE_WS_URL || window.location.origin;

/**
 * Membuat instance STOMP client terhubung ke backend WebSocket endpoint /ws.
 *
 * @param jwtToken access token dari memori (AuthContext), bukan localStorage
 * @returns STOMP Client instance yang sudah dikonfigurasi (belum aktif)
 */
export function createSocketClient(jwtToken) {
    return new Client({
        webSocketFactory: () => new SockJS(`${WS_BASE}/ws`),
        connectHeaders: {
            Authorization: `Bearer ${jwtToken}`,
        },
        reconnectDelay: 5000, // auto-reconnect tiap 5 detik jika putus
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        debug: () => { }, // silent di production
    });
}

/** Singleton client untuk dipakai di seluruh aplikasi. */
let stompClient = null;
/** Token terakhir yang dipakai connect (untuk deteksi perubahan). */
let lastToken = null;
/** Status koneksi STOMP saat ini. */
let connected = false;

/**
 * Registry subscription yang tahan reconnect. Callback didaftarkan lebih dulu,
 * lalu subscription STOMP dibuat saat koneksi benar-benar siap. Ini memperbaiki
 * bug lama yang subscribe sebelum onConnect sehingga pesan tidak pernah masuk.
 *
 * destination -> { callbacks: Set<Function>, subscription: object|null }
 */
const registry = new Map();

function safeUnsubscribe(entry) {
    if (!entry?.subscription) {
        return;
    }
    try {
        entry.subscription.unsubscribe();
    }
    catch { /* ignore */ }
    entry.subscription = null;
}

function clearSubscriptionHandles() {
    for (const entry of registry.values()) {
        safeUnsubscribe(entry);
    }
}

function activateSubscription(client, destination) {
    const entry = registry.get(destination);
    if (!entry || entry.subscription || !client?.connected) {
        return;
    }
    try {
        entry.subscription = client.subscribe(destination, (msg) => {
            let body;
            try {
                body = JSON.parse(msg.body);
            }
            catch {
                return;
            }
            // Salin callback agar aman bila ada unsubscribe saat iterasi.
            for (const callback of Array.from(entry.callbacks)) {
                try {
                    callback(body);
                }
                catch { /* callback error tidak boleh memutus socket */ }
            }
        });
    }
    catch { /* subscribe gagal; akan dicoba lagi saat reconnect */ }
}

function resubscribeAll(client) {
    for (const destination of registry.keys()) {
        activateSubscription(client, destination);
    }
}

/**
 * Connect atau reconnect STOMP client dengan token terbaru.
 * Aman dipanggil berkali-kali — jika token sama dan koneksi aktif, tidak reconnect.
 * Subscription yang sudah terdaftar akan otomatis dipasang ulang setelah
 * koneksi/reconnect berhasil.
 *
 * @param token JWT token dari auth context
 */
export function connectSocket(token) {
    if (!token) {
        return;
    }
    if (stompClient && lastToken === token && stompClient.active) {
        if (stompClient.connected) {
            resubscribeAll(stompClient);
        }
        return;
    }

    if (stompClient) {
        const old = stompClient;
        stompClient = null;
        connected = false;
        clearSubscriptionHandles();
        try {
            old.deactivate();
        }
        catch { /* ignore */ }
    }

    const client = createSocketClient(token);
    stompClient = client;
    lastToken = token;

    client.onConnect = () => {
        if (client !== stompClient) {
            return;
        }
        connected = true;
        resubscribeAll(client);
    };
    client.onDisconnect = () => {
        if (client === stompClient) {
            connected = false;
            clearSubscriptionHandles();
        }
    };
    client.onWebSocketClose = () => {
        if (client === stompClient) {
            connected = false;
            clearSubscriptionHandles();
        }
    };

    try {
        client.activate();
    }
    catch { /* silent fail — WebSocket non-kritis */ }
}

/**
 * Disconnect & reset client (panggil saat logout).
 */
export function disconnectSocket() {
    const client = stompClient;
    stompClient = null;
    lastToken = null;
    connected = false;
    clearSubscriptionHandles();
    registry.clear();
    if (client) {
        try {
            client.deactivate();
        }
        catch { /* ignore */ }
    }
}

/**
 * Subscribe ke sebuah topic. Callback didaftarkan ke registry sehingga tetap
 * aktif setelah reconnect. Bila koneksi belum siap, subscription akan dipasang
 * otomatis pada onConnect.
 *
 * @param token JWT token
 * @param destination tujuan STOMP, mis. /topic/dokter/1
 * @param onMessage callback saat pesan diterima
 * @returns fungsi untuk unsubscribe
 */
export function subscribeTopic(token, destination, onMessage) {
    if (!token || !destination || typeof onMessage !== "function") {
        return () => { };
    }

    connectSocket(token);

    let entry = registry.get(destination);
    if (!entry) {
        entry = { callbacks: new Set(), subscription: null };
        registry.set(destination, entry);
    }
    entry.callbacks.add(onMessage);

    if (connected && stompClient?.connected) {
        activateSubscription(stompClient, destination);
    }

    return () => {
        const current = registry.get(destination);
        if (!current) {
            return;
        }
        current.callbacks.delete(onMessage);
        if (current.callbacks.size === 0) {
            safeUnsubscribe(current);
            registry.delete(destination);
        }
    };
}

/**
 * Subscribe ke topic notifikasi untuk pasien tertentu.
 *
 * @param token JWT token
 * @param pasienId ID pasien untuk subscribe ke /topic/pasien/{id}
 * @param onMessage callback saat pesan diterima
 * @returns fungsi untuk unsubscribe
 */
export function subscribePasien(token, pasienId, onMessage) {
    return subscribeTopic(token, `/topic/pasien/${pasienId}`, onMessage);
}
