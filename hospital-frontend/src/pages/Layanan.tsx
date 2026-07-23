import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, ChevronRight, Stethoscope, CalendarDays } from "lucide-react";
import { C } from "../core/theme";
import { useAuth } from "../core/context/AuthContext";
import api from "../core/api/client";

interface Poli { id: number; nama: string; deskripsi: string | null; isActive: boolean }

export default function Layanan() {
  const { isAuthenticated } = useAuth();
  const [poliList, setPoliList] = useState<Poli[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let aktif = true;
    api.get<Poli[]>("/poli")
      .then(res => aktif && setPoliList(res.data.filter(p => p.isActive)))
      .catch(() => {})
      .finally(() => aktif && setLoading(false));
    return () => { aktif = false; };
  }, []);

  const tujuan = (unggulan: boolean) => unggulan
    ? (isAuthenticated ? "/osteoporosis/unggah" : "/osteoporosis")
    : (isAuthenticated ? "/booking" : "/login");

  return (
    <div>
      <section style={{ backgroundColor: C.navy }} className="relative overflow-hidden">
        <div className="pointer-events-none absolute -right-20 -top-16 h-64 w-64 rounded-full opacity-15" style={{ backgroundColor: C.blue }} />
        <div className="relative mx-auto max-w-6xl px-5 py-14 md:py-16">
          <span className="inline-flex items-center gap-2 rounded-full px-3.5 py-1.5 text-xs font-semibold" style={{ backgroundColor: "rgba(255,255,255,0.12)", color: C.blue }}>
            <Stethoscope size={14} /> Layanan Kami
          </span>
          <h1 className="mt-5 text-4xl font-bold leading-tight text-white md:text-5xl">Layanan kesehatan terintegrasi</h1>
          <p className="mt-4 max-w-2xl text-base leading-relaxed" style={{ color: "#C9D2EC" }}>RS Medika Sentosa menyediakan berbagai layanan medis untuk seluruh kebutuhan keluarga, didukung teknologi AI dan tenaga medis berpengalaman.</p>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-5 py-14">
        <nav className="mb-8 flex items-center gap-1.5 text-sm" aria-label="Breadcrumb">
          <Link to="/" className="transition-opacity hover:opacity-70" style={{ color: C.grey }}>Beranda</Link>
          <ChevronRight size={15} color={C.grey} />
          <span className="font-semibold" style={{ color: C.navy }}>Layanan</span>
        </nav>

        {loading ? (
          <div className="grid gap-6 md:grid-cols-2">
            {[1,2,3,4].map(i => <div key={i} className="rounded-3xl border p-6 animate-pulse" style={{ backgroundColor: C.white, borderColor: C.line }}><div className="h-12 w-12 rounded-2xl" style={{ backgroundColor: C.cream }} /><div className="mt-4 h-5 w-2/3 rounded" style={{ backgroundColor: C.cream }} /><div className="mt-2 h-4 w-full rounded" style={{ backgroundColor: C.cream }} /></div>)}
          </div>
        ) : poliList.length === 0 ? (
          <div className="py-16 text-center"><p className="text-sm" style={{ color: C.grey }}>Belum ada poli terdaftar. Admin dapat menambahkan poli melalui panel admin.</p></div>
        ) : (
          <div className="grid gap-6 md:grid-cols-2">
            {poliList.map((p, i) => (
              <article key={p.id} className="flex flex-col rounded-3xl border p-6 transition-all hover:-translate-y-1 hover:shadow-lg"
                style={{ backgroundColor: i === 0 ? C.navy : C.white, borderColor: i === 0 ? C.navy : C.line }}>
                <div className="flex items-center gap-3">
                  <div className="flex h-12 w-12 items-center justify-center rounded-2xl" style={{ backgroundColor: i === 0 ? "rgba(255,255,255,0.12)" : C.blueSoft }}>
                    <Stethoscope size={24} color={i === 0 ? C.blue : C.navy} />
                  </div>
                  <h2 className="text-xl font-bold" style={{ color: i === 0 ? C.white : C.navy }}>{p.nama}</h2>
                </div>
                <p className="mt-4 flex-1 text-sm leading-relaxed" style={{ color: i === 0 ? "#C9D2EC" : C.grey }}>
                  {p.deskripsi || ""}
                </p>
                <Link to={tujuan(i === 0)} className="mt-5 inline-flex items-center gap-1.5 text-sm font-semibold" style={{ color: i === 0 ? C.orange : C.navy }}>
                  {i === 0 ? "Coba Skrining AI" : "Buat Janji Temu"} <ArrowRight size={15} />
                </Link>
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="mx-auto max-w-6xl px-5 pb-16">
        <div className="flex flex-col items-center gap-4 rounded-3xl px-6 py-12 text-center md:flex-row md:justify-between md:text-left" style={{ backgroundColor: C.cream }}>
          <div className="flex items-center gap-4">
            <div className="flex h-14 w-14 flex-shrink-0 items-center justify-center rounded-full" style={{ backgroundColor: C.orange }}>
              <CalendarDays size={26} color={C.white} />
            </div>
            <div>
              <h2 className="text-xl font-bold" style={{ color: C.navy }}>Butuh konsultasi dokter?</h2>
              <p className="mt-1 text-sm" style={{ color: C.grey }}>Buat janji temu online dengan dokter pilihan Anda.</p>
            </div>
          </div>
          <Link to={isAuthenticated ? "/booking" : "/login"} className="inline-flex items-center gap-2 rounded-full px-7 py-3.5 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5" style={{ backgroundColor: C.orange }}>
            Buat Janji Temu <ArrowRight size={17} />
          </Link>
        </div>
      </section>
    </div>
  );
}
