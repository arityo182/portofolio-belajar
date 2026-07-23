import { Clock, Users, CalendarDays } from "lucide-react";
import { C } from "../../../core/theme";
import type { JadwalDokter, Hari } from "../types";

/**
 * Properti komponen {@link JadwalList}.
 */
interface JadwalListProps {
  /** Daftar jadwal praktik yang ditampilkan */
  jadwal: JadwalDokter[];
}

/** Label hari ramah-baca untuk setiap nilai enum {@link Hari}. */
const LABEL_HARI: Record<Hari, string> = {
  SENIN: "Senin",
  SELASA: "Selasa",
  RABU: "Rabu",
  KAMIS: "Kamis",
  JUMAT: "Jumat",
  SABTU: "Sabtu",
  MINGGU: "Minggu",
};

/**
 * Memangkas string waktu backend "HH:mm:ss" menjadi "HH:mm".
 *
 * @param waktu - string waktu dari backend, mis. "08:00:00"
 * @returns string "HH:mm", mis. "08:00"
 *
 * @example
 * formatJam("08:00:00"); // "08:00"
 */
function formatJam(waktu: string): string {
  return waktu.slice(0, 5);
}

/**
 * Daftar jadwal praktik seorang dokter dalam bentuk kartu-kartu.
 *
 * Setiap kartu menampilkan hari, rentang jam, kuota, dan badge status
 * aktif/nonaktif. Presentasional murni; state loading/error ditangani induk.
 *
 * @param props - lihat {@link JadwalListProps}
 * @returns Grid kartu jadwal
 *
 * @example
 * <JadwalList jadwal={data} />
 */
export default function JadwalList({ jadwal }: JadwalListProps) {
  return (
    <div className="grid gap-4 sm:grid-cols-2">
      {jadwal.map((j) => (
        <div
          key={j.id}
          className="rounded-3xl border p-5"
          style={{ backgroundColor: C.white, borderColor: C.line }}
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div
                className="flex h-10 w-10 items-center justify-center rounded-2xl"
                style={{ backgroundColor: C.blueSoft }}
              >
                <CalendarDays size={20} color={C.navy} />
              </div>
              <span className="text-lg font-bold" style={{ color: C.navy }}>
                {LABEL_HARI[j.hari]}
              </span>
            </div>
            <span
              className="rounded-full px-3 py-1 text-xs font-semibold"
              style={{
                backgroundColor: j.isActive ? C.blueSoft : C.line,
                color: j.isActive ? C.navy : C.grey,
              }}
            >
              {j.isActive ? "Aktif" : "Nonaktif"}
            </span>
          </div>

          <div className="mt-4 flex flex-wrap items-center gap-4">
            <div className="flex items-center gap-1.5">
              <Clock size={16} color={C.orange} />
              <span className="text-sm font-medium" style={{ color: C.grey }}>
                {formatJam(j.jamMulai)} &ndash; {formatJam(j.jamSelesai)}
              </span>
            </div>
            <div className="flex items-center gap-1.5">
              <Users size={16} color={C.orange} />
              <span className="text-sm font-medium" style={{ color: C.grey }}>
                Kuota {j.kuota} pasien
              </span>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
