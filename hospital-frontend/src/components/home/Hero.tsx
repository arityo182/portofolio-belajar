import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Activity, Calendar, MessageCircle, Bone, Microscope,
  ShieldCheck, Stethoscope,
} from "lucide-react";
import { C } from "../../core/theme";
import { useAuth } from "../../core/context/AuthContext";
import api from "../../core/api/client";

interface Stat {
  n: string;
  l: string;
}

export default function Hero() {
  const { isAuthenticated } = useAuth();
  const [c, setC] = useState<Record<string,string>>({});
  useEffect(()=>{api.get("/content/beranda").then(r=>setC(r.data)).catch(()=>{});},[]);

  const stats: Stat[] = [
    { n: "40+", l: "Dokter Spesialis" },
    { n: "12", l: "Poli Layanan" },
    { n: "24/7", l: "Layanan Darurat" },
  ];

  return (
    <section style={{ backgroundColor: C.navy }} className="relative overflow-hidden">
      <div
        className="pointer-events-none absolute -right-20 -top-20 h-72 w-72 rounded-full opacity-20"
        style={{ backgroundColor: C.blue }}
      />
      <div
        className="pointer-events-none absolute -bottom-24 left-1/4 h-64 w-64 rounded-full opacity-10"
        style={{ backgroundColor: C.orange }}
      />

      <div className="relative mx-auto grid max-w-6xl items-center gap-10 px-5 py-16 md:grid-cols-2 md:py-20">
        <div>
          <span
            className="inline-flex items-center gap-2 rounded-full px-3.5 py-1.5 text-xs font-semibold"
            style={{ backgroundColor: "rgba(255,255,255,0.12)", color: C.blue }}
          >
            <Activity size={14} /> {c.hero_badge || "Didukung skrining AI"}
          </span>

          <h1 className="mt-5 text-4xl font-bold leading-tight text-white md:text-5xl">
            {c.hero_title || "Kesehatan Anda,"}{" "}
            <span style={{ color: C.orange }}>{c.hero_title_highlight || "prioritas"} kami.</span>
          </h1>

          <p className="mt-4 max-w-md text-base leading-relaxed" style={{ color: "#C9D2EC" }}>
            {c.hero_subtitle || "Daftar online, konsultasi dengan dokter, dan dapatkan skrining awal osteoporosis dari citra X-ray — semua dalam satu portal."}
          </p>

          <div className="mt-7 flex flex-wrap gap-3">
            <Link
              to={isAuthenticated ? "/booking" : "/login"}
              className="flex items-center gap-2 rounded-full px-6 py-3 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5"
              style={{ backgroundColor: C.orange }}
            >
              <Calendar size={18} /> {isAuthenticated ? "Buat Janji" : "Daftar & Buat Janji"}
            </Link>
            <Link
              to={isAuthenticated ? "/osteoporosis/unggah" : "/osteoporosis"}
              className="flex items-center gap-2 rounded-full px-6 py-3 text-sm font-semibold transition-transform hover:-translate-y-0.5"
              style={{ backgroundColor: C.white, color: C.navy }}
            >
              <MessageCircle size={18} /> Skrining Tulang
            </Link>
          </div>

          <div className="mt-9 flex gap-8">
            {stats.map((s) => (
              <div key={s.l}>
                <p className="text-2xl font-bold text-white">{s.n}</p>
                <p className="text-xs" style={{ color: "#9FABCF" }}>{s.l}</p>
              </div>
            ))}
          </div>
        </div>

        <div className="relative mx-auto w-full max-w-sm">
          <div className="rounded-3xl p-5 shadow-2xl" style={{ backgroundColor: C.white }}>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <div
                  className="flex h-10 w-10 items-center justify-center rounded-full"
                  style={{ backgroundColor: C.blueSoft }}
                >
                  <Bone size={20} color={C.navy} />
                </div>
                <div>
                  <p className="text-sm font-bold" style={{ color: C.navy }}>
                    Skrining Osteoporosis
                  </p>
                  <p className="text-xs" style={{ color: C.grey }}>X-ray · Hari ini</p>
                </div>
              </div>
              <span
                className="rounded-full px-2.5 py-1 text-[10px] font-bold"
                style={{ backgroundColor: C.orangeSoft, color: C.orange }}
              >
                AI
              </span>
            </div>

            <div
              className="mt-4 flex h-32 items-center justify-center rounded-2xl"
              style={{ backgroundColor: C.navy }}
            >
              <Microscope size={40} color={C.blue} strokeWidth={1.5} />
            </div>

            <div className="mt-4 space-y-2.5">
              <div className="flex items-center justify-between">
                <span className="text-xs" style={{ color: C.grey }}>Tingkat keyakinan</span>
                <span className="text-xs font-bold" style={{ color: C.navy }}>90%</span>
              </div>
              <div className="h-2 w-full overflow-hidden rounded-full" style={{ backgroundColor: C.cream }}>
                <div className="h-full rounded-full" style={{ width: "90%", backgroundColor: C.orange }} />
              </div>
              <div
                className="flex items-center gap-2 rounded-xl px-3 py-2"
                style={{ backgroundColor: C.blueSoft }}
              >
                <ShieldCheck size={16} color={C.navy} />
                <span className="text-xs font-medium" style={{ color: C.navy }}>
                  Risiko rendah — pantau berkala
                </span>
              </div>
            </div>
          </div>

          <div
            className="absolute -bottom-6 -left-6 hidden rounded-2xl p-3.5 shadow-xl sm:block"
            style={{ backgroundColor: C.orange }}
          >
            <div className="flex items-center gap-2.5">
              <Stethoscope size={22} color={C.white} />
              <div>
                <p className="text-xs font-bold text-white">Booking Online</p>
                <p className="text-[10px] text-white opacity-90">24 jam · Tanpa antri</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
