import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  HeartPulse, Target, Eye, Award, Users, ChevronRight, ArrowRight,
  Stethoscope, Microscope, ShieldCheck,
} from "lucide-react";
import { C, RS_NAME } from "../core/theme";
import api from "../core/api/client";

/** Item nilai (value) RS. */
interface Nilai {
  icon: typeof Target;
  judul: string;
  desc: string;
}

/** Item statistik RS. */
interface Statistik {
  angka: string;
  label: string;
}

/** Daftar nilai inti RS. */
const NILAI: Nilai[] = [
  { icon: ShieldCheck, judul: "Keamanan Pasien", desc: "Keselamatan pasien adalah prioritas utama dalam setiap layanan kami." },
  { icon: Award, judul: "Kualitas Terbaik", desc: "Pelayanan berkualitas tinggi dengan standar internasional." },
  { icon: Users, judul: "Tim Profesional", desc: "Dokter & perawat berpengalaman yang siap melayani Anda." },
  { icon: Microscope, judul: "Teknologi Modern", desc: "Didukung teknologi medis terkini termasuk AI untuk skrining." },
];

/** Statistik ringkas RS. */
const STATISTIK: Statistik[] = [
  { angka: "10+", label: "Tahun Pengalaman" },
  { angka: "50+", label: "Dokter Spesialis" },
  { angka: "1000+", label: "Pasien Puas" },
  { angka: "24/7", label: "Layanan IGD" },
];

/**
 * @module pages/TentangKami
 *
 * Halaman "Tentang Kami" — profil & informasi rumah sakit.
 *
 * Menampilkan: hero, visi & misi, nilai inti, statistik, dan CTA.
 *
 * @returns Halaman tentang kami
 */
export default function TentangKami() {
  const [c, setC] = useState<Record<string,string>>({});
  useEffect(()=>{api.get("/content/tentang").then(r=>setC(r.data)).catch(()=>{});},[]);

  return (
    <div>
      {/* Hero */}
      <section style={{ backgroundColor: C.navy }} className="relative overflow-hidden">
        <div
          className="pointer-events-none absolute -right-20 -top-16 h-64 w-64 rounded-full opacity-15"
          style={{ backgroundColor: C.blue }}
        />
        <div className="relative mx-auto max-w-6xl px-5 py-14 md:py-16">
          <span
            className="inline-flex items-center gap-2 rounded-full px-3.5 py-1.5 text-xs font-semibold"
            style={{ backgroundColor: "rgba(255,255,255,0.12)", color: C.blue }}
          >
            <HeartPulse size={14} /> Tentang Kami
          </span>
          <h1 className="mt-5 text-4xl font-bold leading-tight text-white md:text-5xl">
            {c.hero_title || "Mendampingi kesehatan keluarga Indonesia"}
          </h1>
          <p className="mt-4 max-w-2xl text-base leading-relaxed" style={{ color: "#C9D2EC" }}>
            {c.hero_subtitle || `${RS_NAME} adalah rumah sakit modern yang berkomitmen memberikan layanan kesehatan terintegrasi dengan dukungan teknologi AI dan tenaga medis berpengalaman.`}
          </p>
        </div>
      </section>

      {/* Breadcrumb */}
      <section className="mx-auto max-w-6xl px-5 py-6">
        <nav className="flex items-center gap-1.5 text-sm" aria-label="Breadcrumb">
          <Link to="/" className="transition-opacity hover:opacity-70" style={{ color: C.grey }}>
            Beranda
          </Link>
          <ChevronRight size={15} color={C.grey} />
          <span className="font-semibold" style={{ color: C.navy }}>Tentang Kami</span>
        </nav>
      </section>

      {/* Visi & Misi */}
      <section className="mx-auto max-w-6xl px-5 pb-14">
        <div className="grid gap-6 md:grid-cols-2">
          {/* Visi */}
          <div
            className="rounded-3xl border p-8"
            style={{ backgroundColor: C.white, borderColor: C.line }}
          >
            <div className="flex items-center gap-3">
              <div
                className="flex h-12 w-12 items-center justify-center rounded-2xl"
                style={{ backgroundColor: C.blueSoft }}
              >
                <Eye size={24} color={C.navy} />
              </div>
              <h2 className="text-xl font-bold" style={{ color: C.navy }}>Visi</h2>
            </div>
            <p className="mt-4 text-sm leading-relaxed" style={{ color: C.grey }}>
              Menjadi rumah sakit terdepan yang menyediakan layanan kesehatan
              berkualitas dengan pemanfaatan teknologi kecerdasan buatan untuk
              meningkatkan kesejahteraan masyarakat.
            </p>
          </div>

          {/* Misi */}
          <div
            className="rounded-3xl border p-8"
            style={{ backgroundColor: C.white, borderColor: C.line }}
          >
            <div className="flex items-center gap-3">
              <div
                className="flex h-12 w-12 items-center justify-center rounded-2xl"
                style={{ backgroundColor: C.orangeSoft }}
              >
                <Target size={24} color={C.orange} />
              </div>
              <h2 className="text-xl font-bold" style={{ color: C.navy }}>Misi</h2>
            </div>
            <ul className="mt-4 flex flex-col gap-2.5">
              {[
                "Memberikan pelayanan medis yang profesional dan manusiawi",
                "Memanfaatkan teknologi AI untuk diagnosis yang cepat & akurat",
                "Menyediakan layanan kesehatan terintegrasi untuk seluruh keluarga",
                "Mengutamakan keselamatan dan kepuasan pasien",
              ].map((m) => (
                <li key={m} className="flex items-start gap-2 text-sm leading-relaxed" style={{ color: C.grey }}>
                  <span className="mt-1.5 h-1.5 w-1.5 flex-shrink-0 rounded-full" style={{ backgroundColor: C.orange }} />
                  {m}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </section>

      {/* Nilai inti */}
      <section className="mx-auto max-w-6xl px-5 pb-14">
        <div className="mb-8 text-center">
          <p className="text-sm font-semibold" style={{ color: C.orange }}>Nilai Inti</p>
          <h2 className="mt-1 text-2xl font-bold" style={{ color: C.navy }}>Prinsip yang kami pegang</h2>
        </div>
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
          {NILAI.map(({ icon: Icon, judul, desc }) => (
            <div
              key={judul}
              className="rounded-3xl border p-6 transition-all hover:-translate-y-1 hover:shadow-md"
              style={{ backgroundColor: C.white, borderColor: C.line }}
            >
              <div
                className="flex h-12 w-12 items-center justify-center rounded-2xl"
                style={{ backgroundColor: C.blueSoft }}
              >
                <Icon size={24} color={C.navy} />
              </div>
              <h3 className="mt-4 text-base font-bold" style={{ color: C.navy }}>{judul}</h3>
              <p className="mt-2 text-sm leading-relaxed" style={{ color: C.grey }}>{desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Statistik */}
      <section style={{ backgroundColor: C.navy }} className="py-14">
        <div className="mx-auto max-w-6xl px-5">
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {STATISTIK.map((s) => (
              <div key={s.label} className="text-center">
                <p className="text-4xl font-bold" style={{ color: C.orange }}>{s.angka}</p>
                <p className="mt-1 text-sm" style={{ color: "#C9D2EC" }}>{s.label}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="mx-auto max-w-6xl px-5 py-14">
        <div
          className="flex flex-col items-center gap-4 rounded-3xl px-6 py-12 text-center md:flex-row md:justify-between md:text-left"
          style={{ backgroundColor: C.cream }}
        >
          <div className="flex items-center gap-4">
            <div
              className="flex h-14 w-14 flex-shrink-0 items-center justify-center rounded-full"
              style={{ backgroundColor: C.orange }}
            >
              <Stethoscope size={26} color={C.white} />
            </div>
            <div>
              <h2 className="text-xl font-bold" style={{ color: C.navy }}>
                Siap melayani kebutuhan kesehatan Anda
              </h2>
              <p className="mt-1 text-sm" style={{ color: C.grey }}>
                Buat janji temu dengan dokter pilihan Anda sekarang.
              </p>
            </div>
          </div>
          <Link
            to="/booking"
            className="inline-flex items-center gap-2 rounded-full px-7 py-3.5 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5"
            style={{ backgroundColor: C.orange }}
          >
            Buat Janji Temu <ArrowRight size={17} />
          </Link>
        </div>
      </section>
    </div>
  );
}