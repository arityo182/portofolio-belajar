import { Link } from "react-router-dom";
import {
  Bone, Activity, ShieldCheck, ArrowRight, Upload, Microscope,
  ScanLine, AlertTriangle,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";
import { C } from "../../../core/theme";

/**
 * Item edukasi (fakta) tentang osteoporosis pada halaman Radiologi.
 */
interface Fact {
  /** Ikon Lucide yang mewakili fakta */
  icon: LucideIcon;
  /** Judul singkat fakta */
  t: string;
  /** Deskripsi/penjelasan fakta */
  d: string;
}

/**
 * Item langkah pada bagian "Cara Kerja" skrining.
 */
interface Step {
  /** Nomor urut langkah (ditampilkan sebagai badge) */
  n: string;
  /** Ikon Lucide yang mewakili langkah */
  icon: LucideIcon;
  /** Judul langkah */
  t: string;
  /** Deskripsi langkah */
  d: string;
}

/**
 * Halaman landing layanan Radiologi & Skrining Tulang.
 *
 * Menyajikan konten edukatif statis (fakta osteoporosis, langkah cara
 * kerja) dan CTA menuju halaman unggah X-ray. Murni presentasional,
 * tanpa props maupun pemanggilan API.
 *
 * @returns Halaman landing radiologi
 */
export default function Radiologi() {
  const facts: Fact[] = [
    {
      icon: Bone,
      t: "Apa itu osteoporosis?",
      d: "Kondisi tulang menjadi rapuh dan mudah patah karena kepadatan tulang menurun. Sering tanpa gejala hingga terjadi patah tulang.",
    },
    {
      icon: AlertTriangle,
      t: "Kenapa deteksi dini penting?",
      d: "Semakin awal terdeteksi, semakin besar peluang pencegahan. Skrining membantu menilai risiko sebelum gejala muncul.",
    },
    {
      icon: Microscope,
      t: "Peran AI di sini",
      d: "Model AI menganalisis citra X-ray untuk membantu dokter menilai risiko secara cepat dan objektif — bukan menggantikan dokter.",
    },
  ];

  const steps: Step[] = [
    { n: "1", icon: Upload, t: "Unggah X-Ray", d: "Pilih citra X-ray tulang dalam format JPG atau PNG." },
    { n: "2", icon: ScanLine, t: "Analisis AI", d: "Sistem memproses gambar dalam hitungan detik." },
    { n: "3", icon: ShieldCheck, t: "Lihat Hasil", d: "Prediksi, tingkat keyakinan, dan area tulang acuan." },
  ];

  return (
    <div>
      <section style={{ backgroundColor: C.navy }} className="relative overflow-hidden">
        <div
          className="pointer-events-none absolute -right-20 -top-16 h-64 w-64 rounded-full opacity-15"
          style={{ backgroundColor: C.blue }}
        />
        <div className="relative mx-auto max-w-6xl px-5 py-16 md:py-20">
          <span
            className="inline-flex items-center gap-2 rounded-full px-3.5 py-1.5 text-xs font-semibold"
            style={{ backgroundColor: "rgba(255,255,255,0.12)", color: C.blue }}
          >
            <Bone size={14} /> Radiologi &amp; Skrining Tulang
          </span>
          <h1 className="mt-5 max-w-2xl text-4xl font-bold leading-tight text-white md:text-5xl">
            Skrining awal osteoporosis dari citra X-ray
          </h1>
          <p className="mt-4 max-w-xl text-base leading-relaxed" style={{ color: "#C9D2EC" }}>
            Layanan radiologi dengan bantuan analisis AI untuk membantu deteksi
            dini risiko osteoporosis — cepat, objektif, dan tetap divalidasi dokter.
          </p>
          <Link
            to="/osteoporosis/unggah"
            className="mt-7 inline-flex items-center gap-2 rounded-full px-6 py-3 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5"
            style={{ backgroundColor: C.orange }}
          >
            <Upload size={18} /> Mulai Skrining
          </Link>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-5 py-16">
        <div className="grid gap-5 md:grid-cols-3">
          {facts.map(({ icon: Icon, t, d }) => (
            <div
              key={t}
              className="rounded-3xl border p-6"
              style={{ backgroundColor: C.white, borderColor: C.line }}
            >
              <div
                className="flex h-12 w-12 items-center justify-center rounded-2xl"
                style={{ backgroundColor: C.blueSoft }}
              >
                <Icon size={24} color={C.navy} />
              </div>
              <h3 className="mt-4 text-lg font-bold" style={{ color: C.navy }}>{t}</h3>
              <p className="mt-2 text-sm leading-relaxed" style={{ color: C.grey }}>{d}</p>
            </div>
          ))}
        </div>
      </section>

      <section style={{ backgroundColor: C.cream }}>
        <div className="mx-auto max-w-6xl px-5 py-16">
          <div className="mb-10 text-center">
            <p className="text-sm font-semibold" style={{ color: C.orange }}>Cara Kerja</p>
            <h2 className="mt-1 text-3xl font-bold" style={{ color: C.navy }}>
              Tiga langkah sederhana
            </h2>
          </div>
          <div className="grid gap-5 md:grid-cols-3">
            {steps.map(({ n, icon: Icon, t, d }) => (
              <div key={n} className="relative rounded-3xl p-6" style={{ backgroundColor: C.white }}>
                <div className="flex items-center gap-3">
                  <div
                    className="flex h-9 w-9 items-center justify-center rounded-full text-sm font-bold text-white"
                    style={{ backgroundColor: C.orange }}
                  >
                    {n}
                  </div>
                  <Icon size={22} color={C.navy} />
                </div>
                <h3 className="mt-4 text-lg font-bold" style={{ color: C.navy }}>{t}</h3>
                <p className="mt-2 text-sm leading-relaxed" style={{ color: C.grey }}>{d}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-5 py-16">
        <div
          className="flex flex-col items-center gap-6 rounded-[2rem] p-10 text-center md:flex-row md:text-left"
          style={{ backgroundColor: C.navy }}
        >
          <div
            className="flex h-16 w-16 flex-shrink-0 items-center justify-center rounded-full"
            style={{ backgroundColor: "rgba(255,255,255,0.12)" }}
          >
            <Activity size={30} color={C.blue} />
          </div>
          <div className="flex-1">
            <h2 className="text-2xl font-bold text-white">Siap melakukan skrining?</h2>
            <p className="mt-1 text-sm" style={{ color: "#C9D2EC" }}>
              Unggah citra X-ray dan dapatkan hasil analisis awal dalam hitungan detik.
            </p>
          </div>
          <Link
            to="/osteoporosis/unggah"
            className="flex items-center gap-2 rounded-full px-7 py-3 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5"
            style={{ backgroundColor: C.orange }}
          >
            Unggah X-Ray <ArrowRight size={17} />
          </Link>
        </div>
      </section>
    </div>
  );
}
