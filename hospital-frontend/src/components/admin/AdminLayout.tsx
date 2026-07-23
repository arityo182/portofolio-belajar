import { useState } from "react";
import { Link, Outlet, useLocation, useNavigate } from "react-router-dom";
import {
  LayoutDashboard, Building2, Stethoscope, CalendarDays,
  Users, LogOut, Menu, X, HeartPulse, Ticket, UserCog, Pill,
  FlaskConical, Image, Bed, Scissors, FileText, Pencil,
} from "lucide-react";
import { C, RS_NAME } from "../../core/theme";
import { useAuth } from "../../core/context/AuthContext";

/** Item menu sidebar admin. */
interface MenuItem {
  label: string;
  to: string;
  icon: typeof LayoutDashboard;
}

/** Daftar menu admin. */
const MENU: MenuItem[] = [
  { label: "Dasbor", to: "/admin/dasbor", icon: LayoutDashboard },
  { label: "Manajemen Poli", to: "/admin/poli", icon: Building2 },
  { label: "Manajemen Dokter", to: "/admin/dokter", icon: Stethoscope },
  { label: "Manajemen Jadwal", to: "/admin/jadwal", icon: CalendarDays },
  { label: "Manajemen Pasien", to: "/admin/pasien", icon: UserCog },
  { label: "Papan Antrian", to: "/admin/antrian", icon: Ticket },
  { label: "Manajemen Obat", to: "/admin/obat", icon: Pill },
  { label: "Laboratorium", to: "/admin/lab", icon: FlaskConical },
  { label: "Radiologi", to: "/admin/radiologi", icon: Image },
  { label: "Rawat Inap", to: "/admin/rawat-inap", icon: Bed },
  { label: "Jadwal Operasi", to: "/admin/operasi", icon: Scissors },
  { label: "Billing", to: "/admin/billing", icon: FileText },
  { label: "Konten Halaman", to: "/admin/content", icon: Pencil },
  { label: "Manajemen Pengguna", to: "/admin/pengguna", icon: Users },
];

/**
 * Layout khusus halaman admin dengan sidebar navigasi.
 *
 * Berbeda dari layout publik (navbar atas), admin pakai sidebar kiri
 * dengan menu dasbor/poli/dokter/jadwal/pengguna. Header menampilkan
 * nama admin + tombol logout.
 *
 * @returns Layout admin dengan sidebar + konten
 */
export default function AdminLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  /** Logout lalu kembali ke beranda. */
  const handleLogout = () => {
    logout();
    navigate("/");
  };

  /** Cek apakah menu aktif berdasarkan path saat ini. */
  const isActive = (to: string) => location.pathname === to;

  return (
    <div
      className="flex min-h-screen flex-col"
      style={{ backgroundColor: "#F4F5F7", fontFamily: "'Poppins', system-ui, sans-serif" }}
    >
      {/* Header */}
      <header
        className="sticky top-0 z-40 flex flex-shrink-0 items-center justify-between border-b px-4 py-3 md:px-6"
        style={{ backgroundColor: C.white, borderColor: C.line }}
      >
        <div className="flex items-center gap-3">
          <button
            className="md:hidden"
            onClick={() => setSidebarOpen(!sidebarOpen)}
            aria-label="Toggle menu"
          >
            {sidebarOpen ? <X size={22} color={C.navy} /> : <Menu size={22} color={C.navy} />}
          </button>
          <div className="flex items-center gap-2">
            <div
              className="flex h-9 w-9 items-center justify-center rounded-full"
              style={{ backgroundColor: C.navy }}
            >
              <HeartPulse size={18} color={C.white} />
            </div>
            <div className="leading-tight">
              <p className="text-sm font-bold" style={{ color: C.navy }}>Admin Panel</p>
              <p className="text-[9px] font-medium tracking-wider" style={{ color: C.orange }}>
                {RS_NAME}
              </p>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <span className="hidden text-sm font-medium sm:inline" style={{ color: C.navy }}>
            {user?.nama}
          </span>
          <span
            className="rounded-full px-2.5 py-0.5 text-[10px] font-bold"
            style={{ backgroundColor: C.orangeSoft, color: C.orange }}
          >
            ADMIN
          </span>
          <button
            onClick={handleLogout}
            className="flex items-center gap-1.5 rounded-full px-3 py-1.5 text-sm font-semibold text-white"
            style={{ backgroundColor: C.orange }}
          >
            <LogOut size={14} /> <span className="hidden sm:inline">Keluar</span>
          </button>
        </div>
      </header>

      <div className="flex flex-1">
        {/* Sidebar desktop (sticky) */}
        <aside
          className="sticky top-[57px] hidden h-[calc(100vh-57px)] w-60 flex-shrink-0 border-r md:block"
          style={{ backgroundColor: C.white, borderColor: C.line }}
        >
          <nav className="flex flex-col gap-1 p-3">
            {MENU.map(({ label, to, icon: Icon }) => (
              <Link
                key={to}
                to={to}
                className="flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-medium transition-colors"
                style={{
                  backgroundColor: isActive(to) ? C.navy : "transparent",
                  color: isActive(to) ? C.white : C.grey,
                }}
              >
                <Icon size={18} />
                {label}
              </Link>
            ))}
          </nav>
        </aside>

        {/* Sidebar mobile (drawer) */}
        {sidebarOpen && (
          <div className="fixed inset-0 z-30 md:hidden" onClick={() => setSidebarOpen(false)}>
            <div className="absolute inset-0 bg-black/30" />
            <aside
              className="absolute left-0 top-0 h-full w-60 border-r"
              style={{ backgroundColor: C.white, borderColor: C.line }}
              onClick={(e) => e.stopPropagation()}
            >
              <div className="flex items-center justify-between border-b px-4 py-3" style={{ borderColor: C.line }}>
                <span className="text-sm font-bold" style={{ color: C.navy }}>Menu</span>
                <button onClick={() => setSidebarOpen(false)} aria-label="Tutup">
                  <X size={20} color={C.navy} />
                </button>
              </div>
              <nav className="flex flex-col gap-1 p-3">
                {MENU.map(({ label, to, icon: Icon }) => (
                  <Link
                    key={to}
                    to={to}
                    onClick={() => setSidebarOpen(false)}
                    className="flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-medium transition-colors"
                    style={{
                      backgroundColor: isActive(to) ? C.navy : "transparent",
                      color: isActive(to) ? C.white : C.grey,
                    }}
                  >
                    <Icon size={18} />
                    {label}
                  </Link>
                ))}
              </nav>
            </aside>
          </div>
        )}

        {/* Konten */}
        <main className="flex-1 overflow-x-hidden p-4 md:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
