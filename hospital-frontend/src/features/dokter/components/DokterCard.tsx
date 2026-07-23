import { Stethoscope, Check } from "lucide-react";
import { C } from "../../../core/theme";
import type { Dokter } from "../types";

/**
 * Properti komponen {@link DokterCard}.
 */
interface DokterCardProps {
  /** Data dokter yang ditampilkan */
  dokter: Dokter;
  /** Apakah kartu ini sedang terpilih */
  selected: boolean;
  /** Callback saat kartu diklik; menerima ID dokter */
  onSelect: (dokterId: number) => void;
}

/**
 * Kartu dokter yang dapat dipilih pada alur "lihat jadwal praktik".
 *
 * Menampilkan nama, spesialisasi, dan poli dokter; menyorot aksen oranye
 * saat terpilih. Murni presentasional.
 *
 * @param props - lihat {@link DokterCardProps}
 * @returns Elemen kartu dokter interaktif
 *
 * @example
 * <DokterCard dokter={d} selected={d.id === selectedId} onSelect={setSelectedId} />
 */
export default function DokterCard({ dokter, selected, onSelect }: DokterCardProps) {
  return (
    <button
      type="button"
      onClick={() => onSelect(dokter.id)}
      className="flex w-full items-center gap-3 rounded-3xl border p-5 text-left transition-all hover:-translate-y-0.5 hover:shadow-md"
      style={{
        backgroundColor: selected ? C.orangeSoft : C.white,
        borderColor: selected ? C.orange : C.line,
      }}
      aria-pressed={selected}
    >
      <div
        className="flex h-14 w-14 flex-shrink-0 items-center justify-center rounded-full"
        style={{ backgroundColor: selected ? C.orange : C.blueSoft }}
      >
        <Stethoscope size={26} color={selected ? C.white : C.navy} />
      </div>
      <div className="min-w-0 flex-1">
        <h3 className="truncate text-base font-bold" style={{ color: C.navy }}>
          {dokter.nama}
        </h3>
        <p className="text-sm font-medium" style={{ color: C.orange }}>
          {dokter.spesialisasi}
        </p>
        <p className="truncate text-xs" style={{ color: C.grey }}>
          {dokter.namaPoli}
        </p>
      </div>
      {selected && (
        <div
          className="flex h-6 w-6 flex-shrink-0 items-center justify-center rounded-full"
          style={{ backgroundColor: C.orange }}
        >
          <Check size={15} color={C.white} strokeWidth={3} />
        </div>
      )}
    </button>
  );
}
