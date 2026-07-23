import { Stethoscope, CalendarDays, Clock, Ticket, X } from "lucide-react";
import { C } from "../../../core/theme";
import type { AppointmentResponse } from "../types";
import StatusBadge from "./StatusBadge";

/**
 * Properti komponen {@link AppointmentCard}.
 */
interface AppointmentCardProps {
  /** Data appointment yang ditampilkan */
  appointment: AppointmentResponse;
  /**
   * Callback pembatalan (opsional). Bila diberikan dan status masih
   * MENUNGGU, tombol "Batalkan" ditampilkan. Menerima ID appointment.
   */
  onCancel?: (id: number) => void;
  /** `true` saat proses pembatalan kartu ini sedang berjalan */
  cancelling?: boolean;
}

/** Memangkas "HH:mm:ss" menjadi "HH:mm". */
function jam(waktu: string): string {
  return waktu.slice(0, 5);
}

/**
 * Memformat tanggal "yyyy-MM-dd" menjadi teks Indonesia, mis. "13 Juli 2026".
 *
 * @param iso - tanggal format "yyyy-MM-dd"
 * @returns tanggal ramah-baca; mengembalikan input apa adanya bila tak valid
 *
 * @example
 * formatTanggal("2026-07-13"); // "13 Juli 2026"
 */
function formatTanggal(iso: string): string {
  const [y, m, d] = iso.split("-").map(Number);
  if (!y || !m || !d) return iso;
  const bulan = [
    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
    "Juli", "Agustus", "September", "Oktober", "November", "Desember",
  ];
  return `${d} ${bulan[m - 1]} ${y}`;
}

/**
 * Kartu ringkasan satu appointment untuk halaman "Appointment Saya".
 *
 * Menampilkan dokter, poli, jadwal (hari/jam/tanggal), keluhan, badge
 * status, dan nomor antrian bila tersedia. Tombol "Batalkan" muncul hanya
 * bila `onCancel` diberikan dan status masih MENUNGGU.
 *
 * @param props - lihat {@link AppointmentCardProps}
 * @returns Elemen kartu appointment
 *
 * @example
 * <AppointmentCard appointment={a} onCancel={handleCancel} cancelling={busyId === a.id} />
 */
export default function AppointmentCard({
  appointment,
  onCancel,
  cancelling = false,
}: AppointmentCardProps) {
  const bisaBatal = onCancel && appointment.status === "MENUNGGU";

  return (
    <div
      className="rounded-3xl border p-5"
      style={{ backgroundColor: C.white, borderColor: C.line }}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-center gap-3">
          <div
            className="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full"
            style={{ backgroundColor: C.blueSoft }}
          >
            <Stethoscope size={24} color={C.navy} />
          </div>
          <div className="min-w-0">
            <h3 className="truncate text-base font-bold" style={{ color: C.navy }}>
              {appointment.namaDokter}
            </h3>
            <p className="truncate text-sm" style={{ color: C.grey }}>
              {appointment.namaPoli}
            </p>
          </div>
        </div>
        <StatusBadge status={appointment.status} />
      </div>

      <div className="mt-4 flex flex-wrap items-center gap-x-4 gap-y-2">
        <div className="flex items-center gap-1.5">
          <CalendarDays size={16} color={C.orange} />
          <span className="text-sm font-medium" style={{ color: C.grey }}>
            {formatTanggal(appointment.tanggal)}
          </span>
        </div>
        <div className="flex items-center gap-1.5">
          <Clock size={16} color={C.orange} />
          <span className="text-sm font-medium" style={{ color: C.grey }}>
            {jam(appointment.jamMulai)} &ndash; {jam(appointment.jamSelesai)}
          </span>
        </div>
      </div>

      {appointment.keluhan && (
        <p className="mt-3 text-sm leading-relaxed" style={{ color: C.grey }}>
          <span className="font-semibold" style={{ color: C.navy }}>Keluhan: </span>
          {appointment.keluhan}
        </p>
      )}

      <div className="mt-4 flex items-center justify-between">
        {appointment.nomorAntrian !== null ? (
          <div
            className="flex items-center gap-2 rounded-2xl px-4 py-2"
            style={{ backgroundColor: C.navy }}
          >
            <Ticket size={18} color={C.white} />
            <span className="text-sm font-semibold text-white">
              Antrian No. {appointment.nomorAntrian}
            </span>
          </div>
        ) : (
          <span className="text-xs" style={{ color: C.grey }}>
            Nomor antrian belum tersedia
          </span>
        )}

        {bisaBatal && (
          <button
            type="button"
            onClick={() => onCancel!(appointment.id)}
            disabled={cancelling}
            className="flex items-center gap-1.5 rounded-full px-4 py-2 text-sm font-semibold transition-opacity hover:opacity-80 disabled:opacity-50"
            style={{ backgroundColor: "#FCE8E6", color: "#C0392B" }}
          >
            <X size={15} /> {cancelling ? "Membatalkan..." : "Batalkan"}
          </button>
        )}
      </div>
    </div>
  );
}
