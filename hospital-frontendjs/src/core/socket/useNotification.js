import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { subscribeTopic, disconnectSocket } from "../socket/socketClient";

/**
 * Hook generik untuk subscribe notifikasi real-time pada satu topic STOMP.
 *
 * Subscription baru dipasang setelah koneksi WebSocket siap dan otomatis
 * dipasang ulang saat reconnect (lihat socketClient). Bila `topic` null,
 * hook tidak melakukan apa pun.
 *
 * @param topic tujuan STOMP, mis. /topic/pasien/1 atau /topic/dokter/2
 * @param options.enabled set false untuk menunda subscription
 * @returns notifikasi terbaru, atau null
 */
export function useTopicNotification(topic, { enabled = true } = {}) {
    const { token } = useAuth();
    const [notif, setNotif] = useState(null);

    useEffect(() => {
        if (!token || !topic || !enabled) {
            return;
        }
        return subscribeTopic(token, topic, (payload) => {
            setNotif({
                type: payload.type ?? "UNKNOWN",
                appointmentId: payload.appointmentId,
                antrianId: payload.antrianId,
                pasienId: payload.pasienId,
                dokterId: payload.dokterId,
                status: payload.status,
                nomorUrut: payload.nomorUrut,
                message: payload.message ?? "Update diterima",
                timestamp: Date.now(),
            });
        });
    }, [token, topic, enabled]);

    // Disconnect saat logout
    useEffect(() => {
        if (!token) {
            disconnectSocket();
        }
    }, [token]);

    return notif;
}

/**
 * Hook untuk subscribe notifikasi real-time pasien.
 *
 * @param pasienId ID pasien (null = tidak subscribe)
 * @returns notifikasi terbaru, atau null
 *
 * @example
 * const { pasienId } = usePasien(); // dari context sendiri
 * const notif = useNotification(pasienId);
 * if (notif) { alert(notif.message); }
 */
export function useNotification(pasienId) {
    const topic = pasienId ? `/topic/pasien/${pasienId}` : null;
    return useTopicNotification(topic);
}
