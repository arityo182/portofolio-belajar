import { useCallback, useEffect, useState } from "react";
import {
  Users, Loader2, AlertTriangle, Power, ShieldCheck, KeyRound, X, Check,
  Eye, EyeOff,
} from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import { getAllUsers, setUserStatus, resetPassword } from "../../admin/services/adminService";
import type { UserAdmin, Role } from "../../admin/types";

/** Label ramah-baca per role. */
const LABEL_ROLE: Record<Role, string> = {
  PASIEN: "Pasien",
  DOKTER: "Dokter",
  ADMIN: "Admin",
};

/** Warna badge per role. */
const WARNA_ROLE: Record<Role, { bg: string; fg: string }> = {
  PASIEN: { bg: C.blueSoft, fg: C.navy },
  DOKTER: { bg: C.orangeSoft, fg: C.orange },
  ADMIN: { bg: C.navy, fg: C.white },
};

/** Opsi filter role. */
const FILTER_ROLE: { value: Role | ""; label: string }[] = [
  { value: "", label: "Semua" },
  { value: "PASIEN", label: "Pasien" },
  { value: "DOKTER", label: "Dokter" },
  { value: "ADMIN", label: "Admin" },
];

/**
 * Halaman manajemen pengguna untuk admin (FR-04).
 *
 * Menampilkan daftar semua pengguna dengan filter role. Admin dapat
 * mengaktifkan/menonaktifkan akun. Tidak ada CRUD penuh — pasien daftar
 * sendiri via /register, dokter dibuat via Manajemen Dokter.
 *
 * @returns Halaman manajemen pengguna
 */
export default function AdminPengguna() {
  const [users, setUsers] = useState<UserAdmin[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState<Role | "">("");
  const [togglingId, setTogglingId] = useState<number | null>(null);

  // Modal ganti password
  const [pwModalOpen, setPwModalOpen] = useState(false);
  const [pwTarget, setPwTarget] = useState<UserAdmin | null>(null);
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showNewPw, setShowNewPw] = useState(false);
  const [showConfirmPw, setShowConfirmPw] = useState(false);
  const [pwSaving, setPwSaving] = useState(false);
  const [pwError, setPwError] = useState<string | null>(null);

  const muat = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getAllUsers(filter || undefined);
      setUsers(res.data);
    } catch (err) {
      setError(toErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => { muat(); }, [muat]);

  /** Toggle status aktif akun. */
  const handleToggle = async (u: UserAdmin) => {
    const aksi = u.isActive ? "nonaktifkan" : "aktifkan";
    if (!confirm(`Yakin ingin ${aksi} akun "${u.nama}"?`)) return;
    setTogglingId(u.id);
    try {
      await setUserStatus(u.id, !u.isActive);
      await muat();
    } catch (err) {
      setError(toErrorMessage(err));
    } finally {
      setTogglingId(null);
    }
  };

  /** Buka modal ganti password. */
  const bukaGantiPassword = (u: UserAdmin) => {
    setPwTarget(u);
    setNewPassword("");
    setConfirmPassword("");
    setShowNewPw(false);
    setShowConfirmPw(false);
    setPwError(null);
    setPwModalOpen(true);
  };

  /** Submit ganti password — validasi 2 field sama. */
  const handleSavePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!pwTarget) return;
    if (!newPassword.trim()) {
      setPwError("Password baru wajib diisi");
      return;
    }
    if (newPassword.length < 6) {
      setPwError("Password baru minimal 6 karakter");
      return;
    }
    if (newPassword !== confirmPassword) {
      setPwError("Konfirmasi password tidak cocok dengan password baru");
      return;
    }
    setPwSaving(true);
    setPwError(null);
    try {
      await resetPassword(pwTarget.id, newPassword);
      setPwModalOpen(false);
    } catch (err) {
      setPwError(toErrorMessage(err));
    } finally {
      setPwSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center gap-3 py-20">
        <Loader2 className="animate-spin" size={28} color={C.navy} />
        <p className="text-sm" style={{ color: C.grey }}>Memuat data pengguna...</p>
      </div>
    );
  }

  return (
    <div>
      <div>
        <h1 className="text-2xl font-bold" style={{ color: C.navy }}>Manajemen Pengguna</h1>
        <p className="mt-1 text-sm" style={{ color: C.grey }}>Kelola akun pengguna & status aktif (FR-04)</p>
      </div>

      {/* Filter role */}
      <div className="mt-4 flex flex-wrap gap-2">
        {FILTER_ROLE.map((f) => (
          <button
            key={f.value}
            onClick={() => setFilter(f.value)}
            className="rounded-full px-4 py-2 text-sm font-semibold transition-colors"
            style={{
              backgroundColor: filter === f.value ? C.navy : C.cream,
              color: filter === f.value ? C.white : C.grey,
            }}
          >
            {f.label}
          </button>
        ))}
      </div>

      {error && (
        <div className="mt-4 flex items-start gap-2 rounded-2xl px-4 py-3" style={{ backgroundColor: "#FCE8E6" }}>
          <AlertTriangle size={18} color="#C0392B" className="mt-0.5 flex-shrink-0" />
          <p className="text-sm font-medium" style={{ color: "#C0392B" }}>{error}</p>
        </div>
      )}

      {/* Tabel pengguna */}
      <div className="mt-6 overflow-x-auto rounded-3xl border" style={{ backgroundColor: C.white, borderColor: C.line }}>
        <table className="w-full text-left text-sm">
          <thead>
            <tr style={{ backgroundColor: C.cream, borderBottom: `1px solid ${C.line}` }}>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Nama</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Email</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Role</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Status</th>
              <th className="px-5 py-3 text-right font-semibold" style={{ color: C.navy }}>Aksi</th>
            </tr>
          </thead>
          <tbody>
            {users.length === 0 ? (
              <tr><td colSpan={5} className="px-5 py-10 text-center" style={{ color: C.grey }}>Tidak ada pengguna.</td></tr>
            ) : (
              users.map((u) => {
                const wr = WARNA_ROLE[u.role];
                return (
                  <tr key={u.id} style={{ borderBottom: `1px solid ${C.line}` }}>
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-3">
                        <div className="flex h-9 w-9 items-center justify-center rounded-full" style={{ backgroundColor: C.blueSoft }}>
                          <Users size={18} color={C.navy} />
                        </div>
                        <span className="font-semibold" style={{ color: C.navy }}>{u.nama}</span>
                      </div>
                    </td>
                    <td className="px-5 py-4" style={{ color: C.grey }}>{u.email}</td>
                    <td className="px-5 py-4">
                      <span className="inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-xs font-semibold" style={{ backgroundColor: wr.bg, color: wr.fg }}>
                        {u.role === "ADMIN" && <ShieldCheck size={12} />}
                        {LABEL_ROLE[u.role]}
                      </span>
                    </td>
                    <td className="px-5 py-4">
                      <span className="rounded-full px-2.5 py-1 text-xs font-semibold" style={{ backgroundColor: u.isActive ? C.blueSoft : C.line, color: u.isActive ? C.navy : C.grey }}>
                        {u.isActive ? "Aktif" : "Nonaktif"}
                      </span>
                    </td>
                    <td className="px-5 py-4">
                      <div className="flex items-center justify-end gap-2">
                        {u.role !== "ADMIN" && (
                          <button
                            onClick={() => bukaGantiPassword(u)}
                            className="inline-flex items-center gap-1.5 rounded-full px-3 py-2 text-xs font-semibold transition-opacity hover:opacity-80"
                            style={{ backgroundColor: C.cream, color: C.navy }}
                          >
                            <KeyRound size={13} /> Password
                          </button>
                        )}
                        <button
                          onClick={() => handleToggle(u)}
                          disabled={togglingId === u.id || u.role === "ADMIN"}
                          className="inline-flex items-center gap-1.5 rounded-full px-3 py-2 text-xs font-semibold transition-opacity hover:opacity-80 disabled:opacity-50"
                          style={{
                            backgroundColor: u.isActive ? "#FCE8E6" : C.blueSoft,
                            color: u.isActive ? "#C0392B" : C.navy,
                          }}
                        >
                          {togglingId === u.id ? <Loader2 className="animate-spin" size={13} /> : <Power size={13} />}
                          {u.isActive ? "Nonaktifkan" : "Aktifkan"}
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {/* Modal Ganti Password */}
      {pwModalOpen && pwTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={() => !pwSaving && setPwModalOpen(false)}>
          <div className="w-full max-w-md rounded-3xl border p-6" style={{ backgroundColor: C.white, borderColor: C.line }} onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-2xl" style={{ backgroundColor: C.blueSoft }}>
                  <KeyRound size={20} color={C.navy} />
                </div>
                <div>
                  <h2 className="text-lg font-bold" style={{ color: C.navy }}>Ganti Password</h2>
                  <p className="text-sm" style={{ color: C.grey }}>{pwTarget.nama}</p>
                </div>
              </div>
              <button onClick={() => !pwSaving && setPwModalOpen(false)} className="flex h-8 w-8 items-center justify-center rounded-full" style={{ backgroundColor: C.cream }}>
                <X size={18} color={C.navy} />
              </button>
            </div>

            <form onSubmit={handleSavePassword} className="mt-5 flex flex-col gap-4">
              <div>
                <label className="mb-1.5 block text-sm font-semibold" style={{ color: C.navy }}>
                  Password Baru <span style={{ color: C.orange }}>*</span>
                </label>
                <div className="relative">
                  <input
                    type={showNewPw ? "text" : "password"}
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    minLength={6}
                    placeholder="Min. 6 karakter"
                    className="w-full rounded-2xl border px-4 py-3 pr-12 text-sm outline-none focus:border-[#EF7A00]"
                    style={{ borderColor: C.line, color: C.navy }}
                    required
                    autoFocus
                  />
                  <button
                    type="button"
                    onClick={() => setShowNewPw(!showNewPw)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 transition-colors hover:text-gray-600"
                    aria-label={showNewPw ? "Sembunyikan password" : "Tampilkan password"}
                  >
                    {showNewPw ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
              </div>

              <div>
                <label className="mb-1.5 block text-sm font-semibold" style={{ color: C.navy }}>
                  Konfirmasi Password Baru <span style={{ color: C.orange }}>*</span>
                </label>
                <div className="relative">
                  <input
                    type={showConfirmPw ? "text" : "password"}
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    minLength={6}
                    placeholder="Ulangi password baru"
                    className="w-full rounded-2xl border px-4 py-3 pr-12 text-sm outline-none focus:border-[#EF7A00]"
                    style={{
                      borderColor: confirmPassword && confirmPassword !== newPassword ? "#C0392B" : C.line,
                      color: C.navy,
                    }}
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPw(!showConfirmPw)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 transition-colors hover:text-gray-600"
                    aria-label={showConfirmPw ? "Sembunyikan password" : "Tampilkan password"}
                  >
                    {showConfirmPw ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
                {confirmPassword && confirmPassword !== newPassword && (
                  <p className="mt-1.5 text-xs font-medium" style={{ color: "#C0392B" }}>
                    Konfirmasi password tidak cocok
                  </p>
                )}
                {confirmPassword && confirmPassword === newPassword && (
                  <p className="mt-1.5 text-xs font-medium" style={{ color: "#1B8A5A" }}>
                    ✓ Password cocok
                  </p>
                )}
              </div>

              <p className="text-xs" style={{ color: C.grey }}>
                Password lama tidak perlu diketahui. User dapat langsung login dengan password baru.
              </p>

              {pwError && (
                <div className="flex items-start gap-2 rounded-2xl px-4 py-3" style={{ backgroundColor: "#FCE8E6" }}>
                  <AlertTriangle size={16} color="#C0392B" className="mt-0.5 flex-shrink-0" />
                  <p className="text-sm font-medium" style={{ color: "#C0392B" }}>{pwError}</p>
                </div>
              )}

              <div className="flex items-center gap-3">
                <button type="submit" disabled={pwSaving} className="inline-flex items-center gap-2 rounded-full px-6 py-3 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5 disabled:opacity-50" style={{ backgroundColor: C.orange }}>
                  {pwSaving ? <Loader2 className="animate-spin" size={17} /> : <Check size={17} />}
                  {pwSaving ? "Menyimpan..." : "Simpan Password"}
                </button>
                <button type="button" onClick={() => !pwSaving && setPwModalOpen(false)} disabled={pwSaving} className="rounded-full px-6 py-3 text-sm font-semibold transition-opacity hover:opacity-80 disabled:opacity-50" style={{ backgroundColor: C.cream, color: C.navy }}>
                  Batal
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
