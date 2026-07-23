import { useCallback, useEffect, useState } from "react";
import {
  Users, Plus, Pencil, Trash2, X, Loader2, AlertTriangle, Check, IdCard,
  Eye, EyeOff,
} from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import {
  getAllPasienAdmin, createPasien, updatePasien, deactivatePasien,
} from "../../admin/services/adminService";
import type { PasienCreateRequest } from "../../admin/types";
import type { Pasien, PasienRequest, JenisKelamin } from "../../pasien/types";

/**
 * Halaman manajemen pasien untuk admin (CRUD: add/edit/delete).
 *
 * Menampilkan tabel semua pasien dengan operasi:
 * - Tambah: buat User(role=PASIEN) + profil Pasien sekaligus
 * - Edit: ubah NIK/alamat/noHp/dll
 * - Hapus: nonaktifkan akun User terkait
 *
 * @returns Halaman manajemen pasien
 */
export default function AdminPasien() {
  const [pasienList, setPasienList] = useState<Pasien[]>([]);
  const [sok, setSok] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Pasien | null>(null);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  // Form create
  const [nama, setNama] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPw, setShowPw] = useState(false);
  // Form shared (create + edit)
  const [nik, setNik] = useState("");
  const [tanggalLahir, setTanggalLahir] = useState("");
  const [jenisKelamin, setJenisKelamin] = useState<JenisKelamin>("L");
  const [alamat, setAlamat] = useState("");
  const [noHp, setNoHp] = useState("");
  const [golDarah, setGolDarah] = useState("");

  const muat = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getAllPasienAdmin();
      setPasienList(res.data);
    } catch (err) {
      setError(toErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { muat(); }, [muat]);

  const resetForm = () => {
    setNama(""); setEmail(""); setPassword(""); setNik(""); setTanggalLahir("");
    setJenisKelamin("L"); setAlamat(""); setNoHp(""); setGolDarah(""); setFormError(null);
  };

  const bukaTambah = () => { setEditing(null); resetForm(); setModalOpen(true); };

  const bukaEdit = (p: Pasien) => {
    setEditing(p);
    setNama(p.nama); setEmail(p.email); setPassword("");
    setNik(p.nik); setTanggalLahir(p.tanggalLahir);
    setJenisKelamin(p.jenisKelamin); setAlamat(p.alamat ?? "");
    setNoHp(p.noHp); setGolDarah(p.golDarah ?? "");
    setFormError(null);
    setModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!nama.trim() || !nik.trim() || !alamat.trim() || !noHp.trim() || !tanggalLahir) return;
    setSaving(true);
    setFormError(null);
    try {
      if (editing) {
        const payload: PasienRequest = {
          userId: editing.userId, nik: nik.trim(), tanggalLahir,
          jenisKelamin, alamat: alamat.trim(), noHp: noHp.trim(),
          golDarah: golDarah.trim() || null,
        };
        await updatePasien(editing.id, payload);
      } else {
        if (!email.trim() || !password.trim()) {
          setFormError("Email dan password wajib diisi untuk pasien baru");
          setSaving(false);
          return;
        }
        const payload: PasienCreateRequest = {
          nama: nama.trim(), email: email.trim(), password,
          nik: nik.trim(), tanggalLahir, jenisKelamin,
          alamat: alamat.trim(), noHp: noHp.trim(),
          golDarah: golDarah.trim() || null,
        };
        await createPasien(payload);
      }
      setModalOpen(false);
      await muat();
    } catch (err) {
      setFormError(toErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("Nonaktifkan akun pasien ini? Pasien tidak bisa login setelah dinonaktifkan.")) return;
    setDeletingId(id);
    try {
      await deactivatePasien(id);
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
        <p className="text-sm" style={{ color: C.grey }}>Memuat data pasien...</p>
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold" style={{ color: C.navy }}>Manajemen Pasien</h1>
          <p className="mt-1 text-sm" style={{ color: C.grey }}>Kelola data pasien (tambah/edit/hapus)</p>
        </div>
        <button onClick={bukaTambah} className="inline-flex items-center gap-2 rounded-full px-5 py-2.5 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5" style={{ backgroundColor: C.orange }}>
          <Plus size={17} /> Tambah Pasien
        </button>
      </div>

      {error && (
        <div className="mt-4 flex items-start gap-2 rounded-2xl px-4 py-3" style={{ backgroundColor: "#FCE8E6" }}>
          <AlertTriangle size={18} color="#C0392B" className="mt-0.5 flex-shrink-0" />
          <p className="text-sm font-medium" style={{ color: "#C0392B" }}>{error}</p>
        </div>
      )}

      <div className="mt-4"><input type="text" value={sok} onChange={e=>setSok(e.target.value)} placeholder="🔍 Cari nama / No.RM / NIK..." className="w-full max-w-sm rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line,color:C.navy}}/></div>

      <div className="mt-2 overflow-x-auto rounded-3xl border" style={{ backgroundColor: C.white, borderColor: C.line }}>
        <table className="w-full text-left text-sm">
          <thead>
            <tr style={{ backgroundColor: C.cream, borderBottom: `1px solid ${C.line}` }}>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Pasien</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>No. RM</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>NIK</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>No HP</th>
              <th className="px-5 py-3 text-right font-semibold" style={{ color: C.navy }}>Aksi</th>
            </tr>
          </thead>
          <tbody>
            {(()=>{const f=pasienList.filter(p=>!sok||p.nama.toLowerCase().includes(sok.toLowerCase())||p.noRekamMedis.toLowerCase().includes(sok.toLowerCase())||p.nik.toLowerCase().includes(sok.toLowerCase()));return f.length===0?(
              <tr><td colSpan={5} className="px-5 py-10 text-center" style={{ color: C.grey }}>Belum ada pasien.</td></tr>
            ) : (
              f.map((p) => (
                <tr key={p.id} style={{ borderBottom: `1px solid ${C.line}` }}>
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-3">
                      <div className="flex h-9 w-9 items-center justify-center rounded-full" style={{ backgroundColor: C.blueSoft }}>
                        <Users size={18} color={C.navy} />
                      </div>
                      <div>
                        <p className="font-semibold" style={{ color: C.navy }}>{p.nama}</p>
                        <p className="text-xs" style={{ color: C.grey }}>{p.email}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-4">
                    <span className="inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-xs font-semibold" style={{ backgroundColor: C.cream, color: C.navy }}>
                      <IdCard size={12} /> {p.noRekamMedis}
                    </span>
                  </td>
                  <td className="px-5 py-4" style={{ color: C.grey }}>{p.nik}</td>
                  <td className="px-5 py-4" style={{ color: C.grey }}>{p.noHp}</td>
                  <td className="px-5 py-4">
                    <div className="flex items-center justify-end gap-2">
                      <button onClick={() => bukaEdit(p)} className="flex h-8 w-8 items-center justify-center rounded-lg transition-colors hover:opacity-70" style={{ backgroundColor: C.cream }} aria-label="Edit">
                        <Pencil size={15} color={C.navy} />
                      </button>
                      <button onClick={() => handleDelete(p.id)} disabled={deletingId === p.id} className="flex h-8 w-8 items-center justify-center rounded-lg transition-colors hover:opacity-70 disabled:opacity-50" style={{ backgroundColor: "#FCE8E6" }} aria-label="Nonaktifkan">
                        {deletingId === p.id ? <Loader2 className="animate-spin" size={15} color="#C0392B" /> : <Trash2 size={15} color="#C0392B" />}
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
          <div className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-3xl border p-6" style={{ backgroundColor: C.white, borderColor: C.line }} onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-bold" style={{ color: C.navy }}>{editing ? "Edit Pasien" : "Tambah Pasien Baru"}</h2>
              <button onClick={() => !saving && setModalOpen(false)} className="flex h-8 w-8 items-center justify-center rounded-full" style={{ backgroundColor: C.cream }}>
                <X size={18} color={C.navy} />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="mt-4 grid gap-4 sm:grid-cols-2">
              <div>
                <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Nama <span style={{ color: C.orange }}>*</span></label>
                <input type="text" value={nama} onChange={(e) => setNama(e.target.value)} disabled={!!editing} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00] disabled:opacity-60" style={{ borderColor: C.line, color: C.navy }} required autoFocus />
              </div>
              <div>
                <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Email <span style={{ color: C.orange }}>*</span></label>
                <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} disabled={!!editing} placeholder="pasien@email.com" className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00] disabled:opacity-60" style={{ borderColor: C.line, color: C.navy }} required={!editing} />
              </div>
              {!editing && (
                <div className="sm:col-span-2">
                  <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Password <span style={{ color: C.orange }}>*</span></label>
                  <div className="relative">
                  <input type={showPw ? "text" : "password"} value={password} onChange={(e) => setPassword(e.target.value)} minLength={6} placeholder="Min. 6 karakter" className="w-full rounded-2xl border px-4 py-2.5 pr-12 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required />
                  <button type="button" onClick={() => setShowPw(!showPw)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600" aria-label="Toggle password">
                    {showPw ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
                </div>
              )}
              <div>
                <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>NIK <span style={{ color: C.orange }}>*</span></label>
                <input type="text" value={nik} onChange={(e) => setNik(e.target.value)} maxLength={16} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required />
              </div>
              <div>
                <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Tanggal Lahir <span style={{ color: C.orange }}>*</span></label>
                <input type="date" value={tanggalLahir} onChange={(e) => setTanggalLahir(e.target.value)} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required />
              </div>
              <div>
                <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Jenis Kelamin <span style={{ color: C.orange }}>*</span></label>
                <select value={jenisKelamin} onChange={(e) => setJenisKelamin(e.target.value as JenisKelamin)} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required>
                  <option value="L">Laki-laki</option>
                  <option value="P">Perempuan</option>
                </select>
              </div>
              <div>
                <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Golongan Darah</label>
                <select value={golDarah} onChange={(e) => setGolDarah(e.target.value)} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }}>
                  <option value="">Belum tahu</option>
                  <option value="A">A</option>
                  <option value="B">B</option>
                  <option value="AB">AB</option>
                  <option value="O">O</option>
                </select>
              </div>
              <div>
                <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>No HP <span style={{ color: C.orange }}>*</span></label>
                <input type="tel" value={noHp} onChange={(e) => setNoHp(e.target.value)} placeholder="08xxxxxxxxxx" className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required />
              </div>
              <div className="sm:col-span-2">
                <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Alamat <span style={{ color: C.orange }}>*</span></label>
                <textarea value={alamat} onChange={(e) => setAlamat(e.target.value)} rows={2} className="w-full resize-none rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required />
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
