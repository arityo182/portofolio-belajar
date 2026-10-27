import { C } from "../../../core/theme";
/** Konfigurasi warna & label per status appointment. */
const STATUS_STYLE = {
    MENUNGGU: { label: "Menunggu", bg: C.orangeSoft, color: C.orange },
    DIPERIKSA: { label: "Diperiksa", bg: C.blueSoft, color: C.navy },
    SELESAI: { label: "Selesai", bg: "#E4F7EC", color: "#1B8A5A" },
    BATAL: { label: "Batal", bg: "#FCE8E6", color: "#C0392B" },
};
/**
 * Badge status appointment dengan warna kontekstual.
 *
 * @param props - lihat {@link StatusBadgeProps}
 * @returns Elemen badge berlabel status
 *
 * @example
 * <StatusBadge status="MENUNGGU" />
 */
export default function StatusBadge({ status }) {
    const s = STATUS_STYLE[status];
    return (<span className="inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold" style={{ backgroundColor: s.bg, color: s.color }}>
      {s.label}
    </span>);
}
