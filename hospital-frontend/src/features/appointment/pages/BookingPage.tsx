import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { AxiosError } from "axios";
import {
  CalendarPlus, Loader2, AlertTriangle, ChevronRight, Stethoscope,
  Clock, Users, Check, CheckCircle2, Ticket,
} from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import { useAuth } from "../../../core/context/AuthContext";
import { getPasienByUserId } from "../../pasien/services/pasienService";
import { getAllPoli } from "../../poli/services/poliService";
import type { Poli } from "../../poli/types";
import PoliCard from "../../poli/components/PoliCard";
import { getDokterByPoli } from "../../dokter/services/dokterService";
import type { Dokter } from "../../dokter/types";
import DokterCard from "../../dokter/components/DokterCard";
import { getJadwalByDokter } from "../../jadwal/services/jadwalService";
import type { JadwalDokter, Hari } from "../../jadwal/types";
import { createAppointment } from "../services/appointmentService";
import type { AppointmentResponse } from "../types";

/** Label hari ramah-baca per nilai enum {@link Hari}. */
const LABEL_HARI: Record<Hari, string> = {
  SENIN: "Senin", SELASA: "Selasa", RABU: "Rabu", KAMIS: "Kamis",
  JUMAT: "Jumat", SABTU: "Sabtu", MINGGU: "Minggu",
};

/** Enum {@link Hari} diindeks berdasarkan `Date.getDay()` (0=Minggu..6=Sabtu). */
const HARI_BY_JS_DAY: Hari[] = [
  "MINGGU", "SENIN", "SELASA", "RABU", "KAMIS", "JUMAT", "SABTU",
];

/**
 * Mengembalikan tanggal hari ini dalam format "yyyy-MM-dd" (zona waktu lokal).
 *
 * @returns string tanggal hari ini, mis. "2026-07-13"
 * @example todayStr(); // "2026-07-13"
 */
function todayStr(): string {
  const n = new Date();
  const mm = String(n.getMonth() + 1).padStart(2, "0");
  const dd = String(n.getDate()).padStart(2, "0");
  return `${n.getFullYear()}-${mm}-${dd}`;
}

/**
 * Menentukan enum {@link Hari} dari string tanggal "yyyy-MM-dd" (lokal).
 *
 * @param iso - tanggal format "yyyy-MM-dd"
 * @returns {@link Hari} yang bersesuaian, atau `null` bila format tak valid
 * @example hariFromTanggal("2026-07-13"); // "SENIN"
 */
function hariFromTanggal(iso: string): Hari | null {
  const [y, m, d] = iso.split("-").map(Number);
  if (!y || !m || !d) return null;
  const dt = new Date(y, m - 1, d);
  if (Number.isNaN(dt.getTime())) return null;
  return HARI_BY_JS_DAY[dt.getDay()];
}

/** Memangkas "HH:mm:ss" menjadi "HH:mm". */
function jam(waktu: string): string {
  return waktu.slice(0, 5);
}

/**
 * Halaman booking janji temu (FR-09, FR-12).
 *
 * Alur: pilih poli → dokter → jadwal, lalu tentukan tanggal & keluhan dan
 * submit. Tanggal diberi validasi ringan di UI (tidak boleh masa lalu dan
 * idealnya cocok dengan hari jadwal) — namun backend tetap sumber kebenaran.
 * `pasienId` diperoleh dari user login via `GET /api/pasien/user/{userId}`.
 * Setelah sukses, nomor antrian (auto-generate backend) ditampilkan.
 *
 * @returns Halaman booking
 */
export default function BookingPage() {
  const { user } = useAuth();

  // Resolusi pasienId dari user login.
  const [pasienId, setPasienId] = useState<number | null>(null);
  const [pasienLoading, setPasienLoading] = useState(true);
  const [pasienError, setPasienError] = useState<string | null>(null);
  const [belumTerdaftar, setBelumTerdaftar] = useState(false);

  // Poli.
  const [poliList, setPoliList] = useState<Poli[]>([]);
  const [poliLoading, setPoliLoading] = useState(true);
  const [poliError, setPoliError] = useState<string | null>(null);
  const [selectedPoliId, setSelectedPoliId] = useState<number | null>(null);

  // Dokter.
  const [dokterList, setDokterList] = useState<Dokter[]>([]);
  const [dokterLoading, setDokterLoading] = useState(false);
  const [dokterError, setDokterError] = useState<string | null>(null);
  const [selectedDokterId, setSelectedDokterId] = useState<number | null>(null);

  // Jadwal.
  const [jadwalList, setJadwalList] = useState<JadwalDokter[]>([]);
  const [jadwalLoading, setJadwalLoading] = useState(false);
  const [jadwalError, setJadwalError] = useState<string | null>(null);
  const [selectedJadwal, setSelectedJadwal] = useState<JadwalDokter | null>(null);

  // Form.
  const [tanggal, setTanggal] = useState("");
  const [keluhan, setKeluhan] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [success, setSuccess] = useState<AppointmentResponse | null>(null);

  // Resolusi pasien saat mount.
  useEffect(() => {
    if (!user) return;
    let aktif = true;
    setPasienLoading(true);
    setPasienError(null);
    setBelumTerdaftar(false);
    getPasienByUserId(user.id)
      .then((res) => aktif && setPasienId(res.data.id))
      .catch((err) => {
        if (!aktif) return;
        if (err instanceof AxiosError && err.response?.status === 404) {
          setBelumTerdaftar(true);
        } else {
          setPasienError(toErrorMessage(err));
        }
      })
      .finally(() => aktif && setPasienLoading(false));
    return () => { aktif = false; };
  }, [user]);

  // Muat poli saat mount.
  useEffect(() => {
    let aktif = true;
    setPoliLoading(true);
    setPoliError(null);
    getAllPoli()
      .then((res) => aktif && setPoliList(res.data.filter((p) => p.isActive)))
      .catch((err) => aktif && setPoliError(toErrorMessage(err)))
      .finally(() => aktif && setPoliLoading(false));
    return () => { aktif = false; };
  }, []);

  // Muat dokter saat poli berubah; reset pilihan di bawahnya.
  useEffect(() => {
    if (selectedPoliId === null) return;
    let aktif = true;
    setDokterLoading(true);
    setDokterError(null);
    setDokterList([]);
    setSelectedDokterId(null);
    setJadwalList([]);
    setSelectedJadwal(null);
    getDokterByPoli(selectedPoliId)
      .then((res) => aktif && setDokterList(res.data))
      .catch((err) => aktif && setDokterError(toErrorMessage(err)))
      .finally(() => aktif && setDokterLoading(false));
    return () => { aktif = false; };
  }, [selectedPoliId]);

  // Muat jadwal saat dokter berubah.
  useEffect(() => {
    if (selectedDokterId === null) return;
    let aktif = true;
    setJadwalLoading(true);
    setJadwalError(null);
    setJadwalList([]);
    setSelectedJadwal(null);
    getJadwalByDokter(selectedDokterId)
      .then((res) => aktif && setJadwalList(res.data))
      .catch((err) => aktif && setJadwalError(toErrorMessage(err)))
      .finally(() => aktif && setJadwalLoading(false));
    return () => { aktif = false; };
  }, [selectedDokterId]);

  // Validasi ringan tanggal.
  const hariTanggal = useMemo(() => (tanggal ? hariFromTanggal(tanggal) : null), [tanggal]);
  const tanggalMasaLalu = tanggal !== "" && tanggal < todayStr();
  const hariMismatch =
    selectedJadwal !== null && hariTanggal !== null && hariTanggal !== selectedJadwal.hari;

  const canSubmit =
    pasienId !== null &&
    selectedJadwal !== null &&
    tanggal !== "" &&
    !tanggalMasaLalu &&
    !hariMismatch &&
    !submitting;

  /** Mengirim permintaan booking ke backend. */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!canSubmit || pasienId === null || selectedJadwal === null) return;
    setSubmitting(true);
    setSubmitError(null);
    try {
      const res = await createAppointment({
        pasienId,
        jadwalId: selectedJadwal.id,
        tanggal,
        keluhan: keluhan.trim() || undefined,
      });
      setSuccess(res.data);
    } catch (err) {
      setSubmitError(toErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  /** Mereset seluruh pilihan untuk membuat janji temu baru. */
  const resetForm = () => {
    setSelectedPoliId(null);
    setSelectedDokterId(null);
    setSelectedJadwal(null);
    setDokterList([]);
    setJadwalList([]);
    setTanggal("");
    setKeluhan("");
    setSubmitError(null);
    setSuccess(null);
  };

  const spinner = <Loader2 className="animate-spin" size={20} color={C.navy} />;

  // --- Layar sukses ---
  if (success) {
    return (
      <section className="mx-auto max-w-2xl px-5 py-16">
        <div
          className="flex flex-col items-center gap-4 rounded-[2rem] border p-10 text-center"
          style={{ backgroundColor: C.white, borderColor: C.line }}
        >
          <CheckCircle2 size={56} color="#1B8A5A" />
          <h1 className="text-2xl font-bold" style={{ color: C.navy }}>
            Janji temu berhasil dibuat
          </h1>
          <p className="text-sm" style={{ color: C.grey }}>
            {success.namaDokter} &middot; {success.namaPoli}
          </p>
          {success.nomorAntrian !== null && (
            <div
              className="mt-2 flex items-center gap-3 rounded-2xl px-6 py-4"
              style={{ backgroundColor: C.navy }}
            >
              <Ticket size={24} color={C.white} />
              <div className="text-left">
                <p className="text-xs" style={{ color: "#C9D2EC" }}>Nomor Antrian</p>
                <p className="text-2xl font-bold text-white">{success.nomorAntrian}</p>
              </div>
            </div>
          )}
          <div className="mt-4 flex flex-wrap items-center justify-center gap-3">
            <Link
              to="/appointment-saya"
              className="rounded-full px-6 py-3 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5"
              style={{ backgroundColor: C.orange }}
            >
              Lihat Appointment Saya
            </Link>
            <button
              type="button"
              onClick={resetForm}
              className="rounded-full px-6 py-3 text-sm font-semibold transition-opacity hover:opacity-80"
              style={{ backgroundColor: C.cream, color: C.navy }}
            >
              Buat Janji Lagi
            </button>
          </div>
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
            <CalendarPlus size={14} /> Buat Janji Temu
          </span>
          <h1 className="mt-5 text-4xl font-bold leading-tight text-white md:text-5xl">
            Booking dokter dengan mudah
          </h1>
          <p className="mt-4 max-w-xl text-base leading-relaxed" style={{ color: "#C9D2EC" }}>
            Pilih poli, dokter, dan jadwal, lalu tentukan tanggal kunjungan Anda.
          </p>
        </div>
      </section>

      <section className="mx-auto max-w-4xl px-5 py-12">
        {pasienLoading ? (
          <div className="flex flex-col items-center gap-3 py-16">
            {spinner}
            <p className="text-sm" style={{ color: C.grey }}>Menyiapkan data pasien...</p>
          </div>
        ) : belumTerdaftar ? (
          <div
            className="flex flex-col items-center gap-3 rounded-3xl border border-dashed px-6 py-16 text-center"
            style={{ borderColor: C.line }}
          >
            <AlertTriangle size={30} color={C.orange} />
            <p className="text-base font-semibold" style={{ color: C.navy }}>
              Profil pasien belum lengkap
            </p>
            <p className="max-w-md text-sm" style={{ color: C.grey }}>
              Lengkapi data pasien terlebih dahulu sebelum membuat janji temu.
              (Pelengkapan profil tersedia pada fitur pasien.)
            </p>
          </div>
        ) : pasienError ? (
          <div
            className="flex flex-col items-center gap-3 rounded-3xl border border-dashed px-6 py-16 text-center"
            style={{ borderColor: C.line }}
          >
            <AlertTriangle size={30} color={C.orange} />
            <p className="text-sm" style={{ color: C.grey }}>{pasienError}</p>
          </div>
        ) : (
          <div className="flex flex-col gap-8">
            {/* Langkah 1: Poli */}
            <StepBlock n="1" title="Pilih Poli" active>
              {poliLoading ? (
                <Hint icon={spinner} text="Memuat poli..." />
              ) : poliError ? (
                <Hint icon={<AlertTriangle size={18} color={C.orange} />} text={poliError} />
              ) : poliList.length === 0 ? (
                <Hint icon={<AlertTriangle size={18} color={C.orange} />} text="Belum ada poli aktif." />
              ) : (
                <div className="grid gap-3 sm:grid-cols-2">
                  {poliList.map((p) => (
                    <PoliCard key={p.id} poli={p} selected={p.id === selectedPoliId} onSelect={setSelectedPoliId} />
                  ))}
                </div>
              )}
            </StepBlock>

            {/* Langkah 2: Dokter */}
            <StepBlock n="2" title="Pilih Dokter" active={selectedPoliId !== null}>
              {selectedPoliId === null ? (
                <Hint icon={<ChevronRight size={18} color={C.grey} />} text="Pilih poli terlebih dahulu." />
              ) : dokterLoading ? (
                <Hint icon={spinner} text="Memuat dokter..." />
              ) : dokterError ? (
                <Hint icon={<AlertTriangle size={18} color={C.orange} />} text={dokterError} />
              ) : dokterList.length === 0 ? (
                <Hint icon={<AlertTriangle size={18} color={C.orange} />} text="Belum ada dokter di poli ini." />
              ) : (
                <div className="grid gap-3 sm:grid-cols-2">
                  {dokterList.map((d) => (
                    <DokterCard key={d.id} dokter={d} selected={d.id === selectedDokterId} onSelect={setSelectedDokterId} />
                  ))}
                </div>
              )}
            </StepBlock>

            {/* Langkah 3: Jadwal */}
            <StepBlock n="3" title="Pilih Jadwal" active={selectedDokterId !== null}>
              {selectedDokterId === null ? (
                <Hint icon={<Stethoscope size={18} color={C.grey} />} text="Pilih dokter terlebih dahulu." />
              ) : jadwalLoading ? (
                <Hint icon={spinner} text="Memuat jadwal..." />
              ) : jadwalError ? (
                <Hint icon={<AlertTriangle size={18} color={C.orange} />} text={jadwalError} />
              ) : jadwalList.length === 0 ? (
                <Hint icon={<AlertTriangle size={18} color={C.orange} />} text="Dokter ini belum punya jadwal aktif." />
              ) : (
                <div className="grid gap-3 sm:grid-cols-2">
                  {jadwalList.map((j) => {
                    const dipilih = selectedJadwal?.id === j.id;
                    return (
                      <button
                        key={j.id}
                        type="button"
                        onClick={() => setSelectedJadwal(j)}
                        className="flex items-center justify-between rounded-3xl border p-4 text-left transition-all hover:-translate-y-0.5"
                        style={{
                          backgroundColor: dipilih ? C.orangeSoft : C.white,
                          borderColor: dipilih ? C.orange : C.line,
                        }}
                        aria-pressed={dipilih}
                      >
                        <div>
                          <p className="text-base font-bold" style={{ color: C.navy }}>{LABEL_HARI[j.hari]}</p>
                          <div className="mt-1 flex items-center gap-3 text-sm" style={{ color: C.grey }}>
                            <span className="flex items-center gap-1"><Clock size={14} color={C.orange} /> {jam(j.jamMulai)}–{jam(j.jamSelesai)}</span>
                            <span className="flex items-center gap-1"><Users size={14} color={C.orange} /> Kuota {j.kuota}</span>
                          </div>
                        </div>
                        {dipilih && (
                          <span className="flex h-6 w-6 items-center justify-center rounded-full" style={{ backgroundColor: C.orange }}>
                            <Check size={15} color={C.white} strokeWidth={3} />
                          </span>
                        )}
                      </button>
                    );
                  })}
                </div>
              )}
            </StepBlock>

            {/* Langkah 4: Detail */}
            <StepBlock n="4" title="Tanggal & Keluhan" active={selectedJadwal !== null}>
              {selectedJadwal === null ? (
                <Hint icon={<ChevronRight size={18} color={C.grey} />} text="Pilih jadwal terlebih dahulu." />
              ) : (
                <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                  <div>
                    <label className="mb-1.5 block text-sm font-semibold" style={{ color: C.navy }}>
                      Tanggal kunjungan
                    </label>
                    <input
                      type="date"
                      value={tanggal}
                      min={todayStr()}
                      onChange={(e) => setTanggal(e.target.value)}
                      className="w-full rounded-2xl border px-4 py-3 text-sm outline-none"
                      style={{ borderColor: C.line, color: C.navy }}
                      required
                    />
                    <p className="mt-1.5 text-xs" style={{ color: C.grey }}>
                      Jadwal dokter: setiap hari <strong>{LABEL_HARI[selectedJadwal.hari]}</strong>.
                    </p>
                    {tanggalMasaLalu && (
                      <p className="mt-1 text-xs font-medium" style={{ color: "#C0392B" }}>
                        Tanggal tidak boleh di masa lalu.
                      </p>
                    )}
                    {hariMismatch && hariTanggal && (
                      <p className="mt-1 text-xs font-medium" style={{ color: "#C0392B" }}>
                        Tanggal tersebut jatuh pada hari {LABEL_HARI[hariTanggal]}, sedangkan
                        jadwal dokter hari {LABEL_HARI[selectedJadwal.hari]}.
                      </p>
                    )}
                  </div>

                  <div>
                    <label className="mb-1.5 block text-sm font-semibold" style={{ color: C.navy }}>
                      Keluhan <span className="font-normal" style={{ color: C.grey }}>(opsional)</span>
                    </label>
                    <textarea
                      value={keluhan}
                      onChange={(e) => setKeluhan(e.target.value)}
                      rows={3}
                      placeholder="Ceritakan keluhan Anda secara singkat..."
                      className="w-full resize-none rounded-2xl border px-4 py-3 text-sm outline-none"
                      style={{ borderColor: C.line, color: C.navy }}
                    />
                  </div>

                  {submitError && (
                    <div
                      className="flex items-start gap-2 rounded-2xl px-4 py-3"
                      style={{ backgroundColor: "#FCE8E6" }}
                    >
                      <AlertTriangle size={18} color="#C0392B" className="mt-0.5 flex-shrink-0" />
                      <p className="text-sm font-medium" style={{ color: "#C0392B" }}>{submitError}</p>
                    </div>
                  )}

                  <button
                    type="submit"
                    disabled={!canSubmit}
                    className="inline-flex items-center justify-center gap-2 rounded-full px-7 py-3.5 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5 disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:translate-y-0"
                    style={{ backgroundColor: C.orange }}
                  >
                    {submitting ? <Loader2 className="animate-spin" size={18} /> : <CalendarPlus size={18} />}
                    {submitting ? "Memproses..." : "Konfirmasi Janji Temu"}
                  </button>
                </form>
              )}
            </StepBlock>
          </div>
        )}
      </section>
    </div>
  );
}

/**
 * Properti komponen bantu {@link StepBlock}.
 */
interface StepBlockProps {
  /** Nomor langkah (badge) */
  n: string;
  /** Judul langkah */
  title: string;
  /** Apakah langkah aktif (badge oranye) atau belum (abu-abu) */
  active: boolean;
  /** Konten langkah */
  children: React.ReactNode;
}

/**
 * Blok satu langkah pada wizard booking (badge nomor + judul + konten).
 *
 * @param props - lihat {@link StepBlockProps}
 * @returns Elemen blok langkah
 */
function StepBlock({ n, title, active, children }: StepBlockProps) {
  return (
    <div>
      <div className="mb-4 flex items-center gap-2">
        <span
          className="flex h-7 w-7 items-center justify-center rounded-full text-sm font-bold text-white"
          style={{ backgroundColor: active ? C.orange : C.line }}
        >
          {n}
        </span>
        <h2 className="text-lg font-bold" style={{ color: C.navy }}>{title}</h2>
      </div>
      {children}
    </div>
  );
}

/**
 * Baris petunjuk ringkas (ikon + teks) untuk state kosong/loading/error.
 *
 * @param props.icon - ikon status
 * @param props.text - teks petunjuk
 * @returns Elemen petunjuk
 */
function Hint({ icon, text }: { icon: React.ReactNode; text: string }) {
  return (
    <div
      className="flex items-center gap-2 rounded-2xl border border-dashed px-4 py-5"
      style={{ borderColor: C.line }}
    >
      {icon}
      <p className="text-sm" style={{ color: C.grey }}>{text}</p>
    </div>
  );
}
