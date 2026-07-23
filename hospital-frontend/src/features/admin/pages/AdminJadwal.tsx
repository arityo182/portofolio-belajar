import { useCallback, useEffect, useState } from "react";
import {
  Plus, Pencil, Trash2, X, Loader2, AlertTriangle, Check,
} from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import {
  getAllJadwalAdmin, createJadwal, updateJadwal, deactivateJadwal, getAllDokterAdmin,
} from "../../admin/services/adminService";
import type { JadwalRequest } from "../../admin/types";
import type { JadwalDokter, Hari } from "../../jadwal/types";
import type { AdminDokter } from "../../admin/types";

/** Label hari ramah-baca. */
const LABEL_HARI: Record<Hari, string> = {
  SENIN: "Senin", SELASA: "Selasa", RABU: "Rabu", KAMIS: "Kamis",
  JUMAT: "Jumat", SABTU: "Sabtu", MINGGU: "Minggu",
};

/** Daftar hari untuk select. */
const DAFTAR_HARI: Hari[] = ["SENIN", "SELASA", "RABU", "KAMIS", "JUMAT", "SABTU", "MINGGU"];

/**
 * Halaman manajemen jadwal praktik dokter untuk admin (FR-36).
 *
 * @returns Halaman manajemen jadwal
 */
export default function AdminJadwal() {
  const [jadwalList, setJadwalList] = useState<JadwalDokter[]>([]);
  const [sok, setSok] = useState("");
  const [dokterList, setDokterList] = useState<AdminDokter[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Modal
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<JadwalDokter | null>(null);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  // Form
  const [dokterId, setDokterId] = useState<number | "">("");
  const [hari, setHari] = useState<Hari>("SENIN");
  const [jamMulai, setJamMulai] = useState("08:00");
  const [jamSelesai, setJamSelesai] = useState("12:00");
  const [kuota, setKuota] = useState(10);

  const [deletingId, setDeletingId] = useState<number | null>(null);

  const muat = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [jRes, dRes] = await Promise.all([getAllJadwalAdmin(), getAllDokterAdmin()]);
      setJadwalList(jRes.data);
      setDokterList(dRes.data);
    } catch (err) {
      setError(toErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { muat(); }, [muat]);

  /** Buka modal tambah. */
  const bukaTambah = () => {
    setEditing(null);
    setDokterId(""); setHari("SENIN"); setJamMulai("08:00"); setJamSelesai("12:00"); setKuota(10);
    setFormError(null);
    setModalOpen(true);
  };

  /** Buka modal edit. */
  const bukaEdit = (j: JadwalDokter) => {
    setEditing(j);
    setDokterId(j.dokterId);
    setHari(j.hari);
    setJamMulai(j.jamMulai.slice(0, 5));
    setJamSelesai(j.jamSelesai.slice(0, 5));
    setKuota(j.kuota);
    setFormError(null);
    setModalOpen(true);
  };

  /** Submit form. */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (dokterId === "" || kuota < 1) return;
    setSaving(true);
    setFormError(null);
    // Backend butuh format "HH:mm:ss"
    const payload: JadwalRequest = {
      dokterId: Number(dokterId),
      hari,
      jamMulai: `${jamMulai}:00`,
      jamSelesai: `${jamSelesai}:00`,
      kuota,
    };
    try {
      if (editing) {
        await updateJadwal(editing.id, payload);
      } else {
        await createJadwal(payload);
      }
      setModalOpen(false);
      await muat();
    } catch (err) {
      setFormError(toErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  /** Nonaktifkan jadwal. */
  const handleDelete = async (id: number) => {
    if (!confirm("Nonaktifkan jadwal ini?")) return;
    setDeletingId(id);
    try {
      await deactivateJadwal(id);
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
        <p className="text-sm" style={{ color: C.grey }}>Memuat data jadwal...</p>
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold" style={{ color: C.navy }}>Manajemen Jadwal</h1>
          <p className="mt-1 text-sm" style={{ color: C.grey }}>Kelola jadwal praktik dokter (FR-36)</p>
        </div>
        <button onClick={bukaTambah} className="inline-flex items-center gap-2 rounded-full px-5 py-2.5 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5" style={{ backgroundColor: C.orange }}>
          <Plus size={17} /> Tambah Jadwal
        </button>
      </div>

      {error && (
        <div className="mt-4 flex items-start gap-2 rounded-2xl px-4 py-3" style={{ backgroundColor: "#FCE8E6" }}>
          <AlertTriangle size={18} color="#C0392B" className="mt-0.5 flex-shrink-0" />
          <p className="text-sm font-medium" style={{ color: "#C0392B" }}>{error}</p>
        </div>
      )}

      <div className="mt-4"><input type="text" value={sok} onChange={e=>setSok(e.target.value)} placeholder="🔍 Cari nama dokter..." className="w-full max-w-sm rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line,color:C.navy}}/></div>

      <div className="mt-2 overflow-x-auto rounded-3xl border" style={{ backgroundColor: C.white, borderColor: C.line }}>
        <table className="w-full text-left text-sm">
          <thead>
            <tr style={{ backgroundColor: C.cream, borderBottom: `1px solid ${C.line}` }}>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Dokter</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Hari</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Jam</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Kuota</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Status</th>
              <th className="px-5 py-3 text-right font-semibold" style={{ color: C.navy }}>Aksi</th>
            </tr>
          </thead>
          <tbody>
            {(()=>{const f=jadwalList.filter(j=>!sok||j.namaDokter.toLowerCase().includes(sok.toLowerCase()));return f.length===0?(
              <tr><td colSpan={6} className="px-5 py-10 text-center" style={{ color: C.grey }}>Belum ada jadwal.</td></tr>
            ) : (
              f.map((j) => (
                <tr key={j.id} style={{ borderBottom: `1px solid ${C.line}` }}>
                  <td className="px-5 py-4 font-semibold" style={{ color: C.navy }}>{j.namaDokter}</td>
                  <td className="px-5 py-4" style={{ color: C.grey }}>{LABEL_HARI[j.hari]}</td>
                  <td className="px-5 py-4" style={{ color: C.grey }}>{j.jamMulai.slice(0, 5)} – {j.jamSelesai.slice(0, 5)}</td>
                  <td className="px-5 py-4" style={{ color: C.grey }}>{j.kuota}</td>
                  <td className="px-5 py-4">
                    <span className="rounded-full px-2.5 py-1 text-xs font-semibold" style={{ backgroundColor: j.isActive ? C.blueSoft : C.line, color: j.isActive ? C.navy : C.grey }}>
                      {j.isActive ? "Aktif" : "Nonaktif"}
                    </span>
                  </td>
                  <td className="px-5 py-4">
                    <div className="flex items-center justify-end gap-2">
                      <button onClick={() => bukaEdit(j)} className="flex h-8 w-8 items-center justify-center rounded-lg transition-colors hover:opacity-70" style={{ backgroundColor: C.cream }} aria-label="Edit">
                        <Pencil size={15} color={C.navy} />
                      </button>
                      {j.isActive && (
                        <button onClick={() => handleDelete(j.id)} disabled={deletingId === j.id} className="flex h-8 w-8 items-center justify-center rounded-lg transition-colors hover:opacity-70 disabled:opacity-50" style={{ backgroundColor: "#FCE8E6" }} aria-label="Nonaktifkan">
                          {deletingId === j.id ? <Loader2 className="animate-spin" size={15} color="#C0392B" /> : <Trash2 size={15} color="#C0392B" />}
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
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={() => !saving && setModalOpen(false)}>
          <div className="w-full max-w-md rounded-3xl border p-6" style={{ backgroundColor: C.white, borderColor: C.line }} onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-bold" style={{ color: C.navy }}>{editing ? "Edit Jadwal" : "Tambah Jadwal"}</h2>
              <button onClick={() => !saving && setModalOpen(false)} className="flex h-8 w-8 items-center justify-center rounded-full" style={{ backgroundColor: C.cream }}>
                <X size={18} color={C.navy} />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="mt-5 flex flex-col gap-4">
              <div>
                <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Dokter <span style={{ color: C.orange }}>*</span></label>
                <select value={dokterId} onChange={(e) => setDokterId(e.target.value ? Number(e.target.value) : "")} disabled={!!editing} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00] disabled:opacity-60" style={{ borderColor: C.line, color: C.navy }} required>
                  <option value="">Pilih dokter...</option>
                  {dokterList.map((d) => <option key={d.id} value={d.id}>{d.nama} — {d.spesialisasi}</option>)}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Hari <span style={{ color: C.orange }}>*</span></label>
                  <select value={hari} onChange={(e) => setHari(e.target.value as Hari)} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required>
                    {DAFTAR_HARI.map((h) => <option key={h} value={h}>{LABEL_HARI[h]}</option>)}
                  </select>
                </div>
                <div>
                  <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Kuota <span style={{ color: C.orange }}>*</span></label>
                  <input type="number" value={kuota} onChange={(e) => setKuota(Number(e.target.value))} min={1} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Jam Mulai <span style={{ color: C.orange }}>*</span></label>
                  <input type="time" value={jamMulai} onChange={(e) => setJamMulai(e.target.value)} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required />
                </div>
                <div>
                  <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Jam Selesai <span style={{ color: C.orange }}>*</span></label>
                  <input type="time" value={jamSelesai} onChange={(e) => setJamSelesai(e.target.value)} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required />
                </div>
              </div>

              {formError && (
                <div className="flex items-start gap-2 rounded-2xl px-4 py-3" style={{ backgroundColor: "#FCE8E6" }}>
                  <AlertTriangle size={16} color="#C0392B" className="mt-0.5 flex-shrink-0" />
                  <p className="text-sm font-medium" style={{ color: "#C0392B" }}>{formError}</p>
                </div>
              )}

              <div className="flex items-center gap-3">
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
