import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getScreeningHistory } from "../services/screeningService";
import type { ScreeningResult } from "../types";
import { C } from "../../../core/theme";
import { Activity, ArrowRight, AlertTriangle, Upload } from "lucide-react";

/**
 * Halaman riwayat skrining osteoporosis user.
 */
export default function RiwayatSkrining() {
  const [data, setData] = useState<ScreeningResult[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getScreeningHistory()
      .then((res) => setData(res.data))
      .catch(() => setError("Gagal memuat riwayat skrining"))
      .finally(() => setLoading(false));
  }, []);

  return (
    <main className="mx-auto max-w-3xl px-5 py-10">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold" style={{ color: C.navy }}>
            Riwayat Skrining
          </h1>
          <p className="mt-1 text-sm" style={{ color: C.grey }}>
            Riwayat skrining osteoporosis Anda
          </p>
        </div>
        <Link
          to="/osteoporosis/unggah"
          className="inline-flex items-center gap-2 rounded-xl px-4 py-2.5 text-sm font-semibold text-white shadow transition hover:-translate-y-0.5"
          style={{ backgroundColor: C.orange }}
        >
          <Upload size={15} /> Skrining Baru
        </Link>
      </div>

      {loading ? (
        <div className="flex items-center gap-3 rounded-2xl border p-6" style={{ borderColor: C.line }}>
          <Activity size={20} className="animate-spin" color={C.navy} />
          <p className="text-sm" style={{ color: C.grey }}>Memuat riwayat skrining...</p>
        </div>
      ) : error ? (
        <div className="flex items-center gap-3 rounded-2xl border p-6" style={{ borderColor: C.line }}>
          <AlertTriangle size={20} color={C.orange} />
          <p className="text-sm" style={{ color: C.grey }}>{error}</p>
        </div>
      ) : data.length === 0 ? (
        <div className="rounded-3xl border border-dashed py-16 text-center" style={{ borderColor: C.line }}>
          <Activity size={36} color={C.grey} className="mx-auto mb-3 opacity-40" />
          <p className="text-sm font-medium" style={{ color: C.navy }}>
            Belum ada riwayat skrining
          </p>
          <p className="mt-1 text-xs" style={{ color: C.grey }}>
            Lakukan skrining osteoporosis pertama Anda
          </p>
          <Link
            to="/osteoporosis/unggah"
            className="mt-4 inline-flex items-center gap-2 text-sm font-semibold transition hover:underline"
            style={{ color: C.orange }}
          >
            Mulai Skrining <ArrowRight size={14} />
          </Link>
        </div>
      ) : (
        <div className="space-y-3">
          {data.map((item) => (
            <div
              key={item.id}
              className="rounded-2xl border p-4 transition hover:shadow-sm"
              style={{ borderColor: C.line }}
            >
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-semibold" style={{ color: C.navy }}>
                    {item.label || item.risk || "Lihat detail"}
                  </p>
                  <p className="text-xs" style={{ color: C.grey }}>
                    {item.createdAt ? new Date(item.createdAt).toLocaleDateString("id-ID", {
                      day: "numeric", month: "long", year: "numeric",
                      hour: "2-digit", minute: "2-digit",
                    }) : "Tanggal tidak tersedia"}
                  </p>
                </div>
                <span
                  className="rounded-full px-3 py-1 text-xs font-semibold"
                  style={{
                    backgroundColor: item.label?.toLowerCase().includes("normal")
                      ? "#E0F2E9" : C.orangeSoft,
                    color: item.label?.toLowerCase().includes("normal")
                      ? "#1B8A5A" : C.orange,
                  }}
                >
                  {item.label || "N/A"}
                </span>
              </div>
              {item.confidence != null && (
                <div className="mt-2 flex items-center gap-2">
                  <div className="h-1.5 flex-1 rounded-full bg-gray-200">
                    <div
                      className="h-1.5 rounded-full"
                      style={{
                        width: `${item.confidence}%`,
                        backgroundColor: C.navy,
                      }}
                    />
                  </div>
                  <span className="text-xs" style={{ color: C.grey }}>
                    {item.confidence}%
                  </span>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </main>
  );
}
