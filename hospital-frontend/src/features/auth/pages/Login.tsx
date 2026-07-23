import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { HeartPulse, Mail, Lock, Eye, EyeOff, Loader2, AlertTriangle } from "lucide-react";
import { C } from "../../../core/theme";
import { useAuth } from "../../../core/context/AuthContext";
import { loginUser } from "../services/authService";
import axios from "axios";

/**
 * Halaman login pengguna.
 *
 * Mengelola form email/password, validasi dasar, lalu memanggil
 * {@link loginUser}. Saat berhasil, sesi disimpan via {@link useAuth}
 * `login()` dan pengguna diarahkan ke beranda. Error dari backend
 * (mis. kredensial salah) ditampilkan di atas form.
 *
 * @returns Halaman form login
 */
export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    if (!email || !password) {
      setError("Email dan password wajib diisi.");
      return;
    }

    setLoading(true);
    try {
      const res = await loginUser({ email, password });
      login(res.data.token, res.data.user);
      navigate("/");
    } catch (err) {
      if (axios.isAxiosError(err)) {
        setError(err.response?.data?.message ?? "Email atau password salah.");
      } else {
        setError("Terjadi kesalahan. Coba lagi.");
      }
    } finally {
      setLoading(false);
    }
  };

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
            Selamat Datang Kembali
          </h1>
          <p className="mt-1 text-sm" style={{ color: C.grey }}>
            Masuk ke akun MEDIKA SENTOSA Anda
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

          {/* Email */}
          <label className="mb-1.5 block text-sm font-medium" style={{ color: C.navy }}>
            Email
          </label>
          <div
            className="mb-4 flex items-center gap-2 rounded-xl border px-3.5 py-3"
            style={{ borderColor: C.line, backgroundColor: C.white }}
          >
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

          {/* Password */}
          <label className="mb-1.5 block text-sm font-medium" style={{ color: C.navy }}>
            Password
          </label>
          <div
            className="mb-5 flex items-center gap-2 rounded-xl border px-3.5 py-3"
            style={{ borderColor: C.line, backgroundColor: C.white }}
          >
            <Lock size={18} color={C.grey} />
            <input
              type={showPw ? "text" : "password"}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              className="w-full bg-transparent text-sm outline-none"
              style={{ color: C.navy }}
            />
            <button
              type="button"
              onClick={() => setShowPw(!showPw)}
              aria-label="Tampilkan password"
            >
              {showPw ? <EyeOff size={18} color={C.grey} /> : <Eye size={18} color={C.grey} />}
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
              "Masuk"
            )}
          </button>

          <p className="mt-5 text-center text-sm" style={{ color: C.grey }}>
            Belum punya akun?{" "}
            <Link to="/registration" className="font-semibold" style={{ color: C.orange }}>
              Daftar di sini
            </Link>
          </p>
        </form>
      </div>
    </div>
  );
}
