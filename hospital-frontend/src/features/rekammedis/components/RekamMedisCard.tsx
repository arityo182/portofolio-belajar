import { useEffect, useState } from "react";
import {
  Stethoscope, CalendarDays, FileText, Activity, Pill, Loader2,
} from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import { getResepByRekamMedis } from "../services/resepService";
import type { RekamMedis, Resep } from "../types";

/**
 * Properti komponen {@link RekamMedisCard}.
 */
interface RekamMedisCardProps {
  /** Data rekam medis yang ditampilkan */
  rekamMedis: RekamMedis;
}

/**
 * Kartu satu rekam medis pada halaman riwayat pasien.
 *
 * Menampilkan dokter, tanggal, diagnosa, tindakan, catatan, dan daftar resep
 * obat terkait. Resep dimuat secara mandiri (lazy) per kartu lewat
 * `GET /api/resep/rekam-medis/{id}` agar daftar rekam medis tidak terblokir
 * saat memuat resep semua rekam medis sekaligus.
 *
 * @param props - lihat {@link RekamMedisCardProps}
 * @returns Elemen kartu rekam medis
 *
 * @example
 * <RekamMedisCard rekamMedis={rm} />
 */
export default function RekamMedisCard({ rekamMedis }: RekamMedisCardProps) {
  const [resep, setResep] = useState<Resep[]>([]);
  const [resepLoading, setResepLoading] = useState(true);
  const [resepError, setResepError] = useState<string | null>(null);

  // Muat resep obat terkait rekam medis ini saat mount.
  useEffect(() => {
    let aktif = true;
    setResepLoading(true);
    setResepError(null);
    getResepByRekamMedis(rekamMedis.id)
      .then((res) => aktif && setResep(res.data))
      .catch((err) => aktif && setResepError(toErrorMessage(err)))
      .finally(() => aktif && setResepLoading(false));
    return () => {
      aktif = false;
    };
  }, [rekamMedis.id]);

  return (
    <div
      className="rounded-3xl border p-5 md:p-6"
      style={{ backgroundColor: C.white, borderColor: C.line }}
    >
      {/* Header: dokter + tanggal */}
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="flex items-center gap-3">
          <div
            className="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full"
            style={{ backgroundColor: C.blueSoft }}
          >
            <Stethoscope size={24} color={C.navy} />
          </div>
          <div className="min-w-0">
            <h3 className="truncate text-base font-bold" style={{ color: C.navy }}>
              {rekamMedis.namaDokter}
            </h3>
            <div className="mt-0.5 flex items-center gap-1.5 text-sm" style={{ color: C.grey }}>
              <CalendarDays size={14} color={C.orange} />
              <span>{formatTanggal(rekamMedis.tanggal)}</span>
            </div>
          </div>
        </div>
        <span
          className="rounded-full px-3 py-1 text-xs font-semibold"
          style={{ backgroundColor: C.cream, color: C.navy }}
        >
          No. RM {rekamMedis.noRekamMedis}
        </span>
      </div>

      {/* Isi rekam medis */}
      <div className="mt-5 grid gap-4 sm:grid-cols-2">
        <InfoBlock
          icon={<FileText size={16} color={C.orange} />}
          label="Diagnosa"
          value={rekamMedis.diagnosa}
        />
        <InfoBlock
          icon={<Activity size={16} color={C.orange} />}
          label="Tindakan"
          value={rekamMedis.tindakan}
        />
        {rekamMedis.catatan && (
          <div className="sm:col-span-2">
            <InfoBlock
              icon={<FileText size={16} color={C.orange} />}
              label="Catatan"
              value={rekamMedis.catatan}
            />
          </div>
        )}
      </div>

      {/* Resep obat */}
      <div className="mt-5">
        <div className="mb-2 flex items-center gap-2">
          <Pill size={17} color={C.orange} />
          <h4 className="text-sm font-bold" style={{ color: C.navy }}>Resep Obat</h4>
        </div>

        {resepLoading ? (
          <div className="flex items-center gap-2 py-3">
            <Loader2 className="animate-spin" size={16} color={C.navy} />
            <span className="text-sm" style={{ color: C.grey }}>Memuat resep...</span>
          </div>
        ) : resepError ? (
          <p className="py-2 text-sm" style={{ color: "#C0392B" }}>{resepError}</p>
        ) : resep.length === 0 ? (
          <p className="py-2 text-sm" style={{ color: C.grey }}>
            Tidak ada resep pada rekam medis ini.
          </p>
        ) : (
          <ul className="flex flex-col gap-2">
            {resep.map((r) => (
              <li
                key={r.id}
                className="flex flex-wrap items-center gap-x-4 gap-y-1 rounded-2xl px-4 py-3"
                style={{ backgroundColor: C.cream }}
              >
                <span className="text-sm font-semibold" style={{ color: C.navy }}>
                  {r.namaObat}
                </span>
                <span className="text-sm" style={{ color: C.grey }}>{r.dosis}</span>
                <span className="text-sm" style={{ color: C.grey }}>
                  Jumlah: {r.jumlah}
                </span>
                <span className="text-sm" style={{ color: C.grey }}>
                  {r.aturanPakai}
                </span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}

/**
 * Blok informasi berlabel (ikon + label + nilai) pada kartu rekam medis.
 *
 * @param props.icon - ikon kecil
 * @param props.label - nama field
 * @param props.value - isi field
 */
function InfoBlock({
  icon, label, value,
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
}) {
  return (
    <div>
      <div className="flex items-center gap-1.5">
        {icon}
        <span className="text-xs font-semibold uppercase tracking-wide" style={{ color: C.grey }}>
          {label}
        </span>
      </div>
      <p className="mt-1 text-sm font-medium leading-relaxed" style={{ color: C.navy }}>
        {value || "-"}
      </p>
    </div>
  );
}

/**
 * Memformat tanggal "yyyy-MM-dd" menjadi teks Indonesia, mis. "13 Juli 2026".
 *
 * @param iso - tanggal format "yyyy-MM-dd"
 * @returns tanggal ramah-baca; mengembalikan input apa adanya bila tak valid
 */
function formatTanggal(iso: string): string {
  const [y, m, d] = iso.split("-").map(Number);
  if (!y || !m || !d) return iso;
  const bulan = [
    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
    "Juli", "Agustus", "September", "Oktober", "November", "Desember",
  ];
  return `${d} ${bulan[m - 1]} ${y}`;
}