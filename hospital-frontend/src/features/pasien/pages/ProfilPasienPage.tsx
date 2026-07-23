import { useEffect, useState } from "react";
import { AxiosError } from "axios";
import {
  UserRound, Loader2, AlertTriangle, Pencil, Check, X, IdCard,
  CalendarDays, MapPin, Phone, Droplet, FileText,
} from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import { useAuth } from "../../../core/context/AuthContext";
import {
  getPasienByUserId, createPasien, updatePasien,
} from "../services/pasienService";
import type { Pasien, PasienRequest, JenisKelamin } from "../types";

/**
 * @module features/pasien/pages/ProfilPasienPage
 *
 * Halaman profil pasien (FR-06, FR-07).
 *
 * Memenuhi kebutuhan pasien untuk melihat dan melengkapi/mengubah data
 * profilnya (NIK, tanggal lahir, alamat, dll) serta menampilkan nomor
 * rekam medis unik yang digenerate backend.
 *
 * Tiga mode tampilan:
 * 1. **Belum terdaftar** (GET `/api/pasien/user/{userId}` → 404): menampilkan
 *    form pembuatan profil (FR-06 "melengkapi data profil").
 * 2. **Lihat** (default setelah profil ada): menampilkan data read-only dengan
 *    tombol "Edit".
 * 3. **Edit** (semantik PUT = replace): form pembaruan profil.
 */

/** Pilihan jenis kelamin untuk ditampilkan pada select. */
const PILIHAN_JK: { value: JenisKelamin; label: string }[] = [
  { value: "L", label: "Laki-laki" },
  { value: "P", label: "Perempuan" },
];

/** Pilihan golongan darah. */
const PILIHAN_GOL = ["", "A", "B", "AB", "O"];

/**
 * Membangun payload {@link PasienRequest} dari data profil pasien yang ada
 * (dipakai saat pra-mengisi form edit).
 *
 * @param p - data pasien yang sudah tersimpan
 * @param userId - ID akun user yang sedang login
 * @returns payload siap kirim untuk PUT
 */
function buildPayload(p: Pasien, userId: number): PasienRequest {
  return {
    userId,
    nik: p.nik,
    tanggalLahir: p.tanggalLahir,
    jenisKelamin: p.jenisKelamin,
    alamat: p.alamat ?? "",
    noHp: p.noHp,
    golDarah: p.golDarah ?? "",
  };
}

/**
 * Halaman profil pasien: tampil / edit / lengkapi profil.
 *
 * @returns Halaman profil pasien
 */
export default function ProfilPasienPage() {
  const { user } = useAuth();

  // --- State profil ---
  const [pasien, setPasien] = useState<Pasien | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  /** true bila user login belum punya profil pasien (404). */
  const [belumTerdaftar, setBelumTerdaftar] = useState(false);

  // --- State form (create & edit) ---
  const [mode, setMode] = useState<"view" | "edit" | "create">("view");
  const [form, setForm] = useState<PasienRequest | null>(null);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  // Muat profil pasien saat mount.
  useEffect(() => {
    if (!user) return;
    let aktif = true;
    setLoading(true);
    setError(null);
    setBelumTerdaftar(false);
    getPasienByUserId(user.id)
      .then((res) => {
        if (!aktif) return;
        setPasien(res.data);
        setMode("view");
      })
      .catch((err) => {
        if (!aktif) return;
        if (err instanceof AxiosError && err.response?.status === 404) {
          // Belum punya profil — siapkan form create kosong.
          setBelumTerdaftar(true);
          setMode("create");
          setForm({
            userId: user.id,
            nik: "",
            tanggalLahir: "",
            jenisKelamin: "L",
            alamat: "",
            noHp: "",
            golDarah: "",
          });
        } else {
          setError(toErrorMessage(err));
        }
      })
      .finally(() => aktif && setLoading(false));
    return () => {
      aktif = false;
    };
  }, [user]);

  /** Masuk ke mode edit: pra-isi form dari data yang sudah ada. */
  const mulaiEdit = () => {
    if (!pasien || !user) return;
    setForm(buildPayload(pasien, user.id));
    setSaveError(null);
    setMode("edit");
  };

  /** Batal edit/create — kembali ke view (atau tetap di create bila belum ada). */
  const batalEdit = () => {
    setSaveError(null);
    if (mode === "edit") {
      setMode("view");
    }
    // bila mode === "create" dan belum punya profil, tetap di create
  };

  /** Simpan (create atau update). */
  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form || !user) return;
    setSaving(true);
    setSaveError(null);
    try {
      if (mode === "create") {
        const res = await createPasien(form);
        setPasien(res.data);
        setBelumTerdaftar(false);
        setMode("view");
      } else if (mode === "edit" && pasien) {
        const res = await updatePasien(pasien.id, form);
        setPasien(res.data);
        setMode("view");
      }
    } catch (err) {
      setSaveError(toErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const spinner = <Loader2 className="animate-spin" size={22} color={C.navy} />;

  // --- State: loading awal ---
  if (loading) {
    return (
      <div className="flex flex-col items-center gap-3 py-24">
        {spinner}
        <p className="text-sm" style={{ color: C.grey }}>Memuat profil pasien...</p>
      </div>
    );
  }

  // --- State: error fatal ---
  if (error && !belumTerdaftar) {
    return (
      <section className="mx-auto max-w-2xl px-5 py-16">
        <div
          className="flex flex-col items-center gap-3 rounded-3xl border border-dashed px-6 py-16 text-center"
          style={{ borderColor: C.line }}
        >
          <AlertTriangle size={30} color={C.orange} />
          <p className="text-sm" style={{ color: C.grey }}>{error}</p>
        </div>
      </section>
    );
  }

  return (
    <div>
      {/* Hero */}
      <section style={{ backgroundColor: C.navy }} className="relative overflow-hidden">
        <div
          className="pointer-events-none absolute -right-20 -top-16 h-64 w-64 rounded-full opacity-15"
          style={{ backgroundColor: C.blue }}
        />
        <div className="relative mx-auto max-w-4xl px-5 py-14 md:py-16">
          <span
            className="inline-flex items-center gap-2 rounded-full px-3.5 py-1.5 text-xs font-semibold"
            style={{ backgroundColor: "rgba(255,255,255,0.12)", color: C.blue }}
          >
            <UserRound size={14} /> Profil Pasien
          </span>
          <h1 className="mt-5 text-4xl font-bold leading-tight text-white md:text-5xl">
            {mode === "create" ? "Lengkapi data diri Anda" : "Data diri pasien"}
          </h1>
          <p className="mt-4 max-w-xl text-base leading-relaxed" style={{ color: "#C9D2EC" }}>
            {mode === "create"
              ? "Profil pasien Anda belum tersimpan. Lengkapi data berikut untuk mulai berobat."
              : " Tinjau dan perbarui informasi pribadi Anda kapan saja."}
          </p>
        </div>
      </section>

      <section className="mx-auto max-w-4xl px-5 py-12">
        {mode === "view" && pasien ? (
          <ProfilView pasien={pasien} onEdit={mulaiEdit} />
        ) : form ? (
          <ProfilForm
            mode={mode === "view" ? "edit" : mode}
            form={form}
            setForm={setForm}
            saving={saving}
            saveError={saveError}
            onSubmit={handleSave}
            onCancel={batalEdit}
          />
        ) : null}
      </section>
    </div>
  );
}

/* ------------------------------------------------------------------ */
/* Sub-komponen: tampilan read-only                                    */
/* ------------------------------------------------------------------ */

interface ProfilViewProps {
  pasien: Pasien;
  onEdit: () => void;
}

/**
 * Tampilan read-only profil pasien dengan tombol Edit.
 *
 * @param props - lihat {@link ProfilViewProps}
 */
function ProfilView({ pasien, onEdit }: ProfilViewProps) {
  return (
    <div
      className="rounded-3xl border p-6 md:p-8"
      style={{ backgroundColor: C.white, borderColor: C.line }}
    >
      {/* Header kartu */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <div
            className="flex h-16 w-16 items-center justify-center rounded-full"
            style={{ backgroundColor: C.blueSoft }}
          >
            <UserRound size={32} color={C.navy} />
          </div>
          <div>
            <h2 className="text-xl font-bold" style={{ color: C.navy }}>{pasien.nama}</h2>
            <p className="text-sm" style={{ color: C.grey }}>{pasien.email}</p>
          </div>
        </div>
        <button
          type="button"
          onClick={onEdit}
          className="inline-flex items-center gap-2 rounded-full px-5 py-2.5 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5"
          style={{ backgroundColor: C.orange }}
        >
          <Pencil size={16} /> Edit Profil
        </button>
      </div>

      {/* Badge No. Rekam Medis (FR-07) */}
      <div
        className="mt-6 flex items-center gap-3 rounded-2xl px-5 py-4"
        style={{ backgroundColor: C.navy }}
      >
        <FileText size={20} color={C.white} />
        <div>
          <p className="text-xs" style={{ color: "#C9D2EC" }}>Nomor Rekam Medis</p>
          <p className="text-lg font-bold text-white">{pasien.noRekamMedis}</p>
        </div>
      </div>

      {/* Grid detail */}
      <div className="mt-6 grid gap-4 sm:grid-cols-2">
        <DetailItem icon={<IdCard size={18} color={C.orange} />} label="NIK" value={pasien.nik} />
        <DetailItem
          icon={<CalendarDays size={18} color={C.orange} />}
          label="Tanggal Lahir"
          value={formatTanggal(pasien.tanggalLahir)}
        />
        <DetailItem
          icon={<UserRound size={18} color={C.orange} />}
          label="Jenis Kelamin"
          value={pasien.jenisKelamin === "L" ? "Laki-laki" : "Perempuan"}
        />
        <DetailItem
          icon={<Droplet size={18} color={C.orange} />}
          label="Golongan Darah"
          value={pasien.golDarah || "-"}
        />
        <DetailItem icon={<Phone size={18} color={C.orange} />} label="No. HP" value={pasien.noHp} />
        <DetailItem
          icon={<MapPin size={18} color={C.orange} />}
          label="Alamat"
          value={pasien.alamat || "-"}
          span
        />
      </div>
    </div>
  );
}

/** Satu baris detail pada tampilan read-only. */
function DetailItem({
  icon, label, value, span,
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
  span?: boolean;
}) {
  return (
    <div className={span ? "sm:col-span-2" : ""}>
      <div className="flex items-center gap-2">
        {icon}
        <span className="text-xs font-semibold uppercase tracking-wide" style={{ color: C.grey }}>
          {label}
        </span>
      </div>
      <p className="mt-1 text-sm font-medium" style={{ color: C.navy }}>{value}</p>
    </div>
  );
}

/* ------------------------------------------------------------------ */
/* Sub-komponen: form create / edit                                   */
/* ------------------------------------------------------------------ */

interface ProfilFormProps {
  mode: "create" | "edit";
  form: PasienRequest;
  setForm: React.Dispatch<React.SetStateAction<PasienRequest | null>>;
  saving: boolean;
  saveError: string | null;
  onSubmit: (e: React.FormEvent) => void;
  onCancel: () => void;
}

/**
 * Form input untuk membuat atau mengubah profil pasien.
 *
 * @param props - lihat {@link ProfilFormProps}
 */
function ProfilForm({
  mode, form, setForm, saving, saveError, onSubmit, onCancel,
}: ProfilFormProps) {
  /** Helper mengubah satu field form. */
  const set = <K extends keyof PasienRequest>(key: K, val: PasienRequest[K]) =>
    setForm((f) => (f ? { ...f, [key]: val } : f));

  return (
    <form
      onSubmit={onSubmit}
      className="rounded-3xl border p-6 md:p-8"
      style={{ backgroundColor: C.white, borderColor: C.line }}
    >
      <div className="mb-6 flex items-center gap-3">
        <div
          className="flex h-12 w-12 items-center justify-center rounded-full"
          style={{ backgroundColor: C.blueSoft }}
        >
          <UserRound size={24} color={C.navy} />
        </div>
        <div>
          <h2 className="text-lg font-bold" style={{ color: C.navy }}>
            {mode === "create" ? "Lengkapi Profil" : "Edit Profil"}
          </h2>
          <p className="text-sm" style={{ color: C.grey }}>
            {mode === "create"
              ? "Data ini dipakai untuk membuat janji temu."
              : "Perbarui data yang ingin Anda ubah."}
          </p>
        </div>
      </div>

      <div className="grid gap-5 sm:grid-cols-2">
        <Field label="NIK" required>
          <input
            type="text"
            value={form.nik}
            onChange={(e) => set("nik", e.target.value)}
            maxLength={16}
            placeholder="16 digit NIK"
            className={inputCls}
            required
          />
        </Field>

        <Field label="Tanggal Lahir" required>
          <input
            type="date"
            value={form.tanggalLahir}
            max={todayStr()}
            onChange={(e) => set("tanggalLahir", e.target.value)}
            className={inputCls}
            required
          />
        </Field>

        <Field label="Jenis Kelamin" required>
          <select
            value={form.jenisKelamin}
            onChange={(e) => set("jenisKelamin", e.target.value as JenisKelamin)}
            className={inputCls}
            required
          >
            {PILIHAN_JK.map((j) => (
              <option key={j.value} value={j.value}>{j.label}</option>
            ))}
          </select>
        </Field>

        <Field label="Golongan Darah">
          <select
            value={form.golDarah ?? ""}
            onChange={(e) => set("golDarah", e.target.value || null)}
            className={inputCls}
          >
            {PILIHAN_GOL.map((g) => (
              <option key={g} value={g}>{g === "" ? "Belum tahu" : g}</option>
            ))}
          </select>
        </Field>

        <Field label="No. HP" required>
          <input
            type="tel"
            value={form.noHp}
            onChange={(e) => set("noHp", e.target.value)}
            placeholder="08xxxxxxxxxx"
            className={inputCls}
            required
          />
        </Field>

        <Field label="Alamat" required span>
          <textarea
            value={form.alamat}
            onChange={(e) => set("alamat", e.target.value)}
            rows={2}
            placeholder="Alamat tempat tinggal"
            className={`${inputCls} resize-none`}
            required
          />
        </Field>
      </div>

      {saveError && (
        <div
          className="mt-5 flex items-start gap-2 rounded-2xl px-4 py-3"
          style={{ backgroundColor: "#FCE8E6" }}
        >
          <AlertTriangle size={18} color="#C0392B" className="mt-0.5 flex-shrink-0" />
          <p className="text-sm font-medium" style={{ color: "#C0392B" }}>{saveError}</p>
        </div>
      )}

      <div className="mt-6 flex items-center gap-3">
        <button
          type="submit"
          disabled={saving}
          className="inline-flex items-center gap-2 rounded-full px-6 py-3 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5 disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:translate-y-0"
          style={{ backgroundColor: C.orange }}
        >
          {saving ? <Loader2 className="animate-spin" size={17} /> : <Check size={17} />}
          {saving ? "Menyimpan..." : "Simpan Profil"}
        </button>
        <button
          type="button"
          onClick={onCancel}
          disabled={saving}
          className="inline-flex items-center gap-2 rounded-full px-6 py-3 text-sm font-semibold transition-opacity hover:opacity-80 disabled:opacity-50"
          style={{ backgroundColor: C.cream, color: C.navy }}
        >
          <X size={17} /> Batal
        </button>
      </div>
    </form>
  );
}

/** Kelas input standar untuk form profil. */
const inputCls =
  "w-full rounded-2xl border px-4 py-3 text-sm outline-none focus:border-[#EF7A00]";

/** Wrapper satu field form (label + input). */
function Field({
  label, required, span, children,
}: {
  label: string;
  required?: boolean;
  span?: boolean;
  children: React.ReactNode;
}) {
  return (
    <div className={span ? "sm:col-span-2" : ""}>
      <label className="mb-1.5 block text-sm font-semibold" style={{ color: C.navy }}>
        {label} {required && <span style={{ color: C.orange }}>*</span>}
      </label>
      {children}
    </div>
  );
}

/**
 * Memformat tanggal "yyyy-MM-dd" menjadi teks Indonesia, mis. "13 Juli 2026".
 *
 * @param iso - tanggal format "yyyy-MM-dd"
 * @returns tanggal ramah-baca; mengembalikan input apa adanya bila tak valid
 */
function formatTanggal(iso: string): string {
  const [y, m, d] = iso.split("-").map(Number);
  if (!y || !m || !d) return iso;
  const bulan = [
    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
    "Juli", "Agustus", "September", "Oktober", "November", "Desember",
  ];
  return `${d} ${bulan[m - 1]} ${y}`;
}

/**
 * Mengembalikan tanggal hari ini dalam format "yyyy-MM-dd" (zona waktu lokal).
 *
 * @returns string tanggal hari ini, mis. "2026-07-15"
 */
function todayStr(): string {
  const n = new Date();
  const mm = String(n.getMonth() + 1).padStart(2, "0");
  const dd = String(n.getDate()).padStart(2, "0");
  return `${n.getFullYear()}-${mm}-${dd}`;
}