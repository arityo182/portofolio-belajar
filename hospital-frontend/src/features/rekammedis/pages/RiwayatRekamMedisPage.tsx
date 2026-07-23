import { useCallback, useEffect, useState } from "react";
import { AxiosError } from "axios";
import { ClipboardList, Loader2, AlertTriangle } from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import { useAuth } from "../../../core/context/AuthContext";
import { getPasienByUserId } from "../../pasien/services/pasienService";
import { getRekamMedisByPasien } from "../services/rekamMedisService";
import type { RekamMedis } from "../types";
import RekamMedisCard from "../components/RekamMedisCard";

/**
 * Halaman "Riwayat Rekam Medis" pasien (FR-15).
 *
 * Menampilkan seluruh rekam medis milik pasien yang sedang login, terurut
 * dari yang terbaru. Setiap rekam medis menampilkan dokter, tanggal,
 * diagnosa, tindakan, catatan, dan daftar resep obat terkait (dimuat lazy
 * per kartu). `pasienId` diperoleh dengan menerjemahkan `user.id` (dari
 * context auth) melalui `GET /api/pasien/user/{userId}`.
 *
 * @returns Halaman riwayat rekam medis pasien
 */
export default function RiwayatRekamMedisPage() {
  const { user } = useAuth();

  const [riwayat, setRiwayat] = useState<RekamMedis[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  /** true bila user login belum memiliki profil pasien (404). */
  const [belumTerdaftar, setBelumTerdaftar] = useState(false);

  const muat = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    setError(null);
    setBelumTerdaftar(false);
    try {
      const pasien = await getPasienByUserId(user.id);
      const res = await getRekamMedisByPasien(pasien.data.id);
      // Urutkan terbaru di atas (createdAt desc), fallback ke tanggal.
      const sorted = [...res.data].sort((a, b) =>
        b.createdAt.localeCompare(a.createdAt) || b.tanggal.localeCompare(a.tanggal)
      );
      setRiwayat(sorted);
    } catch (err) {
      if (err instanceof AxiosError && err.response?.status === 404) {
        setBelumTerdaftar(true);
      } else {
        setError(toErrorMessage(err));
      }
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    muat();
  }, [muat]);

  return (
    <div>
      {/* Hero */}
      <section style={{ backgroundColor: C.navy }} className="relative overflow-hidden">
        <div
          className="pointer-events-none absolute -right-20 -top-16 h-64 w-64 rounded-full opacity-15"
          style={{ backgroundColor: C.blue }}
        />
        <div className="relative mx-auto max-w-5xl px-5 py-14 md:py-16">
          <span
            className="inline-flex items-center gap-2 rounded-full px-3.5 py-1.5 text-xs font-semibold"
            style={{ backgroundColor: "rgba(255,255,255,0.12)", color: C.blue }}
          >
            <ClipboardList size={14} /> Riwayat Rekam Medis
          </span>
          <h1 className="mt-5 text-4xl font-bold leading-tight text-white md:text-5xl">
            Riwayat pemeriksaan Anda
          </h1>
          <p className="mt-4 max-w-xl text-base leading-relaxed" style={{ color: "#C9D2EC" }}>
            Tinjau seluruh rekam medis beserta diagnosa, tindakan, dan resep obat
            dari setiap kunjungan Anda.
          </p>
        </div>
      </section>

      <section className="mx-auto max-w-5xl px-5 py-12">
        {loading ? (
          <div className="flex flex-col items-center gap-3 py-16">
            <Loader2 className="animate-spin" size={28} color={C.navy} />
            <p className="text-sm" style={{ color: C.grey }}>Memuat riwayat rekam medis...</p>
          </div>
        ) : belumTerdaftar ? (
          <div
            className="flex flex-col items-center gap-3 rounded-3xl border border-dashed px-6 py-16 text-center"
            style={{ borderColor: C.line }}
          >
            <AlertTriangle size={30} color={C.orange} />
            <p className="text-base font-semibold" style={{ color: C.navy }}>
              Profil pasien belum lengkap
            </p>
            <p className="max-w-md text-sm" style={{ color: C.grey }}>
              Anda belum memiliki profil pasien. Riwayat rekam medis akan tersedia
              setelah profil dilengkapi dan Anda menjalani pemeriksaan.
            </p>
          </div>
        ) : error ? (
          <div
            className="flex flex-col items-center gap-3 rounded-3xl border border-dashed px-6 py-16 text-center"
            style={{ borderColor: C.line }}
          >
            <AlertTriangle size={30} color={C.orange} />
            <p className="text-sm" style={{ color: C.grey }}>{error}</p>
            <button
              type="button"
              onClick={muat}
              className="mt-1 rounded-full px-5 py-2 text-sm font-semibold text-white"
              style={{ backgroundColor: C.navy }}
            >
              Coba lagi
            </button>
          </div>
        ) : riwayat.length === 0 ? (
          <div
            className="flex flex-col items-center gap-3 rounded-3xl border border-dashed px-6 py-16 text-center"
            style={{ borderColor: C.line }}
          >
            <ClipboardList size={30} color={C.grey} />
            <p className="text-base font-semibold" style={{ color: C.navy }}>
              Belum ada riwayat rekam medis
            </p>
            <p className="text-sm" style={{ color: C.grey }}>
              Riwayat pemeriksaan Anda akan muncul di sini setelah dokter
              menyelesaikan pemeriksaan.
            </p>
          </div>
        ) : (
          <div className="grid gap-4">
            {riwayat.map((rm) => (
              <RekamMedisCard key={rm.id} rekamMedis={rm} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}