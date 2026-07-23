import { useState } from "react";
import { Link, Outlet, useLocation, useNavigate } from "react-router-dom";
import {
  CalendarDays, ClipboardList, LogOut, Menu, X, Stethoscope,
} from "lucide-react";
import { C, RS_NAME } from "../../core/theme";
import { useAuth } from "../../core/context/AuthContext";

interface MenuItem {
  label: string;
  to: string;
  icon: typeof CalendarDays;
}

const MENU: MenuItem[] = [
  { label: "Antrian Pasien", to: "/dokter-panel/antrian", icon: CalendarDays },
  { label: "Riwayat Rekam Medis", to: "/dokter-panel/rekam-medis", icon: ClipboardList },
];

/**
 * Layout khusus halaman panel dokter dengan sidebar.
 *
 * @returns Layout dokter dengan sidebar + konten
 */
export default function DokterLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate("/");
  };

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
          <button className="md:hidden" onClick={() => setSidebarOpen(!sidebarOpen)} aria-label="Toggle menu">
            {sidebarOpen ? <X size={22} color={C.navy} /> : <Menu size={22} color={C.navy} />}
          </button>
          <div className="flex items-center gap-2">
            <div className="flex h-9 w-9 items-center justify-center rounded-full" style={{ backgroundColor: C.navy }}>
              <Stethoscope size={18} color={C.white} />
            </div>
            <div className="leading-tight">
              <p className="text-sm font-bold" style={{ color: C.navy }}>Panel Dokter</p>
              <p className="text-[9px] font-medium tracking-wider" style={{ color: C.orange }}>{RS_NAME}</p>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <span className="hidden text-sm font-medium sm:inline" style={{ color: C.navy }}>
            dr. {user?.nama}
          </span>
          <button onClick={handleLogout} className="flex items-center gap-1.5 rounded-full px-3 py-1.5 text-sm font-semibold text-white" style={{ backgroundColor: C.orange }}>
            <LogOut size={14} /> <span className="hidden sm:inline">Keluar</span>
          </button>
        </div>
      </header>

      <div className="flex flex-1">
        {/* Sidebar desktop */}
        <aside className="sticky top-[57px] hidden h-[calc(100vh-57px)] w-56 flex-shrink-0 border-r md:block" style={{ backgroundColor: C.white, borderColor: C.line }}>
          <nav className="flex flex-col gap-1 p-3">
            {MENU.map(({ label, to, icon: Icon }) => (
              <Link key={to} to={to} className="flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-medium transition-colors" style={{ backgroundColor: isActive(to) ? C.navy : "transparent", color: isActive(to) ? C.white : C.grey }}>
                <Icon size={18} /> {label}
              </Link>
            ))}
          </nav>
        </aside>

        {/* Sidebar mobile */}
        {sidebarOpen && (
          <div className="fixed inset-0 z-30 md:hidden" onClick={() => setSidebarOpen(false)}>
            <div className="absolute inset-0 bg-black/30" />
            <aside className="absolute left-0 top-0 h-full w-56 border-r" style={{ backgroundColor: C.white, borderColor: C.line }} onClick={(e) => e.stopPropagation()}>
              <div className="flex items-center justify-between border-b px-4 py-3" style={{ borderColor: C.line }}>
                <span className="text-sm font-bold" style={{ color: C.navy }}>Menu</span>
                <button onClick={() => setSidebarOpen(false)}><X size={20} color={C.navy} /></button>
              </div>
              <nav className="flex flex-col gap-1 p-3">
                {MENU.map(({ label, to, icon: Icon }) => (
                  <Link key={to} to={to} onClick={() => setSidebarOpen(false)} className="flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-medium transition-colors" style={{ backgroundColor: isActive(to) ? C.navy : "transparent", color: isActive(to) ? C.white : C.grey }}>
                    <Icon size={18} /> {label}
                  </Link>
                ))}
              </nav>
            </aside>
          </div>
        )}

        <main className="flex-1 overflow-x-hidden p-4 md:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
