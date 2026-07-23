import { useCallback, useEffect, useState } from "react";
import { AxiosError } from "axios";
import {
  CalendarDays, Loader2, AlertTriangle, ClipboardList, Ticket, X, Plus,
  Check, Pill, Stethoscope,
} from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import { useAuth } from "../../../core/context/AuthContext";
import {
  getDokterIdByUserId, getAppointmentsByDokter, updateAppointmentStatus,
  createRekamMedis, createResep,
} from "../services/dokterService";
import api from "../../../core/api/client";
import type { AppointmentResponse, StatusAppointment } from "../../appointment/types";
import type { Resep } from "../../rekammedis/types";

/** Warna badge per status. */
const STATUS_STYLE: Record<StatusAppointment, { bg: string; fg: string }> = {
  MENUNGGU: { bg: C.orangeSoft, fg: C.orange },
  DIPERIKSA: { bg: C.blueSoft, fg: C.navy },
  SELESAI: { bg: "#E0F2E9", fg: "#1B8A5A" },
  BATAL: { bg: "#FCE8E6", fg: "#C0392B" },
};

const STATUS_LABEL: Record<StatusAppointment, string> = {
  MENUNGGU: "Menunggu", DIPERIKSA: "Diperiksa", SELESAI: "Selesai", BATAL: "Batal",
};

/**
 * Halaman antrian pasien dokter — daftar appointment pasien yang booking
 * ke dokter tersebut, dengan aksi: mulai periksa (DIPERIKSA), buat rekam
 * medis + resep, selesaikan (SELESAI).
 *
 * @returns Halaman antrian pasien dokter
 */
export default function DokterAntrian() {
  const { user } = useAuth();
  const [dokterId, setDokterId] = useState<number | null>(null);
  const [appointments, setAppointments] = useState<AppointmentResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState<StatusAppointment | "">("");
  const [hariIniOnly, setHariIniOnly] = useState(true); // default: tampilkan hari ini

  // Modal rekam medis
  const [rmModalOpen, setRmModalOpen] = useState(false);
  const [selectedAppt, setSelectedAppt] = useState<AppointmentResponse | null>(null);
  const [diagnosa, setDiagnosa] = useState("");
  const [tindakan, setTindakan] = useState("");
  const [catatan, setCatatan] = useState("");
  const [savingRm, setSavingRm] = useState(false);
  const [rmError, setRmError] = useState<string | null>(null);

  // Resep
  const [resepList, setResepList] = useState<Resep[]>([]);
  const [rmCreated, setRmCreated] = useState<number | null>(null);
  const [obatList, setObatList] = useState<{id:number;nama:string;satuan:string}[]>([]);
  const [selectedObatId, setSelectedObatId] = useState<number | "">("");
  const [namaObat, setNamaObat] = useState("");
  const [dosis, setDosis] = useState("");
  const [jumlah, setJumlah] = useState(1);
  const [aturan, setAturan] = useState("");
  const [addingResep, setAddingResep] = useState(false);

  const muat = useCallback(async () => {
    if (!user || dokterId === null) return;
    setLoading(true);
    setError(null);
    try {
      const res = await getAppointmentsByDokter(dokterId);
      const sorted = [...res.data].sort((a, b) => {
        // Urutkan: MENUNGGU dulu, lalu DIPERIKSA, lalu sisanya
        const order: Record<StatusAppointment, number> = { MENUNGGU: 0, DIPERIKSA: 1, SELESAI: 2, BATAL: 3 };
        return order[a.status] - order[b.status] || b.id - a.id;
      });
      setAppointments(sorted);
    } catch (err) {
      setError(toErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [user, dokterId]);

  // Resolve dokterId dari user login
  useEffect(() => {
    if (!user) return;
    let aktif = true;
    getDokterIdByUserId(user.id)
      .then((res) => aktif && setDokterId(res.data.id))
      .catch((err: unknown) => aktif && setError(toErrorMessage(err)));
    return () => { aktif = false; };
  }, [user]);

  useEffect(() => { if (dokterId !== null) muat(); }, [muat, dokterId]);

  /** Mulai periksa pasien (status → DIPERIKSA). */
  const handleMulaiPeriksa = async (id: number) => {
    try {
      await updateAppointmentStatus(id, "DIPERIKSA");
      await muat();
    } catch (err) {
      setError(toErrorMessage(err));
    }
  };

  /** Buka modal rekam medis untuk appointment. */
  const bukaRekamMedis = async (a: AppointmentResponse) => {
    setSelectedAppt(a);
    setDiagnosa(""); setTindakan(""); setCatatan("");
    setResepList([]); setRmCreated(null);
    setSelectedObatId(""); setNamaObat(""); setDosis(""); setJumlah(1); setAturan("");
    setRmError(null);
    // Muat daftar obat untuk dropdown
    try { const r = await api.get<{id:number;nama:string;satuan:string}[]>("/obat"); setObatList(r.data); }
    catch { setObatList([]); }
    setRmModalOpen(true);
  };

  /** Submit rekam medis. */
  const handleSaveRm = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedAppt || !diagnosa.trim() || !tindakan.trim()) return;
    setSavingRm(true);
    setRmError(null);
    try {
      const res = await createRekamMedis({
        appointmentId: selectedAppt.id,
        diagnosa: diagnosa.trim(),
        tindakan: tindakan.trim(),
        catatan: catatan.trim() || undefined,
      });
      setRmCreated(res.data.id);
      // Status appointment auto SELESAI di backend
      await muat();
    } catch (err) {
      if (err instanceof AxiosError && err.response?.status === 409) {
        setRmError("Appointment ini sudah memiliki rekam medis.");
      } else {
        setRmError(toErrorMessage(err));
      }
    } finally {
      setSavingRm(false);
    }
  };

  /** Tambah resep ke rekam medis yang baru dibuat. */
  const handleAddResep = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!rmCreated) return;
    if (!selectedObatId && !namaObat.trim()) { setRmError("Pilih obat dari daftar atau isi nama obat manual"); return; }
    setAddingResep(true);
    try {
      const payload: Record<string, unknown> = {
        rekamMedisId: rmCreated,
        dosis: dosis.trim() || undefined,
        jumlah,
        aturanPakai: aturan.trim() || undefined,
      };
      if (selectedObatId) { payload.obatId = Number(selectedObatId); }
      else { payload.namaObat = namaObat.trim(); }
      const res = await createResep(payload as Parameters<typeof createResep>[0]);
      setResepList([...resepList, res.data]);
      setSelectedObatId(""); setNamaObat(""); setDosis(""); setJumlah(1); setAturan("");
    } catch (err) {
      setRmError(toErrorMessage(err));
    } finally {
      setAddingResep(false);
    }
  };

  const filtered = appointments.filter((a) => {
    if (filter && a.status !== filter) return false;
    if (hariIniOnly && a.tanggal !== todayStr()) return false;
    return true;
  });

  if (loading && appointments.length === 0) {
    return (
      <div className="flex flex-col items-center gap-3 py-20">
        <Loader2 className="animate-spin" size={28} color={C.navy} />
        <p className="text-sm" style={{ color: C.grey }}>Memuat antrian pasien...</p>
      </div>
    );
  }

  return (
    <div>
      <h1 className="text-2xl font-bold" style={{ color: C.navy }}>Antrian Pasien</h1>
      <p className="mt-1 text-sm" style={{ color: C.grey }}>Pasien yang booking janji temu dengan Anda</p>

      {/* Toggle Hari Ini / Semua */}
      <div className="mt-4 flex flex-wrap items-center gap-2">
        <button
          onClick={() => setHariIniOnly(!hariIniOnly)}
          className="inline-flex items-center gap-1.5 rounded-full px-4 py-1.5 text-xs font-semibold"
          style={{ backgroundColor: hariIniOnly ? C.orange : C.cream, color: hariIniOnly ? C.white : C.grey }}
        >
          <CalendarDays size={14} /> {hariIniOnly ? "Hari Ini" : "Semua Tanggal"}
        </button>
        {(["", "MENUNGGU", "DIPERIKSA", "SELESAI", "BATAL"] as const).map((f) => (
          <button key={f} onClick={() => setFilter(f)} className="rounded-full px-4 py-1.5 text-xs font-semibold" style={{ backgroundColor: filter === f ? C.navy : C.cream, color: filter === f ? C.white : C.grey }}>
            {f === "" ? "Semua Status" : STATUS_LABEL[f]}
          </button>
        ))}
      </div>

      {error && (
        <div className="mt-4 flex items-start gap-2 rounded-2xl px-4 py-3" style={{ backgroundColor: "#FCE8E6" }}>
          <AlertTriangle size={18} color="#C0392B" className="mt-0.5 flex-shrink-0" />
          <p className="text-sm font-medium" style={{ color: "#C0392B" }}>{error}</p>
        </div>
      )}

      {/* Daftar appointment */}
      <div className="mt-6 flex flex-col gap-4">
        {filtered.length === 0 ? (
          <div className="flex flex-col items-center gap-3 rounded-3xl border border-dashed py-16" style={{ borderColor: C.line }}>
            <CalendarDays size={30} color={C.grey} />
            <p className="text-sm" style={{ color: C.grey }}>Tidak ada janji temu.</p>
          </div>
        ) : (
          filtered.map((a) => {
            const st = STATUS_STYLE[a.status];
            return (
              <div key={a.id} className="rounded-3xl border p-5" style={{ backgroundColor: C.white, borderColor: C.line }}>
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="flex items-center gap-3">
                    {a.nomorAntrian !== null && (
                      <div className="flex items-center gap-1.5 rounded-xl px-3 py-2" style={{ backgroundColor: C.navy }}>
                        <Ticket size={16} color={C.white} />
                        <span className="text-lg font-bold text-white">{a.nomorAntrian}</span>
                      </div>
                    )}
                    <div>
                      <h3 className="font-bold" style={{ color: C.navy }}>{a.namaPasien}</h3>
                      <p className="text-xs" style={{ color: C.grey }}>No. RM: {a.noRekamMedis}</p>
                      <p className="text-xs" style={{ color: C.grey }}>{formatTanggal(a.tanggal)} · {a.jamMulai.slice(0, 5)}–{a.jamSelesai.slice(0, 5)}</p>
                      {a.keluhan && <p className="mt-1 text-sm" style={{ color: C.grey }}><span className="font-semibold" style={{ color: C.navy }}>Keluhan: </span>{a.keluhan}</p>}
                    </div>
                  </div>
                  <span className="rounded-full px-3 py-1 text-xs font-semibold" style={{ backgroundColor: st.bg, color: st.fg }}>
                    {STATUS_LABEL[a.status]}
                  </span>
                </div>

                {/* Aksi */}
                <div className="mt-4 flex flex-wrap gap-2">
                  {a.status === "MENUNGGU" && (
                    <button onClick={() => handleMulaiPeriksa(a.id)} className="inline-flex items-center gap-1.5 rounded-full px-4 py-2 text-xs font-semibold text-white" style={{ backgroundColor: C.blue }}>
                      <Stethoscope size={14} /> Mulai Periksa
                    </button>
                  )}
                  {a.status === "DIPERIKSA" && (
                    <button onClick={() => bukaRekamMedis(a)} className="inline-flex items-center gap-1.5 rounded-full px-4 py-2 text-xs font-semibold text-white" style={{ backgroundColor: C.orange }}>
                      <ClipboardList size={14} /> Buat Rekam Medis
                    </button>
                  )}
                  {a.status === "SELESAI" && (
                    <span className="inline-flex items-center gap-1.5 rounded-full px-4 py-2 text-xs font-semibold" style={{ backgroundColor: "#E0F2E9", color: "#1B8A5A" }}>
                      <Check size={14} /> Selesai Diperiksa
                    </span>
                  )}
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Modal Rekam Medis + Resep */}
      {rmModalOpen && selectedAppt && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={() => !savingRm && !addingResep && setRmModalOpen(false)}>
          <div className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-3xl border p-6" style={{ backgroundColor: C.white, borderColor: C.line }} onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-lg font-bold" style={{ color: C.navy }}>Rekam Medis</h2>
                <p className="text-sm" style={{ color: C.grey }}>{selectedAppt.namaPasien} · {formatTanggal(selectedAppt.tanggal)}</p>
              </div>
              <button onClick={() => !savingRm && !addingResep && setRmModalOpen(false)} className="flex h-8 w-8 items-center justify-center rounded-full" style={{ backgroundColor: C.cream }}>
                <X size={18} color={C.navy} />
              </button>
            </div>

            {rmCreated === null ? (
              // Form rekam medis
              <form onSubmit={handleSaveRm} className="mt-5 flex flex-col gap-4">
                <div>
                  <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Diagnosa <span style={{ color: C.orange }}>*</span></label>
                  <textarea value={diagnosa} onChange={(e) => setDiagnosa(e.target.value)} rows={2} placeholder="Diagnosa pasien" className="w-full resize-none rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required />
                </div>
                <div>
                  <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Tindakan <span style={{ color: C.orange }}>*</span></label>
                  <textarea value={tindakan} onChange={(e) => setTindakan(e.target.value)} rows={2} placeholder="Tindakan yang dilakukan" className="w-full resize-none rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required />
                </div>
                <div>
                  <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Catatan</label>
                  <textarea value={catatan} onChange={(e) => setCatatan(e.target.value)} rows={2} placeholder="Catatan tambahan (opsional)" className="w-full resize-none rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} />
                </div>

                {rmError && (
                  <div className="flex items-start gap-2 rounded-2xl px-4 py-3" style={{ backgroundColor: "#FCE8E6" }}>
                    <AlertTriangle size={16} color="#C0392B" className="mt-0.5 flex-shrink-0" />
                    <p className="text-sm font-medium" style={{ color: "#C0392B" }}>{rmError}</p>
                  </div>
                )}

                <button type="submit" disabled={savingRm} className="inline-flex items-center gap-2 rounded-full px-6 py-3 text-sm font-semibold text-white disabled:opacity-50" style={{ backgroundColor: C.orange }}>
                  {savingRm ? <Loader2 className="animate-spin" size={17} /> : <Check size={17} />}
                  {savingRm ? "Menyimpan..." : "Simpan Rekam Medis & Tambah Resep"}
                </button>
              </form>
            ) : (
              // Resep section (setelah RM dibuat)
              <div className="mt-5">
                <div className="mb-4 flex items-center gap-2 rounded-2xl px-4 py-3" style={{ backgroundColor: "#E0F2E9" }}>
                  <Check size={18} color="#1B8A5A" />
                  <p className="text-sm font-semibold" style={{ color: "#1B8A5A" }}>Rekam medis berhasil dibuat. Sekarang tambahkan resep obat.</p>
                </div>

                {/* Daftar resep sudah ditambah */}
                {resepList.length > 0 && (
                  <div className="mb-4">
                    <h3 className="mb-2 flex items-center gap-2 text-sm font-bold" style={{ color: C.navy }}>
                      <Pill size={16} color={C.orange} /> Resep Obat ({resepList.length})
                    </h3>
                    <ul className="flex flex-col gap-2">
                      {resepList.map((r) => (
                        <li key={r.id} className="flex flex-wrap items-center gap-x-4 gap-y-1 rounded-2xl px-4 py-3" style={{ backgroundColor: C.cream }}>
                          <span className="font-semibold" style={{ color: C.navy }}>{r.namaObat}</span>
                          {r.dosis && <span className="text-sm" style={{ color: C.grey }}>{r.dosis}</span>}
                          <span className="text-sm" style={{ color: C.grey }}>Jml: {r.jumlah}</span>
                          {r.aturanPakai && <span className="text-sm" style={{ color: C.grey }}>{r.aturanPakai}</span>}
                        </li>
                      ))}
                    </ul>
                  </div>
                )}

                {/* Form tambah resep */}
                <form onSubmit={handleAddResep} className="grid gap-3 sm:grid-cols-2">
                  <div className="sm:col-span-2">
                    <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Obat</label>
                    {obatList.length > 0 && (
                      <select value={selectedObatId} onChange={(e) => { setSelectedObatId(e.target.value ? Number(e.target.value) : ""); setNamaObat(""); }} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00] mb-2" style={{ borderColor: C.line, color: C.navy }}>
                        <option value="">-- Pilih dari daftar obat --</option>
                        {obatList.map((o) => <option key={o.id} value={o.id}>{o.nama} ({o.satuan})</option>)}
                      </select>
                    )}
                    {!selectedObatId && (
                      <input type="text" value={namaObat} onChange={(e) => setNamaObat(e.target.value)} placeholder="Atau ketik nama obat manual..." className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} />
                    )}
                    <p className="mt-1 text-xs" style={{ color: C.grey }}>Pilih dari daftar obat, atau ketik manual jika tidak ada.</p>
                  </div>
                  <div>
                    <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Dosis</label>
                    <input type="text" value={dosis} onChange={(e) => setDosis(e.target.value)} placeholder="mis. 500mg" className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} />
                  </div>
                  <div>
                    <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Jumlah</label>
                    <input type="number" value={jumlah} onChange={(e) => setJumlah(Number(e.target.value))} min={1} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} required />
                  </div>
                  <div className="sm:col-span-2">
                    <label className="mb-1 block text-sm font-semibold" style={{ color: C.navy }}>Aturan Pakai</label>
                    <input type="text" value={aturan} onChange={(e) => setAturan(e.target.value)} placeholder="mis. 3x sehari setelah makan" className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{ borderColor: C.line, color: C.navy }} />
                  </div>
                  <div className="sm:col-span-2 flex items-center gap-3">
                    <button type="submit" disabled={addingResep} className="inline-flex items-center gap-2 rounded-full px-5 py-2.5 text-sm font-semibold text-white disabled:opacity-50" style={{ backgroundColor: C.navy }}>
                      {addingResep ? <Loader2 className="animate-spin" size={16} /> : <Plus size={16} />}
                      {addingResep ? "Menambah..." : "Tambah Resep"}
                    </button>
                    <button type="button" onClick={() => setRmModalOpen(false)} className="rounded-full px-5 py-2.5 text-sm font-semibold" style={{ backgroundColor: C.cream, color: C.navy }}>
                      Selesai
                    </button>
                  </div>
                </form>

                {rmError && (
                  <div className="mt-3 flex items-start gap-2 rounded-2xl px-4 py-3" style={{ backgroundColor: "#FCE8E6" }}>
                    <AlertTriangle size={16} color="#C0392B" className="mt-0.5 flex-shrink-0" />
                    <p className="text-sm font-medium" style={{ color: "#C0392B" }}>{rmError}</p>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

function formatTanggal(iso: string): string {
  const [y, m, d] = iso.split("-").map(Number);
  if (!y || !m || !d) return iso;
  const bulan = ["Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"];
  return `${d} ${bulan[m - 1]} ${y}`;
}

/** Tanggal hari ini format "yyyy-MM-dd". */
function todayStr(): string {
  const n = new Date();
  const mm = String(n.getMonth() + 1).padStart(2, "0");
  const dd = String(n.getDate()).padStart(2, "0");
  return `${n.getFullYear()}-${mm}-${dd}`;
}
