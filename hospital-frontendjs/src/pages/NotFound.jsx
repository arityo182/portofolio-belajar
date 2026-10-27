import { Link } from "react-router-dom";
import { Home, ArrowLeft } from "lucide-react";
import { C } from "../core/theme";
/**
 * Halaman 404 — tampil saat route tidak ditemukan.
 */
export default function NotFound() {
    return (<main className="flex min-h-[70vh] items-center justify-center px-4 text-center">
      <div>
        <h1 className="text-8xl font-extrabold tracking-tight" style={{ color: C.navy }}>
          404
        </h1>
        <p className="mt-2 text-lg font-medium" style={{ color: C.orange }}>
          Halaman Tidak Ditemukan
        </p>
        <p className="mt-1 text-sm" style={{ color: C.grey }}>
          Maaf, halaman yang Anda cari tidak tersedia atau telah dipindahkan.
        </p>

        <div className="mt-6 flex flex-wrap items-center justify-center gap-3">
          <Link to="/" className="inline-flex items-center gap-2 rounded-xl px-5 py-2.5 text-sm font-semibold text-white shadow transition hover:-translate-y-0.5" style={{ backgroundColor: C.orange }}>
            <Home size={16}/> Beranda
          </Link>
          <button onClick={() => window.history.back()} className="inline-flex items-center gap-2 rounded-xl border px-5 py-2.5 text-sm font-semibold transition hover:bg-gray-50" style={{ borderColor: C.navy, color: C.navy }}>
            <ArrowLeft size={16}/> Kembali
          </button>
        </div>
      </div>
    </main>);
}
