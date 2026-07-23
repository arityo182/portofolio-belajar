import { useEffect, useRef, useState } from "react";
import { ChevronLeft, ChevronRight, Stethoscope, Star } from "lucide-react";
import { C } from "../../core/theme";
import api from "../../core/api/client";

interface Dokter { id: number; nama: string; spesialisasi: string; namaPoli: string }

export default function Doctors() {
  const [dokterList, setDokterList] = useState<Dokter[]>([]);
  const [loading, setLoading] = useState(true);
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    let aktif = true;
    api.get<Dokter[]>("/dokter")
      .then(res => aktif && setDokterList(res.data))
      .catch(() => {})
      .finally(() => aktif && setLoading(false));
    return () => { aktif = false; };
  }, []);

  const scroll = (dir: "left" | "right") => {
    if (scrollRef.current) {
      const w = scrollRef.current.children[0]?.clientWidth ?? 280;
      scrollRef.current.scrollBy({ left: dir === "left" ? -w - 20 : w + 20, behavior: "smooth" });
    }
  };

  const colors = [C.blueSoft, C.orangeSoft, C.blueSoft, C.orangeSoft];

  return (
    <section className="mx-auto max-w-6xl px-5 py-16">
      <div className="mb-10 flex items-end justify-between">
        <div>
          <p className="text-sm font-semibold" style={{ color: C.orange }}>Tim Medis</p>
          <h2 className="mt-1 text-3xl font-bold" style={{ color: C.navy }}>Dokter berpengalaman & tepercaya</h2>
        </div>
        {dokterList.length > 4 && (
          <div className="hidden gap-2 md:flex">
            <button onClick={() => scroll("left")} className="flex h-10 w-10 items-center justify-center rounded-full border" style={{ borderColor: C.line }}><ChevronLeft size={20} color={C.navy} /></button>
            <button onClick={() => scroll("right")} className="flex h-10 w-10 items-center justify-center rounded-full border" style={{ borderColor: C.line }}><ChevronRight size={20} color={C.navy} /></button>
          </div>
        )}
      </div>

      {loading ? (
        <div className="flex gap-5 overflow-hidden">
          {[1,2,3,4].map(i => <div key={i} className="min-w-[260px] rounded-3xl border p-5 text-center animate-pulse" style={{ backgroundColor: C.white, borderColor: C.line }}><div className="mx-auto h-20 w-20 rounded-full" style={{ backgroundColor: C.cream }} /><div className="mt-4 mx-auto h-5 w-2/3 rounded" style={{ backgroundColor: C.cream }} /><div className="mt-2 mx-auto h-4 w-1/2 rounded" style={{ backgroundColor: C.cream }} /></div>)}
        </div>
      ) : dokterList.length === 0 ? (
        <div className="py-10 text-center"><p className="text-sm" style={{ color: C.grey }}>Belum ada dokter terdaftar.</p></div>
      ) : (
        <div ref={scrollRef} className="flex gap-5 overflow-x-auto pb-4 scroll-smooth snap-x snap-mandatory" style={{ scrollbarWidth: "none", msOverflowStyle: "none" }}>
          {dokterList.map((d, i) => (
            <div key={d.id} className="min-w-[260px] max-w-[280px] snap-start rounded-3xl border p-5 text-center transition-all hover:-translate-y-1 hover:shadow-lg flex-shrink-0" style={{ backgroundColor: C.white, borderColor: C.line }}>
              <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-full" style={{ backgroundColor: colors[i % 4] }}>
                <Stethoscope size={32} color={C.navy} />
              </div>
              <h3 className="mt-4 text-base font-bold" style={{ color: C.navy }}>{d.nama}</h3>
              <p className="text-sm" style={{ color: C.orange }}>{d.spesialisasi}</p>
              <p className="text-xs" style={{ color: C.grey }}>{d.namaPoli}</p>
              <div className="mt-2 flex items-center justify-center gap-0.5">
                {Array.from({ length: 5 }).map((_, j) => <Star key={j} size={13} color={C.orange} fill={C.orange} />)}
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
