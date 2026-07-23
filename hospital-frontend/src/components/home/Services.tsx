import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ChevronRight, ArrowRight, Stethoscope } from "lucide-react";
import { C } from "../../core/theme";
import api from "../../core/api/client";

interface Poli { id: number; nama: string; deskripsi: string | null; isActive: boolean }

export default function Services() {
  const [poliList, setPoliList] = useState<Poli[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let aktif = true;
    api.get<Poli[]>("/poli")
      .then(res => aktif && setPoliList(res.data.filter(p => p.isActive)))
      .catch(() => {}) // silent fail — tampilan fallback di bawah
      .finally(() => aktif && setLoading(false));
    return () => { aktif = false; };
  }, []);

  if (loading) return (
    <section className="mx-auto max-w-6xl px-5 py-16">
      <div className="mb-10"><p className="text-sm font-semibold" style={{ color: C.orange }}>Layanan Kami</p><h2 className="mt-1 text-3xl font-bold" style={{ color: C.navy }}>Perawatan lengkap untuk keluarga</h2></div>
      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {[1,2,3,4,5,6].map(i => <div key={i} className="rounded-3xl border p-6 animate-pulse" style={{ backgroundColor: C.white, borderColor: C.line }}><div className="h-12 w-12 rounded-2xl" style={{ backgroundColor: C.cream }} /><div className="mt-4 h-5 w-2/3 rounded" style={{ backgroundColor: C.cream }} /><div className="mt-2 h-4 w-full rounded" style={{ backgroundColor: C.cream }} /></div>)}
      </div>
    </section>
  );

  return (
    <section className="mx-auto max-w-6xl px-5 py-16">
      <div className="mb-10 flex flex-col items-start justify-between gap-3 md:flex-row md:items-end">
        <div>
          <p className="text-sm font-semibold" style={{ color: C.orange }}>Layanan Kami</p>
          <h2 className="mt-1 text-3xl font-bold" style={{ color: C.navy }}>Perawatan lengkap untuk keluarga</h2>
        </div>
        <Link to="/layanan" className="flex items-center gap-1 text-sm font-semibold" style={{ color: C.navy }}>Lihat semua <ChevronRight size={16} /></Link>
      </div>
      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {poliList.length === 0 ? (
          <div className="col-span-full py-10 text-center"><p className="text-sm" style={{ color: C.grey }}>Belum ada poli terdaftar.</p></div>
        ) : (
          poliList.map((p, i) => (
            <div key={p.id} className="group rounded-3xl border p-6 transition-all hover:-translate-y-1 hover:shadow-lg" style={{ backgroundColor: i === 0 ? C.navy : C.white, borderColor: i === 0 ? C.navy : C.line }}>
              <div className="flex h-12 w-12 items-center justify-center rounded-2xl" style={{ backgroundColor: i === 0 ? "rgba(255,255,255,0.12)" : C.blueSoft }}>
                <Stethoscope size={24} color={i === 0 ? C.blue : C.navy} />
              </div>
              <h3 className="mt-4 text-lg font-bold" style={{ color: i === 0 ? C.white : C.navy }}>{p.nama}</h3>
              <p className="mt-2 text-sm leading-relaxed" style={{ color: i === 0 ? "#C9D2EC" : C.grey }}>{p.deskripsi || "Layanan pemeriksaan & konsultasi"}</p>
              <Link to={p.id === 1 ? "/jadwal-praktik" : "/jadwal-praktik"} className="mt-4 inline-flex items-center gap-1 text-sm font-semibold" style={{ color: i === 0 ? C.orange : C.navy }}>Selengkapnya <ArrowRight size={15} /></Link>
            </div>
          ))
        )}
      </div>
    </section>
  );
}
