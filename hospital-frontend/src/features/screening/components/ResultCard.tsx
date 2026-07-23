import { ShieldCheck, AlertTriangle, Info, RefreshCw, Timer } from "lucide-react";
import { C } from "../../../core/theme";
import type { ScreeningResult } from "../types";

/**
 * Props untuk komponen {@link ResultCard}.
 */
interface ResultCardProps {
  /** Hasil skrining dari backend yang akan divisualisasikan */
  result: ScreeningResult;
  /** Callback saat tombol "Skrining Lagi" ditekan (mereset alur skrining) */
  onReset: () => void;
}

/**
 * Memformat latensi inferensi ke teks yang mudah dibaca.
 *
 * @param ms - latensi dalam milidetik (opsional)
 * @returns "-" bila tak ada nilai, "{n} ms" untuk <1 detik, atau "{n} detik"
 */
function formatLatency(ms?: number): string {
  if (ms === undefined) return "-";
  if (ms < 1000) return `${Math.round(ms)} ms`;
  return `${(ms / 1000).toFixed(2)} detik`;
}

/**
 * Menentukan warna aksen berdasarkan label kelas prediksi.
 *
 * @param label - label kelas ("Normal" | "Osteopenia" | "Osteoporosis")
 * @returns kode warna hex: hijau (normal), oranye (osteopenia), merah (osteoporosis)
 */
// warna per kelas
function classColor(label: string): string {
  const l = label.toLowerCase();
  if (l === "normal") return "#1D9E75";       // hijau
  if (l === "osteopenia") return C.orange;    // oranye
  return "#D64545";                            // merah (osteoporosis)
}

/**
 * Kartu hasil skrining osteoporosis.
 *
 * Menampilkan label prediksi, tingkat keyakinan, distribusi probabilitas
 * per kelas (bar chart), visualisasi Grad-CAM, latensi, pesan risiko,
 * dan tombol untuk mengulang skrining.
 *
 * @param props - lihat {@link ResultCardProps}
 * @returns Elemen kartu hasil skrining
 *
 * @example
 * <ResultCard result={result} onReset={handleClear} />
 */
export default function ResultCard({ result, onReset }: ResultCardProps) {
  const accent = classColor(result.label);
  const isNormal = result.label.toLowerCase() === "normal";

  // urutan tampil probabilitas
  const order = ["Normal", "Osteopenia", "Osteoporosis"];
  const probs = result.probabilities ?? {};

  return (
    <div className="rounded-3xl border p-6" style={{ backgroundColor: C.white, borderColor: C.line }}>
      {/* Header hasil */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div
            className="flex h-11 w-11 items-center justify-center rounded-full"
            style={{ backgroundColor: `${accent}22` }}
          >
            {isNormal ? (
              <ShieldCheck size={22} color={accent} />
            ) : (
              <AlertTriangle size={22} color={accent} />
            )}
          </div>
          <div>
            <p className="text-xs" style={{ color: C.grey }}>Hasil Prediksi</p>
            <p className="text-xl font-bold" style={{ color: C.navy }}>{result.label}</p>
          </div>
        </div>
        <span
          className="rounded-full px-3 py-1 text-xs font-bold"
          style={{ backgroundColor: `${accent}22`, color: accent }}
        >
          {result.confidence}%
        </span>
      </div>

      {/* Bar chart 3 kelas */}
      {Object.keys(probs).length > 0 && (
        <div className="mt-5 space-y-2.5">
          <p className="text-sm font-semibold" style={{ color: C.navy }}>Distribusi Probabilitas</p>
          {order.map((cls) => {
            const val = probs[cls] ?? 0;
            return (
              <div key={cls}>
                <div className="flex items-center justify-between text-xs">
                  <span style={{ color: C.grey }}>{cls}</span>
                  <span className="font-semibold" style={{ color: C.navy }}>{val.toFixed(1)}%</span>
                </div>
                <div className="mt-1 h-2 w-full overflow-hidden rounded-full" style={{ backgroundColor: C.cream }}>
                  <div
                    className="h-full rounded-full transition-all"
                    style={{ width: `${val}%`, backgroundColor: classColor(cls) }}
                  />
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Grad-CAM */}
      <div className="mt-5">
        <p className="mb-2 text-sm font-semibold" style={{ color: C.navy }}>
          Visualisasi Grad-CAM
        </p>
        <div
          className="flex h-56 items-center justify-center overflow-hidden rounded-2xl"
          style={{ backgroundColor: C.navy }}
        >
          {result.gradcamImage ? (
            <img
              src={result.gradcamImage}
              alt="Grad-CAM heatmap"
              className="h-full w-full object-contain"
            />
          ) : (
            <span className="px-4 text-center text-xs" style={{ color: C.blue }}>
              Area tulang acuan model akan tampil di sini
            </span>
          )}
        </div>
        <p className="mt-1.5 text-xs" style={{ color: C.grey }}>
          Area berwarna menunjukkan bagian tulang yang menjadi dasar prediksi model.
        </p>
      </div>

      {/* Latensi */}
      <div
        className="mt-4 flex items-center justify-between rounded-xl px-3 py-2.5"
        style={{ backgroundColor: C.cream }}
      >
        <span className="flex items-center gap-1.5 text-sm" style={{ color: C.grey }}>
          <Timer size={15} /> Waktu prediksi
        </span>
        <span className="text-sm font-bold" style={{ color: C.navy }}>
          {formatLatency(result.latencyMs)}
        </span>
      </div>

      {/* Risk message */}
      {result.risk && (
        <div
          className="mt-3 flex items-start gap-2 rounded-xl px-3 py-2.5"
          style={{ backgroundColor: `${accent}18` }}
        >
          <Info size={16} color={accent} className="mt-0.5 flex-shrink-0" />
          <span className="text-sm font-medium" style={{ color: accent }}>{result.risk}</span>
        </div>
      )}

      {/* Disclaimer */}
      <p className="mt-4 text-xs leading-relaxed" style={{ color: C.grey }}>
        Hasil ini bersifat <strong>skrining awal</strong> dan tidak menggantikan
        diagnosis. Silakan konsultasikan dengan dokter untuk pemeriksaan lanjutan.
      </p>

      <button
        onClick={onReset}
        className="mt-5 flex w-full items-center justify-center gap-2 rounded-full py-3 text-sm font-semibold transition-colors"
        style={{ backgroundColor: C.cream, color: C.navy }}
      >
        <RefreshCw size={16} /> Skrining Lagi
      </button>
    </div>
  );
}
