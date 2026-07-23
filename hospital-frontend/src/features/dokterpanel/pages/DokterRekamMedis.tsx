import { useCallback, useEffect, useState } from "react";
import { AxiosError } from "axios";
import {
  ClipboardList, Loader2, AlertTriangle, FileText, Activity, Pill, CalendarDays,
} from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import { useAuth } from "../../../core/context/AuthContext";
import {
  getDokterIdByUserId, getRekamMedisByDokter, getResepByRekamMedis,
} from "../services/dokterService";
import type { RekamMedis, Resep } from "../../rekammedis/types";

/**
 * Halaman riwayat rekam medis dokter — daftar rekam medis yang pernah
 * dibuat dokter yang sedang login, lengkap dengan resep per rekam medis.
 *
 * @returns Halaman riwayat rekam medis dokter
 */
export default function DokterRekamMedis() {
  const { user } = useAuth();
  const [dokterId, setDokterId] = useState<number | null>(null);
  const [riwayat, setRiwayat] = useState<RekamMedis[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const muat = useCallback(async () => {
    if (!user || dokterId === null) return;
    setLoading(true);
    setError(null);
    try {
      const res = await getRekamMedisByDokter(dokterId);
      setRiwayat(res.data);
    } catch (err) {
      setError(toErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [user, dokterId]);

  useEffect(() => {
    if (!user) return;
    let aktif = true;
    getDokterIdByUserId(user.id)
      .then((res) => aktif && setDokterId(res.data.id))
      .catch((err: unknown) => aktif && setError(toErrorMessage(err)));
    return () => { aktif = false; };
  }, [user]);

  useEffect(() => { if (dokterId !== null) muat(); }, [muat, dokterId]);

  if (loading && riwayat.length === 0) {
    return (
      <div className="flex flex-col items-center gap-3 py-20">
        <Loader2 className="animate-spin" size={28} color={C.navy} />
        <p className="text-sm" style={{ color: C.grey }}>Memuat riwayat rekam medis...</p>
      </div>
    );
  }

  return (
    <div>
      <h1 className="text-2xl font-bold" style={{ color: C.navy }}>Riwayat Rekam Medis</h1>
      <p className="mt-1 text-sm" style={{ color: C.grey }}>Rekam medis yang pernah Anda buat</p>

      {error && (
        <div className="mt-4 flex items-start gap-2 rounded-2xl px-4 py-3" style={{ backgroundColor: "#FCE8E6" }}>
          <AlertTriangle size={18} color="#C0392B" className="mt-0.5 flex-shrink-0" />
          <p className="text-sm font-medium" style={{ color: "#C0392B" }}>{error}</p>
        </div>
      )}

      <div className="mt-6 flex flex-col gap-4">
        {riwayat.length === 0 ? (
          <div className="flex flex-col items-center gap-3 rounded-3xl border border-dashed py-16" style={{ borderColor: C.line }}>
            <ClipboardList size={30} color={C.grey} />
            <p className="text-sm" style={{ color: C.grey }}>Belum ada rekam medis yang Anda buat.</p>
          </div>
        ) : (
          riwayat.map((rm) => (
            <RekamMedisDokterCard key={rm.id} rekamMedis={rm} />
          ))
        )}
      </div>
    </div>
  );
}

/** Kartu satu rekam medis dengan resep (lazy load). */
function RekamMedisDokterCard({ rekamMedis }: { rekamMedis: RekamMedis }) {
  const [resep, setResep] = useState<Resep[]>([]);
  const [resepLoading, setResepLoading] = useState(true);
  const [resepError, setResepError] = useState<string | null>(null);

  useEffect(() => {
    let aktif = true;
    setResepLoading(true);
    getResepByRekamMedis(rekamMedis.id)
      .then((res) => aktif && setResep(res.data))
      .catch((err) => {
        if (err instanceof AxiosError) {
          setResepError(toErrorMessage(err));
        } else {
          setResepError(toErrorMessage(err));
        }
      })
      .finally(() => aktif && setResepLoading(false));
    return () => { aktif = false; };
  }, [rekamMedis.id]);

  return (
    <div className="rounded-3xl border p-5" style={{ backgroundColor: C.white, borderColor: C.line }}>
      {/* Header */}
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h3 className="font-bold" style={{ color: C.navy }}>{rekamMedis.namaPasien}</h3>
          <p className="text-xs" style={{ color: C.grey }}>No. RM: {rekamMedis.noRekamMedis}</p>
          <div className="mt-1 flex items-center gap-1.5 text-xs" style={{ color: C.grey }}>
            <CalendarDays size={13} color={C.orange} /> {formatTanggal(rekamMedis.tanggal)}
          </div>
        </div>
      </div>

      {/* Diagnosa & Tindakan */}
      <div className="mt-4 grid gap-4 sm:grid-cols-2">
        <div>
          <div className="flex items-center gap-1.5">
            <FileText size={15} color={C.orange} />
            <span className="text-xs font-semibold uppercase tracking-wide" style={{ color: C.grey }}>Diagnosa</span>
          </div>
          <p className="mt-1 text-sm font-medium leading-relaxed" style={{ color: C.navy }}>{rekamMedis.diagnosa}</p>
        </div>
        <div>
          <div className="flex items-center gap-1.5">
            <Activity size={15} color={C.orange} />
            <span className="text-xs font-semibold uppercase tracking-wide" style={{ color: C.grey }}>Tindakan</span>
          </div>
          <p className="mt-1 text-sm font-medium leading-relaxed" style={{ color: C.navy }}>{rekamMedis.tindakan}</p>
        </div>
        {rekamMedis.catatan && (
          <div className="sm:col-span-2">
            <span className="text-xs font-semibold uppercase tracking-wide" style={{ color: C.grey }}>Catatan</span>
            <p className="mt-1 text-sm" style={{ color: C.grey }}>{rekamMedis.catatan}</p>
          </div>
        )}
      </div>

      {/* Resep */}
      <div className="mt-5">
        <div className="mb-2 flex items-center gap-2">
          <Pill size={16} color={C.orange} />
          <h4 className="text-sm font-bold" style={{ color: C.navy }}>Resep Obat</h4>
        </div>
        {resepLoading ? (
          <div className="flex items-center gap-2 py-2">
            <Loader2 className="animate-spin" size={16} color={C.navy} />
            <span className="text-sm" style={{ color: C.grey }}>Memuat resep...</span>
          </div>
        ) : resepError ? (
          <p className="py-2 text-sm" style={{ color: "#C0392B" }}>{resepError}</p>
        ) : resep.length === 0 ? (
          <p className="py-2 text-sm" style={{ color: C.grey }}>Tidak ada resep.</p>
        ) : (
          <ul className="flex flex-col gap-2">
            {resep.map((r) => (
              <li key={r.id} className="flex flex-wrap items-center gap-x-4 gap-y-1 rounded-2xl px-4 py-3" style={{ backgroundColor: C.cream }}>
                <span className="font-semibold" style={{ color: C.navy }}>{r.namaObat}</span>
                {r.dosis && <span className="text-sm" style={{ color: C.grey }}>{r.dosis}</span>}
                <span className="text-sm" style={{ color: C.grey }}>Jml: {r.jumlah}</span>
                {r.aturanPakai && <span className="text-sm" style={{ color: C.grey }}>{r.aturanPakai}</span>}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}

function formatTanggal(iso: string): string {
  const [y, m, d] = iso.split("-").map(Number);
  if (!y || !m || !d) return iso;
  const bulan = ["Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"];
  return `${d} ${bulan[m - 1]} ${y}`;
}
