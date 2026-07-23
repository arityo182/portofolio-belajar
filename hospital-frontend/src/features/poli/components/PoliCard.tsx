import { Building2, Check } from "lucide-react";
import { C } from "../../../core/theme";
import type { Poli } from "../types";

/**
 * Properti komponen {@link PoliCard}.
 */
interface PoliCardProps {
  /** Data poli yang ditampilkan */
  poli: Poli;
  /** Apakah kartu ini sedang terpilih */
  selected: boolean;
  /** Callback saat kartu diklik; menerima ID poli */
  onSelect: (poliId: number) => void;
}

/**
 * Kartu poli yang dapat dipilih pada alur "lihat jadwal praktik".
 *
 * Menyorot border/aksen oranye saat terpilih dan menampilkan ikon centang.
 * Murni presentasional — seluruh state pemilihan dikelola induk.
 *
 * @param props - lihat {@link PoliCardProps}
 * @returns Elemen kartu poli interaktif
 *
 * @example
 * <PoliCard poli={p} selected={p.id === selectedId} onSelect={setSelectedId} />
 */
export default function PoliCard({ poli, selected, onSelect }: PoliCardProps) {
  return (
    <button
      type="button"
      onClick={() => onSelect(poli.id)}
      className="flex w-full items-start gap-3 rounded-3xl border p-5 text-left transition-all hover:-translate-y-0.5 hover:shadow-md"
      style={{
        backgroundColor: selected ? C.orangeSoft : C.white,
        borderColor: selected ? C.orange : C.line,
      }}
      aria-pressed={selected}
    >
      <div
        className="flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-2xl"
        style={{ backgroundColor: selected ? C.orange : C.blueSoft }}
      >
        <Building2 size={22} color={selected ? C.white : C.navy} />
      </div>
      <div className="min-w-0 flex-1">
        <h3 className="truncate text-base font-bold" style={{ color: C.navy }}>
          {poli.nama}
        </h3>
        {poli.deskripsi && (
          <p className="mt-1 line-clamp-2 text-sm leading-relaxed" style={{ color: C.grey }}>
            {poli.deskripsi}
          </p>
        )}
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
