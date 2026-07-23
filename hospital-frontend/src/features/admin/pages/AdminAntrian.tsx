import { useCallback, useEffect, useState } from "react";
import { Ticket, Loader2, AlertTriangle, RefreshCw, Users } from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import { getAllAntrianAdmin } from "../../admin/services/adminService";
import type { AntrianAdmin } from "../../admin/types";

/** Warna badge per status antrian. */
const STATUS_STYLE: Record<string, { bg: string; fg: string }> = {
  MENUNGGU: { bg: C.orangeSoft, fg: C.orange },
  DIPERIKSA: { bg: C.blueSoft, fg: C.navy },
  SELESAI: { bg: "#E0F2E9", fg: "#1B8A5A" },
  BATAL: { bg: "#FCE8E6", fg: "#C0392B" },
};

/** Label status ramah-baca. */
const STATUS_LABEL: Record<string, string> = {
  MENUNGGU: "Menunggu",
  DIPERIKSA: "Diperiksa",
  SELESAI: "Selesai",
  BATAL: "Batal",
};

/**
 * Halaman papan antrian admin — menampilkan semua antrian dengan status.
 *
 * @returns Halaman papan antrian
 */
export default function AdminAntrian() {
  const [antrian, setAntrian] = useState<AntrianAdmin[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState<string>("");
  const [poliFilter, setPoliFilter] = useState("");
  const [tglFilter, setTglFilter] = useState("");

  // Unique poli names for dropdown
  const poliNames = [...new Set(antrian.map(a => a.namaPoli).filter(Boolean))];

  const muat = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getAllAntrianAdmin();
      setAntrian(res.data);
    } catch (err) {
      setError(toErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { muat(); }, [muat]);

  /** Auto-refresh setiap 15 detik. */
  useEffect(() => {
    const t = setInterval(muat, 15000);
    return () => clearInterval(t);
  }, [muat]);

  const filtered = antrian.filter(a => {
    if (filter && a.status !== filter) return false;
    if (poliFilter && a.namaPoli !== poliFilter) return false;
    if (tglFilter && a.tanggal !== tglFilter) return false;
    return true;
  });
  // Cluster: group by (namaPoli, tanggal)
  const clusters: Record<string, AntrianAdmin[]> = {};
  filtered.forEach(a => {
    const key = `${a.namaPoli || "Tanpa Poli"} | ${a.tanggal}`;
    if (!clusters[key]) clusters[key] = [];
    clusters[key].push(a);
  });
  const jumlahMenunggu = antrian.filter((a) => a.status === "MENUNGGU").length;
  const jumlahDiperiksa = antrian.filter((a) => a.status === "DIPERIKSA").length;
  const jumlahSelesai = antrian.filter((a) => a.status === "SELESAI").length;

  if (loading && antrian.length === 0) {
    return (
      <div className="flex flex-col items-center gap-3 py-20">
        <Loader2 className="animate-spin" size={28} color={C.navy} />
        <p className="text-sm" style={{ color: C.grey }}>Memuat antrian...</p>
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold" style={{ color: C.navy }}>Papan Antrian</h1>
          <p className="mt-1 text-sm" style={{ color: C.grey }}>Pantau antrian pasien (auto-refresh 15 detik)</p>
        </div>
        <button onClick={muat} disabled={loading} className="inline-flex items-center gap-2 rounded-full px-4 py-2.5 text-sm font-semibold transition-opacity hover:opacity-80" style={{ backgroundColor: C.cream, color: C.navy }}>
          <RefreshCw size={16} className={loading ? "animate-spin" : ""} /> Refresh
        </button>
      </div>

      {/* Statistik ringkas */}
      <div className="mt-4 grid grid-cols-3 gap-3">
        {[
          { label: "Menunggu", value: jumlahMenunggu, color: C.orange, bg: C.orangeSoft },
          { label: "Diperiksa", value: jumlahDiperiksa, color: C.navy, bg: C.blueSoft },
          { label: "Selesai", value: jumlahSelesai, color: "#1B8A5A", bg: "#E0F2E9" },
        ].map((s) => (
          <div key={s.label} className="rounded-2xl border p-4 text-center" style={{ backgroundColor: C.white, borderColor: C.line }}>
            <p className="text-2xl font-bold" style={{ color: s.color }}>{s.value}</p>
            <p className="text-xs" style={{ color: C.grey }}>{s.label}</p>
          </div>
        ))}
      </div>

      {/* Filter */}
      <div className="mt-4 flex flex-wrap gap-2 items-center">
        <select value={poliFilter} onChange={e=>setPoliFilter(e.target.value)} className="rounded-full px-3 py-1.5 text-xs font-semibold border bg-white" style={{borderColor:C.line,color:C.navy}}>
          <option value="">🏥 Semua Poli</option>
          {poliNames.map(p=><option key={p} value={p}>{p}</option>)}
        </select>
        <input type="date" value={tglFilter} onChange={e=>setTglFilter(e.target.value)} className="rounded-full px-3 py-1.5 text-xs font-semibold border bg-white" style={{borderColor:C.line,color:C.navy}}/>
        {(poliFilter||tglFilter)&&<button onClick={()=>{setPoliFilter("");setTglFilter("");}} className="text-xs font-semibold" style={{color:C.orange}}>✕ Reset</button>}
        {["", "MENUNGGU", "DIPERIKSA", "SELESAI", "BATAL"].map((f) => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            className="rounded-full px-4 py-1.5 text-xs font-semibold transition-colors"
            style={{ backgroundColor: filter === f ? C.navy : C.cream, color: filter === f ? C.white : C.grey }}
          >
            {f === "" ? "Semua" : STATUS_LABEL[f]}
          </button>
        ))}
      </div>

      {error && (
        <div className="mt-4 flex items-start gap-2 rounded-2xl px-4 py-3" style={{ backgroundColor: "#FCE8E6" }}>
          <AlertTriangle size={18} color="#C0392B" className="mt-0.5 flex-shrink-0" />
          <p className="text-sm font-medium" style={{ color: "#C0392B" }}>{error}</p>
        </div>
      )}

      {/* Tabel antrian */}
      <div className="mt-6 overflow-x-auto rounded-3xl border" style={{ backgroundColor: C.white, borderColor: C.line }}>
        <table className="w-full text-left text-sm">
          <thead>
            <tr style={{ backgroundColor: C.cream, borderBottom: `1px solid ${C.line}` }}>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>No. Antrian</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Pasien</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Dokter</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Tanggal</th>
              <th className="px-5 py-3 font-semibold" style={{ color: C.navy }}>Status</th>
            </tr>
          </thead>
          <tbody>
            {Object.keys(clusters).length === 0 ? (
              <tr><td colSpan={5} className="px-5 py-10 text-center" style={{ color: C.grey }}>
                <Users size={30} color={C.grey} className="mx-auto mb-2" /> Tidak ada antrian.
              </td></tr>
            ) : (
              Object.entries(clusters).map(([label, items]) => (
                <>
                  <tr key={label} style={{ backgroundColor: C.cream }}>
                    <td colSpan={5} className="px-4 py-2 font-bold text-sm" style={{ color: C.navy }}>
                      📋 {label} — {items.length} antrian
                    </td>
                  </tr>
                  {items.map((a) => {
                    const st = STATUS_STYLE[a.status] ?? { bg: C.line, fg: C.grey };
                    return (
                      <tr key={a.id} style={{ borderBottom: `1px solid ${C.line}` }}>
                        <td className="px-5 py-4">
                          <div className="flex items-center gap-2">
                            <div className="flex h-8 w-8 items-center justify-center rounded-lg" style={{ backgroundColor: C.navy }}>
                              <Ticket size={16} color={C.white} />
                            </div>
                            <span className="text-lg font-bold" style={{ color: C.navy }}>{a.nomorUrut}</span>
                          </div>
                        </td>
                        <td className="px-5 py-4 font-semibold" style={{ color: C.navy }}>{a.namaPasien}</td>
                        <td className="px-5 py-4" style={{ color: C.grey }}>{a.namaDokter}</td>
                        <td className="px-5 py-4" style={{ color: C.grey }}>{formatTanggal(a.tanggal)}</td>
                        <td className="px-5 py-4">
                          <span className="rounded-full px-2.5 py-1 text-xs font-semibold" style={{ backgroundColor: st.bg, color: st.fg }}>
                            {STATUS_LABEL[a.status] ?? a.status}
                          </span>
                        </td>
                      </tr>
                    );
                  })}
                </>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

/**
 * Memformat tanggal "yyyy-MM-dd" menjadi teks Indonesia.
 */
function formatTanggal(iso: string): string {
  const [y, m, d] = iso.split("-").map(Number);
  if (!y || !m || !d) return iso;
  const bulan = ["Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"];
  return `${d} ${bulan[m - 1]} ${y}`;
}
