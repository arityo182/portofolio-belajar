import { useState } from "react";
import { Link } from "react-router-dom";
import { ArrowLeft, Loader2, ScanLine, ListChecks } from "lucide-react";
import axios from "axios";
import { C } from "../../../core/theme";
import type { ScreeningResult } from "../types";
import { uploadScreening } from "../services/screeningService";
import UploadZone from "../components/UploadZone";
import ResultCard from "../components/ResultCard";

/** Status alur skrining: menganggur, sedang menganalisis, atau selesai. */
type Status = "idle" | "loading" | "done";

/**
 * Halaman skrining osteoporosis (unggah citra X-ray).
 *
 * Mengelola seluruh alur: memilih & memvalidasi file (tipe JPG/PNG,
 * maks 10MB), menampilkan pratinjau via {@link UploadZone}, memanggil
 * {@link uploadScreening}, lalu menampilkan hasil melalui {@link ResultCard}.
 * Menangani error jaringan/otorisasi (mis. 401 sesi berakhir).
 *
 * @returns Halaman unggah dan hasil skrining
 *
 * @example
 * <Route path="/osteoporosis/unggah" element={<OsteoScreening />} />
 */
export default function OsteoScreening() {
  const [file, setFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<string | null>(null);
  const [status, setStatus] = useState<Status>("idle");
  const [result, setResult] = useState<ScreeningResult | null>(null);
  const [error, setError] = useState("");

  const handleFile = (f: File) => {
    setError("");
    const okTypes = ["image/jpeg", "image/png"];
    if (!okTypes.includes(f.type)) {
      setError("Format harus JPG atau PNG.");
      return;
    }
    if (f.size > 10 * 1024 * 1024) {
      setError("Ukuran file maksimal 10MB.");
      return;
    }
    setFile(f);
    setPreview(URL.createObjectURL(f));
    setStatus("idle");
    setResult(null);
  };

  const handleClear = () => {
    setFile(null);
    setPreview(null);
    setStatus("idle");
    setResult(null);
    setError("");
  };

  const handleAnalyze = async () => {
    if (!file) return;
    setStatus("loading");
    setError("");

    try {
      // ── Panggilan ASLI ke backend ──
      const res = await uploadScreening(file);
      setResult(res.data);
      setStatus("done");
    } catch (err) {
      setStatus("idle");
      if (axios.isAxiosError(err)) {
        if (err.response?.status === 401) {
          setError("Sesi berakhir. Silakan login ulang.");
        } else {
          setError(
            err.response?.data?.message ??
              "Gagal menganalisis. Pastikan server aktif & coba lagi."
          );
        }
      } else {
        setError("Terjadi kesalahan. Coba lagi.");
      }
    }
  };

  return (
    <div style={{ backgroundColor: C.cream }} className="min-h-screen">
      <div className="mx-auto max-w-5xl px-5 py-10">
        <Link
          to="/osteoporosis"
          className="inline-flex items-center gap-1.5 text-sm font-medium"
          style={{ color: C.grey }}
        >
          <ArrowLeft size={16} /> Kembali ke layanan
        </Link>

        <div className="mt-4 mb-8">
          <h1 className="text-3xl font-bold" style={{ color: C.navy }}>
            Unggah Citra X-Ray
          </h1>
          <p className="mt-1 text-sm" style={{ color: C.grey }}>
            Sistem akan menganalisis citra untuk skrining awal osteoporosis.
          </p>
        </div>

        <div className="grid gap-6 lg:grid-cols-2">
          <div>
            <UploadZone
              preview={preview}
              onFile={handleFile}
              onClear={handleClear}
              error={error}
            />

            {preview && status !== "done" && (
              <button
                onClick={handleAnalyze}
                disabled={status === "loading"}
                className="mt-4 flex w-full items-center justify-center gap-2 rounded-full py-3.5 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5 disabled:opacity-70"
                style={{ backgroundColor: status === "loading" ? C.grey : C.orange }}
              >
                {status === "loading" ? (
                  <>
                    <Loader2 size={18} className="animate-spin" /> Menganalisis...
                  </>
                ) : (
                  <>
                    <ScanLine size={18} /> Analisis Sekarang
                  </>
                )}
              </button>
            )}
          </div>

          <div>
            {status === "done" && result ? (
              <ResultCard result={result} onReset={handleClear} />
            ) : (
              <div
                className="rounded-3xl border p-6"
                style={{ backgroundColor: C.white, borderColor: C.line }}
              >
                <div className="flex items-center gap-2.5">
                  <ListChecks size={20} color={C.navy} />
                  <h3 className="text-base font-bold" style={{ color: C.navy }}>
                    Panduan Unggah
                  </h3>
                </div>
                <ul className="mt-4 space-y-3">
                  {[
                    "Gunakan citra X-ray tulang yang jelas dan tidak buram.",
                    "Pastikan seluruh area tulang terlihat dalam frame.",
                    "Format yang didukung: JPG dan PNG.",
                    "Ukuran file maksimal 10MB.",
                  ].map((tip, i) => (
                    <li key={i} className="flex items-start gap-2.5">
                      <span
                        className="mt-0.5 flex h-5 w-5 flex-shrink-0 items-center justify-center rounded-full text-xs font-bold"
                        style={{ backgroundColor: C.blueSoft, color: C.navy }}
                      >
                        {i + 1}
                      </span>
                      <span className="text-sm" style={{ color: C.grey }}>{tip}</span>
                    </li>
                  ))}
                </ul>

                <div
                  className="mt-5 rounded-xl px-3 py-2.5 text-xs leading-relaxed"
                  style={{ backgroundColor: C.cream, color: C.grey }}
                >
                  Hasil bersifat skrining awal dan perlu validasi dokter.
                  Proses analisis bisa memakan beberapa detik.
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
