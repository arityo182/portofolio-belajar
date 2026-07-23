import { Link } from "react-router-dom";
import { Bone, ArrowRight } from "lucide-react";
import { C } from "../../core/theme";
import { useAuth } from "../../core/context/AuthContext";

interface Step {
  n: string;
  t: string;
  d: string;
}

export default function OsteoHighlight() {
  const { isAuthenticated } = useAuth();
  const steps: Step[] = [
    { n: "1", t: "Unggah X-Ray", d: "Pasien atau dokter mengunggah citra X-ray tulang." },
    { n: "2", t: "Analisis AI", d: "Model EfficientNetV2-S menganalisis dalam hitungan detik." },
    { n: "3", t: "Hasil + Grad-CAM", d: "Lihat prediksi & area tulang yang jadi acuan." },
  ];

  return (
    <section style={{ backgroundColor: C.cream }}>
      <div className="mx-auto max-w-6xl px-5 py-16">
        <div className="overflow-hidden rounded-[2rem]" style={{ backgroundColor: C.navy }}>
          <div className="grid items-center gap-8 p-8 md:grid-cols-2 md:p-12">
            <div>
              <span
                className="inline-flex items-center gap-2 rounded-full px-3.5 py-1.5 text-xs font-semibold"
                style={{ backgroundColor: "rgba(255,255,255,0.12)", color: C.blue }}
              >
                <Bone size={14} /> Fitur Unggulan
              </span>
              <h2 className="mt-4 text-3xl font-bold leading-tight text-white">
                Skrining awal osteoporosis dengan AI
              </h2>
              <p className="mt-3 text-sm leading-relaxed" style={{ color: "#C9D2EC" }}>
                Teknologi deteksi dini membantu dokter menilai risiko osteoporosis
                lebih cepat dan objektif. Tetap dengan validasi tenaga medis.
              </p>
              <Link
                to={isAuthenticated ? "/osteoporosis/unggah" : "/login"}
                className="mt-6 inline-flex items-center gap-2 rounded-full px-6 py-3 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5"
                style={{ backgroundColor: C.orange }}
              >
                Coba Skrining <ArrowRight size={17} />
              </Link>
            </div>

            <div className="space-y-3">
              {steps.map((s) => (
                <div
                  key={s.n}
                  className="flex items-start gap-4 rounded-2xl p-4"
                  style={{ backgroundColor: "rgba(255,255,255,0.06)" }}
                >
                  <div
                    className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full text-sm font-bold"
                    style={{ backgroundColor: C.orange, color: C.white }}
                  >
                    {s.n}
                  </div>
                  <div>
                    <p className="text-sm font-bold text-white">{s.t}</p>
                    <p className="mt-0.5 text-xs" style={{ color: "#9FABCF" }}>{s.d}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
