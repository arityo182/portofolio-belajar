import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { HeartPulse, User, Mail, Lock, Eye, EyeOff, Loader2, AlertTriangle } from "lucide-react";
import { C } from "../../../core/theme";
import { useAuth } from "../../../core/context/AuthContext";
import { registerUser } from "../services/authService";
import axios from "axios";

/**
 * Halaman registrasi akun baru.
 *
 * Mengelola form (nama, email, password, konfirmasi) dengan validasi
 * dasar (kelengkapan field, panjang password minimal 6, kecocokan
 * konfirmasi), lalu memanggil {@link registerUser}. Saat berhasil,
 * pengguna langsung di-auto-login via {@link useAuth} `login()` dan
 * diarahkan ke beranda.
 *
 * @returns Halaman form registrasi
 */
export default function Register() {
  const [nama, setNama] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    if (!nama || !email || !password || !confirm) {
      setError("Semua field wajib diisi.");
      return;
    }
    if (password.length < 6) {
      setError("Password minimal 6 karakter.");
      return;
    }
    if (password !== confirm) {
      setError("Konfirmasi password tidak cocok.");
      return;
    }

    setLoading(true);
    try {
      const res = await registerUser({ nama, email, password });
      // auto-login setelah register berhasil
      login(res.data.token, res.data.user);
      navigate("/");
    } catch (err) {
      if (axios.isAxiosError(err)) {
        setError(err.response?.data?.message ?? "Gagal mendaftar. Email mungkin sudah terpakai.");
      } else {
        setError("Terjadi kesalahan. Coba lagi.");
      }
    } finally {
      setLoading(false);
    }
  };

  const inputWrap = "mb-4 flex items-center gap-2 rounded-xl border px-3.5 py-3";
  const inputStyle = { borderColor: C.line, backgroundColor: C.white };
  const labelStyle = { color: C.navy };

  return (
    <div
      className="flex min-h-[calc(100vh-65px)] items-center justify-center px-5 py-12"
      style={{ backgroundColor: C.cream }}
    >
      <div className="w-full max-w-md">
        <div className="mb-6 text-center">
          <div
            className="mx-auto flex h-14 w-14 items-center justify-center rounded-full"
            style={{ backgroundColor: C.navy }}
          >
            <HeartPulse size={26} color={C.white} />
          </div>
          <h1 className="mt-4 text-2xl font-bold" style={{ color: C.navy }}>
            Buat Akun Baru
          </h1>
          <p className="mt-1 text-sm" style={{ color: C.grey }}>
            Daftar untuk mengakses layanan MEDIKA SENTOSA
          </p>
        </div>

        <form
          onSubmit={handleSubmit}
          className="rounded-3xl border p-7 shadow-sm"
          style={{ backgroundColor: C.white, borderColor: C.line }}
        >
          {error && (
            <div
              className="mb-4 flex items-center gap-2 rounded-xl px-3 py-2.5"
              style={{ backgroundColor: "#FCEBEB" }}
            >
              <AlertTriangle size={16} color="#A32D2D" />
              <span className="text-sm font-medium" style={{ color: "#A32D2D" }}>{error}</span>
            </div>
          )}

          <label className="mb-1.5 block text-sm font-medium" style={labelStyle}>Nama Lengkap</label>
          <div className={inputWrap} style={inputStyle}>
            <User size={18} color={C.grey} />
            <input
              type="text"
              value={nama}
              onChange={(e) => setNama(e.target.value)}
              placeholder="Nama lengkap Anda"
              className="w-full bg-transparent text-sm outline-none"
              style={{ color: C.navy }}
            />
          </div>

          <label className="mb-1.5 block text-sm font-medium" style={labelStyle}>Email</label>
          <div className={inputWrap} style={inputStyle}>
            <Mail size={18} color={C.grey} />
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="nama@email.com"
              className="w-full bg-transparent text-sm outline-none"
              style={{ color: C.navy }}
            />
          </div>

          <label className="mb-1.5 block text-sm font-medium" style={labelStyle}>Password</label>
          <div className={inputWrap} style={inputStyle}>
            <Lock size={18} color={C.grey} />
            <input
              type={showPw ? "text" : "password"}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Minimal 6 karakter"
              className="w-full bg-transparent text-sm outline-none"
              style={{ color: C.navy }}
            />
            <button type="button" onClick={() => setShowPw(!showPw)} aria-label="Tampilkan password">
              {showPw ? <EyeOff size={18} color={C.grey} /> : <Eye size={18} color={C.grey} />}
            </button>
          </div>

          <label className="mb-1.5 block text-sm font-medium" style={labelStyle}>Konfirmasi Password</label>
          <div className="mb-5 flex items-center gap-2 rounded-xl border px-3.5 py-3" style={inputStyle}>
            <Lock size={18} color={C.grey} />
            <input
              type={showConfirm ? "text" : "password"}
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
              placeholder="Ulangi password"
              className="w-full bg-transparent text-sm outline-none"
              style={{ color: C.navy }}
            />
            <button type="button" onClick={() => setShowConfirm(!showConfirm)} aria-label="Tampilkan konfirmasi password">
              {showConfirm ? <EyeOff size={18} color={C.grey} /> : <Eye size={18} color={C.grey} />}
            </button>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="flex w-full items-center justify-center gap-2 rounded-full py-3.5 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5 disabled:opacity-70"
            style={{ backgroundColor: C.orange }}
          >
            {loading ? (
              <>
                <Loader2 size={18} className="animate-spin" /> Memproses...
              </>
            ) : (
              "Daftar"
            )}
          </button>

          <p className="mt-5 text-center text-sm" style={{ color: C.grey }}>
            Sudah punya akun?{" "}
            <Link to="/login" className="font-semibold" style={{ color: C.orange }}>
              Masuk di sini
            </Link>
          </p>
        </form>
      </div>
    </div>
  );
}


