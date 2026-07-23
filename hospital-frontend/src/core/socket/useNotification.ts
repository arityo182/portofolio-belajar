import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { subscribePasien, disconnectSocket } from "../socket/socketClient";

/** Payload notifikasi dari WebSocket. */
export interface Notifikasi {
  type: string;
  appointmentId?: number;
  status?: string;
  message: string;
  timestamp: number;
}

/**
 * Hook untuk subscribe notifikasi real-time pasien.
 *
 * @param pasienId ID pasien (nil, null = tidak subscribe)
 * @returns notifikasi terbaru, atau null
 *
 * @example
 * const { pasienId } = usePasien(); // dari context sendiri
 * const notif = useNotification(pasienId);
 * if (notif) { alert(notif.message); }
 */
export function useNotification(pasienId: number | null): Notifikasi | null {
  const { token } = useAuth();
  const [notif, setNotif] = useState<Notifikasi | null>(null);

  useEffect(() => {
    if (!token || !pasienId) return;
    const unsub = subscribePasien(token, pasienId, (payload) => {
      setNotif({
        type: (payload.type as string) ?? "UNKNOWN",
        appointmentId: payload.appointmentId as number,
        status: payload.status as string,
        message: (payload.message as string) ?? "Update diterima",
        timestamp: Date.now(),
      });
    });
    return unsub;
  }, [token, pasienId]);

  // Disconnect saat logout
  useEffect(() => {
    if (!token) disconnectSocket();
  }, [token]);

  return notif;
}
