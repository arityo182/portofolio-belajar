import { useState, useRef, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  Menu, X, HeartPulse, LogOut, ShieldCheck, Stethoscope,
  CalendarPlus, MessageSquare, ChevronDown,
  ClipboardList, Receipt, UserCircle,
} from "lucide-react";
import { C } from "../core/theme";
import { useAuth } from "../core/context/AuthContext";

interface NavLink {
  label: string;
  to: string;
}

export default function Navbar() {
  const [open, setOpen] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const userMenuRef = useRef<HTMLDivElement>(null);
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
    setUserMenuOpen(false);
    navigate("/");
  };

  // Close dropdown on outside click
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (userMenuRef.current && !userMenuRef.current.contains(e.target as Node)) {
        setUserMenuOpen(false);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  const isPatient = user?.role === "PASIEN";

  return (
    <header
      className="sticky top-0 z-50 w-full border-b bg-white/95 backdrop-blur-md"
      style={{ borderColor: C.line }}
    >
      <nav className="mx-auto flex max-w-6xl items-center justify-between px-5 py-2.5">
        {/* ─── Logo ────────────────────── */}
        <Link to="/" className="flex items-center gap-2.5 shrink-0">
          <div
            className="flex h-10 w-10 items-center justify-center rounded-xl shadow-sm"
            style={{ backgroundColor: C.navy }}
          >
            <HeartPulse size={20} color={C.white} strokeWidth={2.2} />
          </div>
          <div className="leading-tight hidden sm:block">
            <p className="text-base font-extrabold tracking-tight" style={{ color: C.navy }}>
              MEDIKA
            </p>
            <p className="text-[10px] font-bold tracking-[0.25em]" style={{ color: C.orange }}>
              SENTOSA
            </p>
          </div>
        </Link>

        {/* ─── Menu Tengah ─────────────── */}
        <ul className="hidden items-center gap-1 lg:flex">
          {links.map((l) => (
            <li key={l.label}>
              <Link
                to={l.to}
                className="rounded-lg px-3.5 py-2 text-sm font-medium transition-colors hover:bg-gray-50"
                style={{ color: C.grey }}
              >
                {l.label}
              </Link>
            </li>
          ))}
        </ul>

        {/* ─── Kanan ───────────────────── */}
        <div className="flex items-center gap-2">
          {isAuthenticated ? (
            <>
              {/* Buat Janji (PASIEN only) */}
              {isPatient && (
                <Link
                  to="/booking"
                  className="hidden sm:inline-flex items-center gap-1.5 rounded-xl px-3.5 py-2 text-sm font-semibold text-white shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md"
                  style={{ backgroundColor: C.orange }}
                >
                  <CalendarPlus size={15} /> <span className="hidden md:inline">Buat Janji</span>
                </Link>
              )}

              {/* Senta AI */}
              <Link
                to="/chat"
                className="hidden sm:inline-flex items-center gap-1.5 rounded-xl px-3.5 py-2 text-sm font-semibold text-white shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md"
                style={{ backgroundColor: C.navy }}
              >
                <MessageSquare size={15} /> <span className="hidden md:inline">Senta AI</span>
              </Link>

              {/* Role-specific panel buttons */}
              {user?.role === "ADMIN" && (
                <Link
                  to="/admin/dasbor"
                  className="inline-flex items-center gap-1.5 rounded-xl px-3.5 py-2 text-sm font-semibold text-white shadow-sm"
                  style={{ backgroundColor: C.navy }}
                >
                  <ShieldCheck size={15} /> Admin
                </Link>
              )}
              {user?.role === "DOKTER" && (
                <Link
                  to="/dokter-panel/antrian"
                  className="inline-flex items-center gap-1.5 rounded-xl px-3.5 py-2 text-sm font-semibold text-white shadow-sm"
                  style={{ backgroundColor: C.navy }}
                >
                  <Stethoscope size={15} /> Panel Dokter
                </Link>
              )}

              {/* User Dropdown */}
              <div className="relative" ref={userMenuRef}>
                <button
                  onClick={() => setUserMenuOpen(!userMenuOpen)}
                  className="flex items-center gap-2 rounded-xl border px-3 py-2 text-sm font-semibold transition-colors hover:bg-gray-50"
                  style={{ borderColor: C.line, color: C.navy }}
                >
                  <div
                    className="flex h-7 w-7 items-center justify-center rounded-full text-[11px] font-bold text-white"
                    style={{ backgroundColor: C.orange }}
                  >
                    {user?.nama?.[0] || "U"}
                  </div>
                  <span className="hidden md:inline max-w-[80px] truncate">
                    {user?.nama?.split(" ")[0]}
                  </span>
                  <ChevronDown
                    size={14}
                    className={`hidden md:block transition-transform ${userMenuOpen ? "rotate-180" : ""}`}
                  />
                </button>

                {userMenuOpen && (
                  <div
                    className="absolute right-0 top-full mt-2 w-52 rounded-xl border bg-white py-1.5 shadow-xl animate-in fade-in zoom-in-95"
                    style={{ borderColor: C.line }}
                  >
                    <div className="px-4 py-2 border-b" style={{ borderColor: C.line }}>
                      <p className="text-sm font-semibold" style={{ color: C.navy }}>
                        {user?.nama}
                      </p>
                      <p className="text-xs" style={{ color: C.grey }}>
                        {user?.email}
                      </p>
                    </div>

                    {isPatient && (
                      <>
                        <Link
                          to="/profil-pasien"
                          onClick={() => setUserMenuOpen(false)}
                          className="flex items-center gap-3 px-4 py-2.5 text-sm transition-colors hover:bg-gray-50"
                          style={{ color: C.grey }}
                        >
                          <UserCircle size={16} /> Profil Saya
                        </Link>
                        <Link
                          to="/appointment-saya"
                          onClick={() => setUserMenuOpen(false)}
                          className="flex items-center gap-3 px-4 py-2.5 text-sm transition-colors hover:bg-gray-50"
                          style={{ color: C.grey }}
                        >
                          <CalendarPlus size={16} /> Janji Saya
                        </Link>
                        <Link
                          to="/rekam-medis"
                          onClick={() => setUserMenuOpen(false)}
                          className="flex items-center gap-3 px-4 py-2.5 text-sm transition-colors hover:bg-gray-50"
                          style={{ color: C.grey }}
                        >
                          <ClipboardList size={16} /> Rekam Medis
                        </Link>
                        <Link
                          to="/tagihan-saya"
                          onClick={() => setUserMenuOpen(false)}
                          className="flex items-center gap-3 px-4 py-2.5 text-sm transition-colors hover:bg-gray-50"
                          style={{ color: C.grey }}
                        >
                          <Receipt size={16} /> Tagihan
                        </Link>
                      </>
                    )}
                    <div className="border-t" style={{ borderColor: C.line }}>
                      <button
                        onClick={handleLogout}
                        className="flex w-full items-center gap-3 px-4 py-2.5 text-sm font-medium transition-colors hover:bg-red-50"
                        style={{ color: "#C0392B" }}
                      >
                        <LogOut size={16} /> Keluar
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </>
          ) : (
            <>
              <Link
                to="/login"
                className="text-sm font-semibold transition-opacity hover:opacity-70 hidden sm:block"
                style={{ color: C.navy }}
              >
                Masuk
              </Link>
              <Link
                to="/registration"
                className="rounded-xl px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md"
                style={{ backgroundColor: C.orange }}
              >
                Daftar Online
              </Link>
            </>
          )}

          {/* Hamburger */}
          <button className="lg:hidden ml-1" onClick={() => setOpen(!open)} aria-label="Menu">
            {open ? <X size={24} color={C.navy} /> : <Menu size={24} color={C.navy} />}
          </button>
        </div>
      </nav>

      {/* ─── Mobile Menu ─────────────── */}
      {open && (
        <div className="border-t px-5 py-4 lg:hidden" style={{ borderColor: C.line }}>
          <ul className="flex flex-col gap-1">
            {links.map((l) => (
              <li key={l.label}>
                <Link
                  to={l.to}
                  className="block rounded-lg px-3 py-2.5 text-sm font-medium hover:bg-gray-50"
                  style={{ color: C.grey }}
                  onClick={() => setOpen(false)}
                >
                  {l.label}
                </Link>
              </li>
            ))}
          </ul>
          <div className="mt-3 flex flex-col gap-2">
            {isAuthenticated ? (
              <>
                {isPatient && (
                  <>
                    <Link
                      to="/booking"
                      className="flex items-center justify-center gap-2 rounded-xl py-2.5 text-sm font-semibold text-white"
                      style={{ backgroundColor: C.orange }}
                      onClick={() => setOpen(false)}
                    >
                      <CalendarPlus size={16} /> Buat Janji
                    </Link>
                    <Link
                      to="/chat"
                      className="flex items-center justify-center gap-2 rounded-xl py-2.5 text-sm font-semibold text-white"
                      style={{ backgroundColor: C.navy }}
                      onClick={() => setOpen(false)}
                    >
                      <MessageSquare size={16} /> Senta AI
                    </Link>
                    <Link
                      to="/profil"
                      className="rounded-xl py-2.5 text-center text-sm font-semibold"
                      style={{ backgroundColor: C.cream, color: C.navy }}
                      onClick={() => setOpen(false)}
                    >
                      Profil Saya
                    </Link>
                    <Link
                      to="/appointment-saya"
                      className="rounded-xl py-2.5 text-center text-sm font-semibold"
                      style={{ backgroundColor: C.cream, color: C.navy }}
                      onClick={() => setOpen(false)}
                    >
                      Janji Saya
                    </Link>
                    <Link
                      to="/rekam-medis"
                      className="rounded-xl py-2.5 text-center text-sm font-semibold"
                      style={{ backgroundColor: C.cream, color: C.navy }}
                      onClick={() => setOpen(false)}
                    >
                      Rekam Medis
                    </Link>
                    <Link
                      to="/tagihan-saya"
                      className="rounded-xl py-2.5 text-center text-sm font-semibold"
                      style={{ backgroundColor: C.cream, color: C.navy }}
                      onClick={() => setOpen(false)}
                    >
                      Tagihan
                    </Link>
                  </>
                )}
                {user?.role === "ADMIN" && (
                  <Link
                    to="/admin/dasbor"
                    className="rounded-xl py-2.5 text-center text-sm font-semibold text-white"
                    style={{ backgroundColor: C.navy }}
                    onClick={() => setOpen(false)}
                  >
                    Admin Panel
                  </Link>
                )}
                {user?.role === "DOKTER" && (
                  <Link
                    to="/dokter-panel/antrian"
                    className="rounded-xl py-2.5 text-center text-sm font-semibold text-white"
                    style={{ backgroundColor: C.navy }}
                    onClick={() => setOpen(false)}
                  >
                    Panel Dokter
                  </Link>
                )}
                <button
                  onClick={() => { handleLogout(); setOpen(false); }}
                  className="rounded-xl py-2.5 text-center text-sm font-semibold text-white"
                  style={{ backgroundColor: C.orange }}
                >
                  Keluar
                </button>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  className="rounded-xl py-2.5 text-center text-sm font-semibold"
                  style={{ backgroundColor: C.cream, color: C.navy }}
                >
                  Masuk
                </Link>
                <Link
                  to="/registration"
                  className="rounded-xl py-2.5 text-center text-sm font-semibold text-white"
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
