import { useCallback, useEffect, useState } from "react";
import {
  Building2, Plus, Pencil, Trash2, X, Loader2, AlertTriangle, Check,
} from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import {
  getAllPoliAdmin, createPoli, updatePoli, deactivatePoli,
} from "../../admin/services/adminService";
import type { Poli } from "../../poli/types";
import type { PoliRequest } from "../../admin/types";

/**
 * Halaman manajemen poli untuk admin (FR-35).
 *
 * Menampilkan tabel semua poli (aktif & nonaktif) dengan operasi:
 * tambah, edit, dan nonaktifkan (soft delete).
 *
 * @returns Halaman manajemen poli
 */
export default function AdminPoli() {
  const [poliList, setPoliList] = useState<Poli[]>([]);
  const [loading, setLoading] = useState(true);
  const [sok, setSok] = useState(""); // search keyword
  const [error, setError] = useState<string | null>(null);

  // State modal form
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Poli | null>(null);
  const [nama, setNama] = useState("");
  const [deskripsi, setDeskripsi] = useState("");
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  // State nonaktifkan
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const muat = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getAllPoliAdmin();
      setPoliList(res.data);
    } catch (err) {
      setError(toErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { muat(); }, [muat]);

  /** Buka modal untuk tambah poli. */
  const bukaTambah = () => {
    setEditing(null);
    setNama("");
    setDeskripsi("");
    setFormError(null);
    setModalOpen(true);
  };

  /** Buka modal untuk edit poli. */
  const bukaEdit = (p: Poli) => {
    setEditing(p);
    setNama(p.nama);
    setDeskripsi(p.deskripsi ?? "");
    setFormError(null);
    setModalOpen(true);
  };

  /** Submit form (tambah atau edit). */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!nama.trim()) return;
    setSaving(true);
    setFormError(null);
    const payload: PoliRequest = { nama: nama.trim(), deskripsi: deskripsi.trim() || undefined };
    try {
      if (editing) {
        await updatePoli(editing.id, payload);
      } else {
        await createPoli(payload);
      }
      setModalOpen(false);
      await muat();
    } catch (err) {
      setFormError(toErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  /** Nonaktifkan poli (soft delete). */
  const handleDelete = async (id: number) => {
    if (!confirm("Nonaktifkan poli ini? Poli yang dinonaktifkan tidak muncul di pilihan pasien.")) return;
    setDeletingId(id);
    try {
      await deactivatePoli(id);
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
        <p className="text-sm" style={{ color: C.grey }}>Memuat data poli...</p>
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold" style={{ color: C.navy }}>Manajemen Poli</h1>
          <p className="mt-1 text-sm" style={{ color: C.grey }}>Kelola data poliklinik (FR-35)</p>
        </div>
        <button
          onClick={bukaTambah}
          className="inline-flex items-center gap-2 rounded-full px-5 py-2.5 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5"
          style={{ backgroundColor: C.orange }}
        >
          <Plus size={17} /> Tambah Poli
        </button>
      </div>

      {error && (
        <div
          className="mt-4 flex items-start gap-2 rounded-2xl px-4 py-3"
          style={{ backgroundColor: "#FCE8E6" }}
        >
          <AlertTriangle size={18} color="#C0392B" className="mt-0.5 flex-shrink-0" />
          <p className="text-sm font-medium" style={{ color: "#C0392B" }}>{error}</p>
        </div>
      )}

      <div className="mt-4"><input type="text" value={sok} onChange={e=>setSok(e.target.value)} placeholder="🔍 Cari nama / deskripsi..." className="w-full max-w-sm rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line,color:C.navy}}/></div>

      {/* Tabel poli */}
      <div
        className="mt-2 overflow-hidden rounded-3xl border"
        style={{ backgroundColor: C.white, borderColor: C.line }}
      >
        <table className="w-full text-left text-sm">
          <thead>
            <tr style={{ backgroundColor: C.cream, borderBottom: `1px solid ${C.line}` }}>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Nama Poli</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Deskripsi</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Status</th>
              <th className="px-5 py-3 text-right font-semibold" style={{ color: C.navy }}>Aksi</th>
            </tr>
          </thead>
          <tbody>
            {(() => { const f = poliList.filter(p => !sok || p.nama.toLowerCase().includes(sok.toLowerCase()) || (p.deskripsi||"").toLowerCase().includes(sok.toLowerCase())); return f.length === 0 ? (
              <tr>
                <td colSpan={4} className="px-5 py-10 text-center" style={{ color: C.grey }}>
                  Belum ada poli.
                </td>
              </tr>
            ) : (
              f.map((p) => (
                <tr key={p.id} style={{ borderBottom: `1px solid ${C.line}` }}>
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-3">
                      <div
                        className="flex h-9 w-9 items-center justify-center rounded-xl"
                        style={{ backgroundColor: C.blueSoft }}
                      >
                        <Building2 size={18} color={C.navy} />
                      </div>
                      <span className="font-semibold" style={{ color: C.navy }}>{p.nama}</span>
                    </div>
                  </td>
                  <td className="px-5 py-4" style={{ color: C.grey }}>
                    {p.deskripsi || "-"}
                  </td>
                  <td className="px-5 py-4">
                    <span
                      className="rounded-full px-2.5 py-1 text-xs font-semibold"
                      style={{
                        backgroundColor: p.isActive ? C.blueSoft : C.line,
                        color: p.isActive ? C.navy : C.grey,
                      }}
                    >
                      {p.isActive ? "Aktif" : "Nonaktif"}
                    </span>
                  </td>
                  <td className="px-5 py-4">
                    <div className="flex items-center justify-end gap-2">
                      <button
                        onClick={() => bukaEdit(p)}
                        className="flex h-8 w-8 items-center justify-center rounded-lg transition-colors hover:opacity-70"
                        style={{ backgroundColor: C.cream }}
                        aria-label="Edit"
                      >
                        <Pencil size={15} color={C.navy} />
                      </button>
                      {p.isActive && (
                        <button
                          onClick={() => handleDelete(p.id)}
                          disabled={deletingId === p.id}
                          className="flex h-8 w-8 items-center justify-center rounded-lg transition-colors hover:opacity-70 disabled:opacity-50"
                          style={{ backgroundColor: "#FCE8E6" }}
                          aria-label="Nonaktifkan"
                        >
                          {deletingId === p.id ? (
                            <Loader2 className="animate-spin" size={15} color="#C0392B" />
                          ) : (
                            <Trash2 size={15} color="#C0392B" />
                          )}
                        </button>
                      )}
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
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
          onClick={() => !saving && setModalOpen(false)}
        >
          <div
            className="w-full max-w-md rounded-3xl border p-6"
            style={{ backgroundColor: C.white, borderColor: C.line }}
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-bold" style={{ color: C.navy }}>
                {editing ? "Edit Poli" : "Tambah Poli"}
              </h2>
              <button
                onClick={() => !saving && setModalOpen(false)}
                className="flex h-8 w-8 items-center justify-center rounded-full"
                style={{ backgroundColor: C.cream }}
              >
                <X size={18} color={C.navy} />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="mt-5 flex flex-col gap-4">
              <div>
                <label className="mb-1.5 block text-sm font-semibold" style={{ color: C.navy }}>
                  Nama Poli <span style={{ color: C.orange }}>*</span>
                </label>
                <input
                  type="text"
                  value={nama}
                  onChange={(e) => setNama(e.target.value)}
                  placeholder="mis. Poli Umum"
                  className="w-full rounded-2xl border px-4 py-3 text-sm outline-none focus:border-[#EF7A00]"
                  style={{ borderColor: C.line, color: C.navy }}
                  required
                  autoFocus
                />
              </div>

              <div>
                <label className="mb-1.5 block text-sm font-semibold" style={{ color: C.navy }}>
                  Deskripsi
                </label>
                <textarea
                  value={deskripsi}
                  onChange={(e) => setDeskripsi(e.target.value)}
                  rows={3}
                  placeholder="Deskripsi singkat poli (opsional)"
                  className="w-full resize-none rounded-2xl border px-4 py-3 text-sm outline-none focus:border-[#EF7A00]"
                  style={{ borderColor: C.line, color: C.navy }}
                />
              </div>

              {formError && (
                <div
                  className="flex items-start gap-2 rounded-2xl px-4 py-3"
                  style={{ backgroundColor: "#FCE8E6" }}
                >
                  <AlertTriangle size={16} color="#C0392B" className="mt-0.5 flex-shrink-0" />
                  <p className="text-sm font-medium" style={{ color: "#C0392B" }}>{formError}</p>
                </div>
              )}

              <div className="flex items-center gap-3">
                <button
                  type="submit"
                  disabled={saving}
                  className="inline-flex items-center gap-2 rounded-full px-6 py-3 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5 disabled:opacity-50"
                  style={{ backgroundColor: C.orange }}
                >
                  {saving ? <Loader2 className="animate-spin" size={17} /> : <Check size={17} />}
                  {saving ? "Menyimpan..." : "Simpan"}
                </button>
                <button
                  type="button"
                  onClick={() => !saving && setModalOpen(false)}
                  disabled={saving}
                  className="rounded-full px-6 py-3 text-sm font-semibold transition-opacity hover:opacity-80 disabled:opacity-50"
                  style={{ backgroundColor: C.cream, color: C.navy }}
                >
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
