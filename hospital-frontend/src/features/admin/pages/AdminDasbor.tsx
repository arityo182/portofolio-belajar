import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Users, Stethoscope, Building2, CalendarDays, Loader2, AlertTriangle,
  ArrowRight,
} from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import { getDashboard } from "../../admin/services/adminService";
import type { DashboardStats } from "../../admin/types";

/** Kartu statistik untuk dashboard. */
interface StatCard {
  label: string;
  value: number;
  icon: typeof Users;
  to: string;
  color: string;
  bg: string;
}

/**
 * Halaman dashboard admin — statistik ringkas + shortcut ke menu.
 *
 * @returns Halaman dashboard
 */
export default function AdminDasbor() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let aktif = true;
    setLoading(true);
    getDashboard()
      .then((res) => aktif && setStats(res.data))
      .catch((err) => aktif && setError(toErrorMessage(err)))
      .finally(() => aktif && setLoading(false));
    return () => { aktif = false; };
  }, []);

  if (loading) {
    return (
      <div className="flex flex-col items-center gap-3 py-20">
        <Loader2 className="animate-spin" size={28} color={C.navy} />
        <p className="text-sm" style={{ color: C.grey }}>Memuat dashboard...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center gap-3 py-20 text-center">
        <AlertTriangle size={30} color={C.orange} />
        <p className="text-sm" style={{ color: C.grey }}>{error}</p>
      </div>
    );
  }

  const cards: StatCard[] = stats ? [
    { label: "Total Pasien", value: stats.totalPasien, icon: Users, to: "/admin/pengguna", color: C.navy, bg: C.blueSoft },
    { label: "Total Dokter", value: stats.totalDokter, icon: Stethoscope, to: "/admin/dokter", color: C.orange, bg: C.orangeSoft },
    { label: "Total Poli", value: stats.totalPoli, icon: Building2, to: "/admin/poli", color: C.navy, bg: C.blueSoft },
    { label: "Total Appointment", value: stats.totalAppointment, icon: CalendarDays, to: "/admin/jadwal", color: C.orange, bg: C.orangeSoft },
  ] : [];

  return (
    <div>
      <h1 className="text-2xl font-bold" style={{ color: C.navy }}>Dasbor</h1>
      <p className="mt-1 text-sm" style={{ color: C.grey }}>
        Ringkasan data RS Medika Sentosa
      </p>

      <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {cards.map(({ label, value, icon: Icon, to, color, bg }) => (
          <Link
            key={label}
            to={to}
            className="group rounded-3xl border p-5 transition-all hover:-translate-y-1 hover:shadow-md"
            style={{ backgroundColor: C.white, borderColor: C.line }}
          >
            <div
              className="flex h-12 w-12 items-center justify-center rounded-2xl"
              style={{ backgroundColor: bg }}
            >
              <Icon size={24} color={color} />
            </div>
            <p className="mt-4 text-3xl font-bold" style={{ color: C.navy }}>{value}</p>
            <p className="text-sm" style={{ color: C.grey }}>{label}</p>
            <span
              className="mt-3 inline-flex items-center gap-1 text-xs font-semibold opacity-0 transition-opacity group-hover:opacity-100"
              style={{ color: C.orange }}
            >
              Kelola <ArrowRight size={13} />
            </span>
          </Link>
        ))}
      </div>
    </div>
  );
}
