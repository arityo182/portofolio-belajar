import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { AxiosError } from "axios";
import { Loader2, AlertTriangle, CalendarPlus, ClipboardList, Bell } from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import { useAuth } from "../../../core/context/AuthContext";
import { useNotification } from "../../../core/socket/useNotification";
import { getPasienByUserId } from "../../pasien/services/pasienService";
import {
  getAppointmentsByPasien,
  updateAppointmentStatus,
} from "../services/appointmentService";
import type { AppointmentResponse } from "../types";
import AppointmentCard from "../components/AppointmentCard";

/**
 * Halaman "Appointment Saya" (FR-11).
 *
 * Menampilkan seluruh janji temu milik pasien yang sedang login beserta
 * status dan nomor antriannya. Pasien juga dapat membatalkan janji yang
 * masih berstatus MENUNGGU. `pasienId` diperoleh dengan menerjemahkan
 * `user.id` (dari context auth) melalui `GET /api/pasien/user/{userId}`.
 *
 * @returns Halaman daftar appointment pasien
 */
export default function AppointmentSayaPage() {
  const { user } = useAuth();

  const [appointments, setAppointments] = useState<AppointmentResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  /** true bila user login belum memiliki profil pasien (404). */
  const [belumTerdaftar, setBelumTerdaftar] = useState(false);
  /** ID appointment yang sedang dibatalkan (untuk state tombol). */
  const [cancellingId, setCancellingId] = useState<number | null>(null);

  // WebSocket notifikasi real-time
  const [pasienIdForNotif, setPasienIdForNotif] = useState<number | null>(null);
  const notif = useNotification(pasienIdForNotif);
  const [toast, setToast] = useState<string | null>(null);

  const muat = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    setError(null);
    setBelumTerdaftar(false);
    try {
      const pasien = await getPasienByUserId(user.id);
      setPasienIdForNotif(pasien.data.id);
      const res = await getAppointmentsByPasien(pasien.data.id);
      // Tampilkan yang terbaru di atas.
      setAppointments([...res.data].sort((a, b) => b.id - a.id));
    } catch (err) {
      if (err instanceof AxiosError && err.response?.status === 404) {
        setBelumTerdaftar(true);
      } else {
        setError(toErrorMessage(err));
      }
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    muat();
  }, [muat]);

  // Tampilkan toast saat ada notifikasi WebSocket
  useEffect(() => {
    if (notif) { setToast(notif.message); setTimeout(() => setToast(null), 5000); }
  }, [notif]);

  /** Membatalkan sebuah appointment lalu memuat ulang daftar. */
  const handleCancel = async (id: number) => {
    setCancellingId(id);
    try {
      await updateAppointmentStatus(id, "BATAL");
      await muat();
    } catch (err) {
      setError(toErrorMessage(err));
    } finally {
      setCancellingId(null);
    }
  };

  return (
    <div>
      {/* Toast notifikasi WebSocket */}
      {toast && (
        <div className="fixed right-5 top-20 z-50 flex items-center gap-3 rounded-2xl border px-5 py-4 shadow-lg animate-bounce" style={{backgroundColor:C.white,borderColor:C.orange}}>
          <Bell size={20} color={C.orange}/>
          <p className="text-sm font-semibold" style={{color:C.navy}}>{toast}</p>
        </div>
      )}
      {/* Hero */}
      <section style={{ backgroundColor: C.navy }} className="relative overflow-hidden">
        <div
          className="pointer-events-none absolute -right-20 -top-16 h-64 w-64 rounded-full opacity-15"
          style={{ backgroundColor: C.blue }}
        />
        <div className="relative mx-auto max-w-5xl px-5 py-14 md:py-16">
          <span
            className="inline-flex items-center gap-2 rounded-full px-3.5 py-1.5 text-xs font-semibold"
            style={{ backgroundColor: "rgba(255,255,255,0.12)", color: C.blue }}
          >
            <ClipboardList size={14} /> Appointment Saya
          </span>
          <h1 className="mt-5 text-4xl font-bold leading-tight text-white md:text-5xl">
            Janji temu Anda
          </h1>
          <p className="mt-4 max-w-xl text-base leading-relaxed" style={{ color: "#C9D2EC" }}>
            Pantau status janji temu dan nomor antrian Anda di sini.
          </p>
        </div>
      </section>

      <section className="mx-auto max-w-5xl px-5 py-12">
        <div className="mb-6 flex items-center justify-between">
          <h2 className="text-xl font-bold" style={{ color: C.navy }}>Daftar Janji Temu</h2>
          <Link
            to="/booking"
            className="inline-flex items-center gap-2 rounded-full px-5 py-2.5 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5"
            style={{ backgroundColor: C.orange }}
          >
            <CalendarPlus size={17} /> Buat Janji Temu
          </Link>
        </div>

        {loading ? (
          <div className="flex flex-col items-center gap-3 py-16">
            <Loader2 className="animate-spin" size={28} color={C.navy} />
            <p className="text-sm" style={{ color: C.grey }}>Memuat appointment...</p>
          </div>
        ) : belumTerdaftar ? (
          <div
            className="flex flex-col items-center gap-3 rounded-3xl border border-dashed px-6 py-16 text-center"
            style={{ borderColor: C.line }}
          >
            <AlertTriangle size={30} color={C.orange} />
            <p className="text-base font-semibold" style={{ color: C.navy }}>
              Profil pasien belum lengkap
            </p>
            <p className="max-w-md text-sm" style={{ color: C.grey }}>
              Anda belum memiliki profil pasien. Lengkapi data pasien terlebih dahulu
              sebelum membuat janji temu. (Pelengkapan profil tersedia pada fitur pasien.)
            </p>
          </div>
        ) : error ? (
          <div
            className="flex flex-col items-center gap-3 rounded-3xl border border-dashed px-6 py-16 text-center"
            style={{ borderColor: C.line }}
          >
            <AlertTriangle size={30} color={C.orange} />
            <p className="text-sm" style={{ color: C.grey }}>{error}</p>
            <button
              type="button"
              onClick={muat}
              className="mt-1 rounded-full px-5 py-2 text-sm font-semibold text-white"
              style={{ backgroundColor: C.navy }}
            >
              Coba lagi
            </button>
          </div>
        ) : appointments.length === 0 ? (
          <div
            className="flex flex-col items-center gap-3 rounded-3xl border border-dashed px-6 py-16 text-center"
            style={{ borderColor: C.line }}
          >
            <ClipboardList size={30} color={C.grey} />
            <p className="text-base font-semibold" style={{ color: C.navy }}>
              Belum ada janji temu
            </p>
            <p className="text-sm" style={{ color: C.grey }}>
              Buat janji temu pertama Anda dengan menekan tombol di atas.
            </p>
          </div>
        ) : (
          <div className="grid gap-4">
            {appointments.map((a) => (
              <AppointmentCard
                key={a.id}
                appointment={a}
                onCancel={handleCancel}
                cancelling={cancellingId === a.id}
              />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
