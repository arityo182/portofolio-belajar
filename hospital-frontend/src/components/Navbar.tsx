import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Menu, X, Search, HeartPulse, LogOut, ShieldCheck, Stethoscope, CalendarPlus } from "lucide-react";
import { C } from "../core/theme";
import { useAuth } from "../core/context/AuthContext";

interface NavLink {
  label: string;
  to: string;
}

export default function Navbar() {
  const [open, setOpen] = useState(false);
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const links: NavLink[] = [
    { label: "Beranda", to: "/" },
    { label: "Layanan", to: "/layanan" },
    { label: "Dokter", to: "/dokter" },
    { label: "Tentang Kami", to: "/tentang" },
    { label: "Kontak", to: "/kontak" },
  ];

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <header
      className="sticky top-0 z-50 w-full border-b"
      style={{ backgroundColor: C.white, borderColor: C.line }}
    >
      <nav className="mx-auto flex max-w-6xl items-center justify-between px-5 py-3">
        <Link to="/" className="flex items-center gap-2.5">
          <div
            className="flex h-10 w-10 items-center justify-center rounded-full"
            style={{ backgroundColor: C.navy }}
          >
            <HeartPulse size={20} color={C.white} strokeWidth={2.2} />
          </div>
          <div className="leading-tight">
            <p className="text-base font-bold tracking-tight" style={{ color: C.navy }}>
              MEDIKA
            </p>
            <p className="text-[10px] font-medium tracking-[0.2em]" style={{ color: C.orange }}>
              SENTOSA
            </p>
          </div>
        </Link>

        <ul className="hidden items-center gap-7 md:flex">
          {links.map((l) => (
            <li key={l.label}>
              <Link
                to={l.to}
                className="text-sm font-medium transition-colors hover:opacity-70"
                style={{ color: C.grey }}
              >
                {l.label}
              </Link>
            </li>
          ))}
        </ul>

        <div className="hidden items-center gap-3 md:flex">
          <button
            className="flex h-9 w-9 items-center justify-center rounded-full"
            style={{ backgroundColor: C.cream }}
            aria-label="Cari"
          >
            <Search size={17} color={C.navy} />
          </button>

          {isAuthenticated ? (
            <>
              {user?.role === "PASIEN" && (
                <>
                  <Link to="/booking" className="inline-flex items-center gap-1.5 rounded-full px-4 py-2 text-sm font-semibold text-white" style={{ backgroundColor: C.orange }}>
                    <CalendarPlus size={15} /> Buat Janji
                  </Link>
                  <Link to="/appointment-saya" className="text-sm font-medium transition-colors hover:opacity-70" style={{ color: C.grey }}>Janji Saya</Link>
                  <Link to="/rekam-medis" className="text-sm font-medium transition-colors hover:opacity-70" style={{ color: C.grey }}>RM</Link>
                  <Link to="/tagihan-saya" className="text-sm font-medium transition-colors hover:opacity-70" style={{ color: C.grey }}>Tagihan</Link>
                </>
              )}
              {user?.role === "ADMIN" && (
                <Link to="/admin/dasbor" className="inline-flex items-center gap-1.5 rounded-full px-4 py-2 text-sm font-semibold text-white" style={{ backgroundColor: C.navy }}>
                  <ShieldCheck size={15} /> Admin Panel
                </Link>
              )}
              {user?.role === "DOKTER" && (
                <Link to="/dokter-panel/antrian" className="inline-flex items-center gap-1.5 rounded-full px-4 py-2 text-sm font-semibold text-white" style={{ backgroundColor: C.navy }}>
                  <Stethoscope size={15} /> Panel Dokter
                </Link>
              )}
              <span className="text-sm font-medium" style={{ color: C.navy }}>
                Hai, {user?.nama?.split(" ")[0]}
              </span>
              <button onClick={handleLogout} className="flex items-center gap-1.5 rounded-full px-4 py-2 text-sm font-semibold text-white" style={{ backgroundColor: C.orange }}>
                <LogOut size={15} /> Keluar
              </button>
            </>
          ) : (
            <>
              <Link
                to="/login"
                className="text-sm font-semibold transition-opacity hover:opacity-70"
                style={{ color: C.navy }}
              >
                Masuk
              </Link>
              <Link
                to="/registration"
                className="rounded-full px-5 py-2 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5"
                style={{ backgroundColor: C.orange }}
              >
                Daftar Online
              </Link>
            </>
          )}
        </div>

        <button className="md:hidden" onClick={() => setOpen(!open)} aria-label="Menu">
          {open ? <X size={24} color={C.navy} /> : <Menu size={24} color={C.navy} />}
        </button>
      </nav>

      {open && (
        <div className="border-t px-5 py-4 md:hidden" style={{ borderColor: C.line }}>
          <ul className="flex flex-col gap-3">
            {links.map((l) => (
              <li key={l.label}>
                <Link
                  to={l.to}
                  className="block text-sm font-medium"
                  style={{ color: C.grey }}
                  onClick={() => setOpen(false)}
                >
                  {l.label}
                </Link>
              </li>
            ))}
          </ul>
          <div className="mt-4 flex flex-col gap-2">
            {isAuthenticated ? (
              <>
                {user?.role === "PASIEN" && (
                  <>
                    <Link to="/booking" className="rounded-full py-2.5 text-center text-sm font-semibold text-white" style={{ backgroundColor: C.orange }} onClick={() => setOpen(false)}>Buat Janji</Link>
                    <Link to="/appointment-saya" className="rounded-full py-2.5 text-center text-sm font-semibold" style={{ backgroundColor: C.cream, color: C.navy }} onClick={() => setOpen(false)}>Janji Saya</Link>
                    <Link to="/rekam-medis" className="rounded-full py-2.5 text-center text-sm font-semibold" style={{ backgroundColor: C.cream, color: C.navy }} onClick={() => setOpen(false)}>Rekam Medis</Link>
                    <Link to="/tagihan-saya" className="rounded-full py-2.5 text-center text-sm font-semibold" style={{ backgroundColor: C.cream, color: C.navy }} onClick={() => setOpen(false)}>Tagihan</Link>
                  </>
                )}
                {user?.role === "ADMIN" && (
                  <Link to="/admin/dasbor" className="rounded-full py-2.5 text-center text-sm font-semibold text-white" style={{ backgroundColor: C.navy }} onClick={() => setOpen(false)}>Admin Panel</Link>
                )}
                {user?.role === "DOKTER" && (
                  <Link to="/dokter-panel/antrian" className="rounded-full py-2.5 text-center text-sm font-semibold text-white" style={{ backgroundColor: C.navy }} onClick={() => setOpen(false)}>Panel Dokter</Link>
                )}
                <button onClick={() => { handleLogout(); setOpen(false); }} className="rounded-full py-2.5 text-center text-sm font-semibold text-white" style={{ backgroundColor: C.orange }}>Keluar</button>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  className="rounded-full py-2.5 text-center text-sm font-semibold"
                  style={{ backgroundColor: C.cream, color: C.navy }}
                >
                  Masuk
                </Link>
                <Link
                  to="/registration"
                  className="rounded-full py-2.5 text-center text-sm font-semibold text-white"
                  style={{ backgroundColor: C.orange }}
                >
                  Daftar Online
                </Link>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
