import { useRef, useState } from "react";
import { UploadCloud, X, AlertTriangle } from "lucide-react";
import { C } from "../../../core/theme";

/**
 * Props untuk komponen {@link UploadZone}.
 */
interface UploadZoneProps {
  /** URL pratinjau gambar terpilih, atau `null` bila belum ada file */
  preview: string | null;
  /** Callback saat file dipilih (via klik) atau di-drop ke zona unggah */
  onFile: (file: File) => void;
  /** Callback saat tombol hapus (X) ditekan untuk membatalkan pilihan */
  onClear: () => void;
  /** Pesan error validasi yang ditampilkan di bawah zona (kosong = tidak ada) */
  error: string;
}

/**
 * Zona unggah citra X-ray dengan dukungan drag-and-drop dan klik.
 *
 * Menampilkan area drop kosong bila belum ada `preview`, atau pratinjau
 * gambar beserta tombol hapus bila sudah ada. Validasi tipe/ukuran file
 * ditangani oleh pemanggil lewat callback `onFile`.
 *
 * @param props - lihat {@link UploadZoneProps}
 * @returns Elemen zona unggah
 *
 * @example
 * <UploadZone
 *   preview={preview}
 *   onFile={handleFile}
 *   onClear={handleClear}
 *   error={error}
 * />
 */
export default function UploadZone({ preview, onFile, onClear, error }: UploadZoneProps) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [drag, setDrag] = useState(false);

  const pick = () => inputRef.current?.click();

  const onInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0];
    if (f) onFile(f);
  };

  const onDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setDrag(false);
    const f = e.dataTransfer.files?.[0];
    if (f) onFile(f);
  };

  return (
    <div>
      <input
        ref={inputRef}
        type="file"
        accept="image/png, image/jpeg"
        className="hidden"
        onChange={onInputChange}
      />

      {!preview ? (
        <div
          onClick={pick}
          onDragOver={(e) => {
            e.preventDefault();
            setDrag(true);
          }}
          onDragLeave={() => setDrag(false)}
          onDrop={onDrop}
          className="flex cursor-pointer flex-col items-center justify-center rounded-3xl border-2 border-dashed px-6 py-14 text-center transition-colors"
          style={{
            borderColor: drag ? C.orange : C.line,
            backgroundColor: drag ? C.orangeSoft : C.cream,
          }}
        >
          <div
            className="flex h-16 w-16 items-center justify-center rounded-full"
            style={{ backgroundColor: C.white }}
          >
            <UploadCloud size={30} color={C.navy} />
          </div>
          <p className="mt-4 text-base font-bold" style={{ color: C.navy }}>
            Tarik &amp; lepas citra X-ray di sini
          </p>
          <p className="mt-1 text-sm" style={{ color: C.grey }}>
            atau klik untuk memilih file
          </p>
          <p className="mt-3 text-xs" style={{ color: C.grey }}>
            Format JPG atau PNG · maks. 10MB
          </p>
        </div>
      ) : (
        <div
          className="relative overflow-hidden rounded-3xl border"
          style={{ borderColor: C.line, backgroundColor: C.navy }}
        >
          <img
            src={preview}
            alt="Pratinjau X-ray"
            className="mx-auto max-h-80 w-full object-contain"
          />
          <button
            onClick={onClear}
            className="absolute right-3 top-3 flex h-9 w-9 items-center justify-center rounded-full transition-transform hover:scale-105"
            style={{ backgroundColor: C.white }}
            aria-label="Hapus gambar"
          >
            <X size={18} color={C.navy} />
          </button>
        </div>
      )}

      {error && (
        <div
          className="mt-3 flex items-center gap-2 rounded-xl px-3 py-2.5"
          style={{ backgroundColor: "#FCEBEB" }}
        >
          <AlertTriangle size={16} color="#A32D2D" />
          <span className="text-sm font-medium" style={{ color: "#A32D2D" }}>{error}</span>
        </div>
      )}
    </div>
  );
}
