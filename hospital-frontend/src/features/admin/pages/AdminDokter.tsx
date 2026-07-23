import { useCallback, useEffect, useState } from "react";
import {
  Stethoscope, Plus, Pencil, Trash2, X, Loader2, AlertTriangle, Check, Mail,
  Eye, EyeOff,
} from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import {
  getAllDokterAdmin, createDokter, updateDokter, deactivateDokter,
  getAllPoliAdmin,
} from "../../admin/services/adminService";
import type { AdminDokter, DokterCreateRequest, DokterUpdateRequest } from "../../admin/types";
import type { Poli } from "../../poli/types";

/**
 * Halaman manajemen dokter untuk admin (FR-36).
 *
 * Menampilkan tabel semua dokter dengan operasi:
 * - Tambah: buat User(role=DOKTER) + profil Dokter sekaligus (solve masalah
 *   "siapa buat user dokter" — admin yang buat, bukan via /register)
 * - Edit: ubah poli/noStr/spesialisasi/noHp
 * - Nonaktifkan: soft delete
 *
 * @returns Halaman manajemen dokter
 */
export default function AdminDokter() {
  const [dokterList, setDokterList] = useState<AdminDokter[]>([]);
  const [sok, setSok] = useState("");
  const [poliList, setPoliList] = useState<Poli[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // State modal
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<AdminDokter | null>(null);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  // Form fields (create + edit share)
  const [nama, setNama] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [poliId, setPoliId] = useState<number | "">("");
  const [noStr, setNoStr] = useState("");
  const [spesialisasi, setSpesialisasi] = useState("");
  const [noHp, setNoHp] = useState("");

  // State nonaktifkan
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const muat = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [dRes, pRes] = await Promise.all([getAllDokterAdmin(), getAllPoliAdmin()]);
      setDokterList(dRes.data);
      setPoliList(pRes.data.filter((p) => p.isActive));
    } catch (err) {
      setError(toErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { muat(); }, [muat]);

  /** Reset semua field form. */
  const resetForm = () => {
    setNama(""); setEmail(""); setPassword(""); setPoliId("");
    setNoStr(""); setSpesialisasi(""); setNoHp(""); setFormError(null);
  };

  /** Buka modal tambah dokter. */
  const bukaTambah = () => {
    setEditing(null);
    resetForm();
    setModalOpen(true);
  };

  /** Buka modal edit dokter. */
  const bukaEdit = (d: AdminDokter) => {
    setEditing(d);
    setNama(d.nama);
    setEmail(d.email);
    setPassword("");
    setPoliId(d.poliId);
    setNoStr(d.noStr);
    setSpesialisasi(d.spesialisasi);
    setNoHp(d.noHp);
    setFormError(null);
    setModalOpen(true);
  };

  /** Submit form (tambah atau edit). */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (poliId === "" || !nama.trim() || !noStr.trim() || !spesialisasi.trim() || !noHp.trim()) return;
    setSaving(true);
    setFormError(null);
    try {
      if (editing) {
        // Edit: tidak ubah nama/email/password (itu di User, butuh endpoint terpisah)
        const payload: DokterUpdateRequest = {
          userId: editing.userId,
          poliId: Number(poliId),
          noStr: noStr.trim(),
          spesialisasi: spesialisasi.trim(),
          noHp: noHp.trim(),
        };
        await updateDokter(editing.id, payload);
      } else {
        // Create: butuh semua field termasuk email+password
        if (!email.trim() || !password.trim()) {
          setFormError("Email dan password wajib diisi untuk dokter baru");
          setSaving(false);
          return;
        }
        const payload: DokterCreateRequest = {
          nama: nama.trim(),
          email: email.trim(),
          password,
          poliId: Number(poliId),
          noStr: noStr.trim(),
          spesialisasi: spesialisasi.trim(),
          noHp: noHp.trim(),
        };
        await createDokter(payload);
      }
      setModalOpen(false);
      await muat();
    } catch (err) {
      setFormError(toErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  /** Nonaktifkan dokter (soft delete). */
  const handleDelete = async (id: number) => {
    if (!confirm("Nonaktifkan dokter ini? Dokter yang dinonaktifkan tidak muncul di pilihan pasien.")) return;
    setDeletingId(id);
    try {
      await deactivateDokter(id);
      await muat();
    } catch (err) {
      setError(toErrorMessage(err));
    } finally {
      setDeletingId(null);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center gap-3 py-20">
        <Loader2 className="animate-spin" size={28} color={C.navy} />
        <p className="text-sm" style={{ color: C.grey }}>Memuat data dokter...</p>
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold" style={{ color: C.navy }}>Manajemen Dokter</h1>
          <p className="mt-1 text-sm" style={{ color: C.grey }}>Kelola data dokter & buat akun dokter (FR-36)</p>
        </div>
        <button
          onClick={bukaTambah}
          className="inline-flex items-center gap-2 rounded-full px-5 py-2.5 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5"
          style={{ backgroundColor: C.orange }}
        >
          <Plus size={17} /> Tambah Dokter
        </button>
      </div>

      {error && (
        <div className="mt-4 flex items-start gap-2 rounded-2xl px-4 py-3" style={{ backgroundColor: "#FCE8E6" }}>
          <AlertTriangle size={18} color="#C0392B" className="mt-0.5 flex-shrink-0" />
          <p className="text-sm font-medium" style={{ color: "#C0392B" }}>{error}</p>
        </div>
      )}

      <div className="mt-4"><input type="text" value={sok} onChange={e=>setSok(e.target.value)} placeholder="🔍 Cari nama / spesialisasi / poli / STR..." className="w-full max-w-sm rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line,color:C.navy}}/></div>

      {/* Tabel dokter */}
      <div className="mt-2 overflow-x-auto rounded-3xl border" style={{ backgroundColor: C.white, borderColor: C.line }}>
        <table className="w-full text-left text-sm">
          <thead>
            <tr style={{ backgroundColor: C.cream, borderBottom: `1px solid ${C.line}` }}>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Dokter</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Poli</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Spesialisasi</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>No STR</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>No HP</th>
              <th className="px-5 py-3 text-right font-semibold" style={{ color: C.navy }}>Aksi</th>
            </tr>
          </thead>
          <tbody>
            {(()=>{const f=dokterList.filter(d=>!sok||d.nama.toLowerCase().includes(sok.toLowerCase())||d.spesialisasi.toLowerCase().includes(sok.toLowerCase())||d.namaPoli.toLowerCase().includes(sok.toLowerCase())||d.noStr.toLowerCase().includes(sok.toLowerCase()));return f.length===0?(
              <tr>
                <td colSpan={6} className="px-5 py-10 text-center" style={{ color: C.grey }}>
                  Belum ada dokter. Klik "Tambah Dokter" untuk membuat akun dokter baru.
                </td>
              </tr>
            ) : (
              f.map((d) => (
                <tr key={d.id} style={{ borderBottom: `1px solid ${C.line}` }}>
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-3">
                      <div className="flex h-9 w-9 items-center justify-center rounded-full" style={{ backgroundColor: C.blueSoft }}>
                        <Stethoscope size={18} color={C.navy} />
                      </div>
                      <div>
                        <p className="font-semibold" style={{ color: C.navy }}>{d.nama}</p>
                        <p className="text-xs" style={{ color: C.grey }}>{d.email}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-4" style={{ color: C.grey }}>{d.namaPoli}</td>
                  <td className="px-5 py-4" style={{ color: C.orange }}>{d.spesialisasi}</td>
                  <td className="px-5 py-4" style={{ color: C.grey }}>{d.noStr}</td>
                  <td className="px-5 py-4" style={{ color: C.grey }}>{d.noHp}</td>
                  <td className="px-5 py-4">
                    <div className="flex items-center justify-end gap-2">
                      <button onClick={() => bukaEdit(d)} className="flex h-8 w-8 items-center justify-center rounded-lg transition-colors hover:opacity-70" style={{ backgroundColor: C.cream }} aria-label="Edit">
                        <Pencil size={15} color={C.navy} />
                      </button>
                      <button onClick={() => handleDelete(d.id)} disabled={deletingId === d.id} className="flex h-8 w-8 items-center justify-center rounded-lg transition-colors hover:opacity-70 disabled:opacity-50" style={{ backgroundColor: "#FCE8E6" }} aria-label="Nonaktifkan">
                        {deletingId === d.id ? <Loader2 className="animate-spin" size={15} color="#C0392B" /> : <Trash2 size={15} color="#C0392B" />}
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )})()}
          </tbody>
        </table>
      </div>

      {/* Modal Form */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={() => !saving && setModalOpen(false)}>
          <div className="w-full max-w-lg rounded-3xl border p-6" style={{ backgroundColor: C.white, borderColor: C.line }} onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-bold" style={{ color: C.navy }}>
                {editing ? "Edit Dokter" : "Tambah Dokter Baru"}
              </h2>
              <button onClick={() => !saving && setModalOpen(false)} className="flex h-8 w-8 items-center justify-center rounded-full" style={{ backgroundColor: C.cream }}>
                <X size={18} color={C.navy} />
              </button>
            </div>

            {!editing && (
              <p className="mt-2 text-xs" style={{ color: C.grey }}>
                Akun dokter baru akan dibuat dengan kredensial di bawah. Dokter bisa login langsung setelah dibuat.
              </p>
            )}

            <form onSubmit={handleSubmit} className="mt-4 grid gap-4 sm:grid-cols-2">
              {/* Nama */}
              <div>
                <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Nama <span style={{ color: C.orange }}>*</span></label>
                <input type="text" value={nama} onChange={(e) => setNama(e.target.value)} disabled={!!editing} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00] disabled:opacity-60" style={{ borderColor: C.line, color: C.navy }} required autoFocus />
              </div>

              {/* Email */}
              <div>
                <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Email <span style={{ color: C.orange }}>*</span></label>
                <div className="relative">
                  <Mail size={15} color={C.grey} className="absolute left-3 top-1/2 -translate-y-1/2" />
                  <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} disabled={!!editing} placeholder="dokter@rs.com" className="w-full rounded-2xl border py-2.5 pl-9 pr-4 text-sm outline-none focus:border-[#EF7A00] disabled:opacity-60" style={{ borderColor: C.line, color: C.navy }} required={!editing} />
                </div>
              </div>

              {/* Password (hanya saat create) */}
              {!editing && (
                <div className="sm:col-span-2">
                  <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Password <span style={{ color: C.orange }}>*</span></label>
                  <div className="relative">
                  <input type={showPw ? "text" : "password"} value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Min. 6 karakter" minLength={6} className="w-full rounded-2xl border px-4 py-2.5 pr-12 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required />
                  <button type="button" onClick={() => setShowPw(!showPw)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600" aria-label="Toggle password">
                    {showPw ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
                </div>
              )}

              {/* Poli */}
              <div>
                <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Poli <span style={{ color: C.orange }}>*</span></label>
                <select value={poliId} onChange={(e) => setPoliId(e.target.value ? Number(e.target.value) : "")} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required>
                  <option value="">Pilih poli...</option>
                  {poliList.map((p) => <option key={p.id} value={p.id}>{p.nama}</option>)}
                </select>
              </div>

              {/* No STR */}
              <div>
                <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>No STR <span style={{ color: C.orange }}>*</span></label>
                <input type="text" value={noStr} onChange={(e) => setNoStr(e.target.value)} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required />
              </div>

              {/* Spesialisasi */}
              <div>
                <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Spesialisasi <span style={{ color: C.orange }}>*</span></label>
                <input type="text" value={spesialisasi} onChange={(e) => setSpesialisasi(e.target.value)} placeholder="mis. Ortopedi" className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required />
              </div>

              {/* No HP */}
              <div>
                <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>No HP <span style={{ color: C.orange }}>*</span></label>
                <input type="tel" value={noHp} onChange={(e) => setNoHp(e.target.value)} placeholder="08xxxxxxxxxx" className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required />
              </div>

              {formError && (
                <div className="sm:col-span-2 flex items-start gap-2 rounded-2xl px-4 py-3" style={{ backgroundColor: "#FCE8E6" }}>
                  <AlertTriangle size={16} color="#C0392B" className="mt-0.5 flex-shrink-0" />
                  <p className="text-sm font-medium" style={{ color: "#C0392B" }}>{formError}</p>
                </div>
              )}

              <div className="sm:col-span-2 flex items-center gap-3">
                <button type="submit" disabled={saving} className="inline-flex items-center gap-2 rounded-full px-6 py-3 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5 disabled:opacity-50" style={{ backgroundColor: C.orange }}>
                  {saving ? <Loader2 className="animate-spin" size={17} /> : <Check size={17} />}
                  {saving ? "Menyimpan..." : "Simpan"}
                </button>
                <button type="button" onClick={() => !saving && setModalOpen(false)} disabled={saving} className="rounded-full px-6 py-3 text-sm font-semibold transition-opacity hover:opacity-80 disabled:opacity-50" style={{ backgroundColor: C.cream, color: C.navy }}>
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
