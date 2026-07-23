import { Clock, ShieldCheck, MapPin, HeartPulse } from "lucide-react";
import type { LucideIcon } from "lucide-react";
import { C } from "../../core/theme";

interface TrustItem {
  icon: LucideIcon;
  t: string;
}

export default function TrustBar() {
  const items: TrustItem[] = [
    { icon: Clock, t: "Pendaftaran 24 Jam" },
    { icon: ShieldCheck, t: "Dokter Bersertifikat" },
    { icon: MapPin, t: "Lokasi Strategis" },
    { icon: HeartPulse, t: "Gawat Darurat Cepat" },
  ];

  return (
    <section style={{ backgroundColor: C.cream }}>
      <div className="mx-auto grid max-w-6xl grid-cols-2 gap-4 px-5 py-6 md:grid-cols-4">
        {items.map(({ icon: Icon, t }) => (
          <div key={t} className="flex items-center gap-3">
            <div
              className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full"
              style={{ backgroundColor: C.white }}
            >
              <Icon size={18} color={C.navy} />
            </div>
            <span className="text-sm font-medium" style={{ color: C.grey }}>{t}</span>
          </div>
        ))}
      </div>
    </section>
  );
}
