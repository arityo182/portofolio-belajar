import { useEffect, useState } from "react";
import { Stethoscope, Loader2, AlertTriangle, CalendarDays, ChevronRight } from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import { getAllPoli } from "../../poli/services/poliService";
import type { Poli } from "../../poli/types";
import PoliCard from "../../poli/components/PoliCard";
import { getDokterByPoli } from "../../dokter/services/dokterService";
import type { Dokter } from "../../dokter/types";
import DokterCard from "../../dokter/components/DokterCard";
import { getJadwalByDokter } from "../services/jadwalService";
import type { JadwalDokter } from "../types";
import JadwalList from "../components/JadwalList";

/**
 * Blok status ringkas (loading / error / kosong) untuk tiap kolom.
 */
function StatusBlock({ icon, text }: { icon: React.ReactNode; text: string }) {
  return (
    <div
      className="flex flex-col items-center justify-center gap-2 rounded-3xl border border-dashed px-4 py-10 text-center"
      style={{ borderColor: C.line }}
    >
      {icon}
      <p className="text-sm" style={{ color: C.grey }}>{text}</p>
    </div>
  );
}

/**
 * Halaman "Jadwal Praktik Dokter" (FR-08).
 *
 * Menyajikan alur drill-down untuk data booking: pilih poli → lihat dokter
 * pada poli itu → lihat jadwal praktik dokter (hari, jam, kuota, status).
 * Setiap kolom menangani state loading dan error secara mandiri.
 *
 * Data diambil via service layer masing-masing fitur (poli/dokter/jadwal)
 * yang menggunakan API client ber-JWT terpusat. Halaman ini murni untuk
 * MENAMPILKAN data; aksi booking ditangani fitur appointment berikutnya.
 *
 * @returns Halaman jadwal praktik dokter
 */
export default function JadwalPraktik() {
  // --- Kolom 1: Poli ---
  const [poliList, setPoliList] = useState<Poli[]>([]);
  const [poliLoading, setPoliLoading] = useState(true);
  const [poliError, setPoliError] = useState<string | null>(null);
  const [selectedPoliId, setSelectedPoliId] = useState<number | null>(null);

  // --- Kolom 2: Dokter ---
  const [dokterList, setDokterList] = useState<Dokter[]>([]);
  const [dokterLoading, setDokterLoading] = useState(false);
  const [dokterError, setDokterError] = useState<string | null>(null);
  const [selectedDokterId, setSelectedDokterId] = useState<number | null>(null);

  // --- Kolom 3: Jadwal ---
  const [jadwalList, setJadwalList] = useState<JadwalDokter[]>([]);
  const [jadwalLoading, setJadwalLoading] = useState(false);
  const [jadwalError, setJadwalError] = useState<string | null>(null);

  // Muat daftar poli saat halaman pertama kali dirender.
  useEffect(() => {
    let aktif = true;
    setPoliLoading(true);
    setPoliError(null);
    getAllPoli()
      .then((res) => {
        if (!aktif) return;
        // Tampilkan hanya poli yang aktif untuk keperluan booking.
        setPoliList(res.data.filter((p) => p.isActive));
      })
      .catch((err) => aktif && setPoliError(toErrorMessage(err)))
      .finally(() => aktif && setPoliLoading(false));
    return () => {
      aktif = false;
    };
  }, []);

  // Muat dokter setiap kali poli terpilih berubah; reset pilihan dokter & jadwal.
  useEffect(() => {
    if (selectedPoliId === null) return;
    let aktif = true;
    setDokterLoading(true);
    setDokterError(null);
    setDokterList([]);
    setSelectedDokterId(null);
    setJadwalList([]);
    setJadwalError(null);
    getDokterByPoli(selectedPoliId)
      .then((res) => aktif && setDokterList(res.data))
      .catch((err) => aktif && setDokterError(toErrorMessage(err)))
      .finally(() => aktif && setDokterLoading(false));
    return () => {
      aktif = false;
    };
  }, [selectedPoliId]);

  // Muat jadwal setiap kali dokter terpilih berubah.
  useEffect(() => {
    if (selectedDokterId === null) return;
    let aktif = true;
    setJadwalLoading(true);
    setJadwalError(null);
    setJadwalList([]);
    getJadwalByDokter(selectedDokterId)
      .then((res) => aktif && setJadwalList(res.data))
      .catch((err) => aktif && setJadwalError(toErrorMessage(err)))
      .finally(() => aktif && setJadwalLoading(false));
    return () => {
      aktif = false;
    };
  }, [selectedDokterId]);

  const spinner = <Loader2 className="animate-spin" size={22} color={C.navy} />;
  const warn = <AlertTriangle size={22} color={C.orange} />;

  return (
    <div>
      {/* Hero */}
      <section style={{ backgroundColor: C.navy }} className="relative overflow-hidden">
        <div
          className="pointer-events-none absolute -right-20 -top-16 h-64 w-64 rounded-full opacity-15"
          style={{ backgroundColor: C.blue }}
        />
        <div className="relative mx-auto max-w-6xl px-5 py-14 md:py-16">
          <span
            className="inline-flex items-center gap-2 rounded-full px-3.5 py-1.5 text-xs font-semibold"
            style={{ backgroundColor: "rgba(255,255,255,0.12)", color: C.blue }}
          >
            <CalendarDays size={14} /> Jadwal Praktik
          </span>
          <h1 className="mt-5 max-w-2xl text-4xl font-bold leading-tight text-white md:text-5xl">
            Jadwal praktik dokter per poli
          </h1>
          <p className="mt-4 max-w-xl text-base leading-relaxed" style={{ color: "#C9D2EC" }}>
            Pilih poli, lihat dokter yang bertugas, lalu tinjau hari, jam, dan kuota
            praktiknya sebelum membuat janji temu.
          </p>
        </div>
      </section>

      {/* Alur drill-down */}
      <section className="mx-auto max-w-6xl px-5 py-12">
        <div className="grid gap-6 lg:grid-cols-3">
          {/* Kolom 1 — Poli */}
          <div>
            <div className="mb-4 flex items-center gap-2">
              <span
                className="flex h-7 w-7 items-center justify-center rounded-full text-sm font-bold text-white"
                style={{ backgroundColor: C.orange }}
              >
                1
              </span>
              <h2 className="text-lg font-bold" style={{ color: C.navy }}>Pilih Poli</h2>
            </div>
            {poliLoading ? (
              <StatusBlock icon={spinner} text="Memuat daftar poli..." />
            ) : poliError ? (
              <StatusBlock icon={warn} text={poliError} />
            ) : poliList.length === 0 ? (
              <StatusBlock icon={warn} text="Belum ada poli aktif." />
            ) : (
              <div className="flex flex-col gap-3">
                {poliList.map((p) => (
                  <PoliCard
                    key={p.id}
                    poli={p}
                    selected={p.id === selectedPoliId}
                    onSelect={setSelectedPoliId}
                  />
                ))}
              </div>
            )}
          </div>

          {/* Kolom 2 — Dokter */}
          <div>
            <div className="mb-4 flex items-center gap-2">
              <span
                className="flex h-7 w-7 items-center justify-center rounded-full text-sm font-bold text-white"
                style={{ backgroundColor: selectedPoliId ? C.orange : C.line }}
              >
                2
              </span>
              <h2 className="text-lg font-bold" style={{ color: C.navy }}>Pilih Dokter</h2>
            </div>
            {selectedPoliId === null ? (
              <StatusBlock icon={<ChevronRight size={22} color={C.grey} />} text="Pilih poli terlebih dahulu." />
            ) : dokterLoading ? (
              <StatusBlock icon={spinner} text="Memuat dokter..." />
            ) : dokterError ? (
              <StatusBlock icon={warn} text={dokterError} />
            ) : dokterList.length === 0 ? (
              <StatusBlock icon={warn} text="Belum ada dokter di poli ini." />
            ) : (
              <div className="flex flex-col gap-3">
                {dokterList.map((d) => (
                  <DokterCard
                    key={d.id}
                    dokter={d}
                    selected={d.id === selectedDokterId}
                    onSelect={setSelectedDokterId}
                  />
                ))}
              </div>
            )}
          </div>

          {/* Kolom 3 — Jadwal */}
          <div>
            <div className="mb-4 flex items-center gap-2">
              <span
                className="flex h-7 w-7 items-center justify-center rounded-full text-sm font-bold text-white"
                style={{ backgroundColor: selectedDokterId ? C.orange : C.line }}
              >
                3
              </span>
              <h2 className="text-lg font-bold" style={{ color: C.navy }}>Jadwal Praktik</h2>
            </div>
            {selectedDokterId === null ? (
              <StatusBlock icon={<Stethoscope size={22} color={C.grey} />} text="Pilih dokter untuk melihat jadwal." />
            ) : jadwalLoading ? (
              <StatusBlock icon={spinner} text="Memuat jadwal..." />
            ) : jadwalError ? (
              <StatusBlock icon={warn} text={jadwalError} />
            ) : jadwalList.length === 0 ? (
              <StatusBlock icon={warn} text="Dokter ini belum punya jadwal aktif." />
            ) : (
              <JadwalList jadwal={jadwalList} />
            )}
          </div>
        </div>
      </section>
    </div>
  );
}
