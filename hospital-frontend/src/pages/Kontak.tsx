import { Link } from "react-router-dom";
import {
  MapPin, Phone, Mail, Clock, ChevronRight, HeartPulse,
} from "lucide-react";
import { FaInstagram, FaFacebook, FaYoutube } from "react-icons/fa";
import type { IconType } from "react-icons";
import { C, RS_NAME } from "../core/theme";

/** Item kontak (ikon + label + nilai + tautan opsional). */
interface ItemKontak {
  icon: typeof MapPin;
  label: string;
  nilai: string;
  href?: string;
}

/** Satu baris jam operasional. */
interface JamOperasional {
  hari: string;
  jam: string;
}

/** Daftar kontak utama RS. */
const KONTAK: ItemKontak[] = [
  {
    icon: MapPin,
    label: "Alamat",
    nilai: "Jl. Sehat No. 123, Jakarta",
    href: "https://maps.google.com/?q=Jl.+Sehat+No.+123+Jakarta",
  },
  {
    icon: Phone,
    label: "Telepon",
    nilai: "(021) 1234-5678",
    href: "tel:02112345678",
  },
  {
    icon: Mail,
    label: "Email",
    nilai: "info@medikasentosa.id",
    href: "mailto:info@medikasentosa.id",
  },
];

/** Jadwal jam operasional per unit. */
const JAM: JamOperasional[] = [
  { hari: "Senin – Jumat", jam: "08.00 – 21.00 WIB" },
  { hari: "Sabtu", jam: "08.00 – 17.00 WIB" },
  { hari: "Minggu & Hari Libur", jam: "09.00 – 14.00 WIB" },
  { hari: "IGD", jam: "24 jam (setiap hari)" },
];

/** Sosial media RS. */
const SOSIAL: IconType[] = [FaInstagram, FaFacebook, FaYoutube];

/**
 * @module pages/Kontak
 *
 * Halaman kontak RS Medika Sentosa (informasi statis).
 *
 * Menampilkan alamat, telepon, email, jam operasional, peta lokasi (Google
 * Maps embed), dan tautan sosial media. Info konsisten dengan footer.
 * Form pesan ditunda ke Fase 2 (membutuhkan backend penyimpanan pesan).
 *
 * @returns Halaman kontak
 */
export default function Kontak() {
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
            <Phone size={14} /> Kontak Kami
          </span>
          <h1 className="mt-5 text-4xl font-bold leading-tight text-white md:text-5xl">
            Hubungi {RS_NAME}
          </h1>
          <p className="mt-4 max-w-2xl text-base leading-relaxed" style={{ color: "#C9D2EC" }}>
            Kami siap membantu Anda. Hubungi kami untuk informasi layanan, janji
            temu, atau pertanyaan seputar kesehatan.
          </p>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-5 py-14">
        <nav className="mb-8 flex items-center gap-1.5 text-sm" aria-label="Breadcrumb">
          <Link to="/" className="transition-opacity hover:opacity-70" style={{ color: C.grey }}>
            Beranda
          </Link>
          <ChevronRight size={15} color={C.grey} />
          <span className="font-semibold" style={{ color: C.navy }}>Kontak</span>
        </nav>

        <div className="grid gap-8 lg:grid-cols-2">
          {/* Kolom kiri: info kontak + jam operasional */}
          <div className="flex flex-col gap-6">
            {/* Kartu info kontak */}
            <div
              className="rounded-3xl border p-6 md:p-8"
              style={{ backgroundColor: C.white, borderColor: C.line }}
            >
              <h2 className="text-lg font-bold" style={{ color: C.navy }}>Informasi Kontak</h2>
              <ul className="mt-5 flex flex-col gap-5">
                {KONTAK.map(({ icon: Icon, label, nilai, href }) => (
                  <li key={label} className="flex items-start gap-4">
                    <div
                      className="flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-2xl"
                      style={{ backgroundColor: C.blueSoft }}
                    >
                      <Icon size={20} color={C.navy} />
                    </div>
                    <div>
                      <p className="text-xs font-semibold uppercase tracking-wide" style={{ color: C.grey }}>
                        {label}
                      </p>
                      {href ? (
                        <a
                          href={href}
                          target={href.startsWith("http") ? "_blank" : undefined}
                          rel="noopener noreferrer"
                          className="mt-0.5 block text-sm font-semibold transition-opacity hover:opacity-70"
                          style={{ color: C.navy }}
                        >
                          {nilai}
                        </a>
                      ) : (
                        <p className="mt-0.5 text-sm font-semibold" style={{ color: C.navy }}>
                          {nilai}
                        </p>
                      )}
                    </div>
                  </li>
                ))}
              </ul>

              {/* Sosial media */}
              <div className="mt-6 border-t pt-5" style={{ borderColor: C.line }}>
                <p className="text-xs font-semibold uppercase tracking-wide" style={{ color: C.grey }}>
                  Ikuti Kami
                </p>
                <div className="mt-3 flex gap-3">
                  {SOSIAL.map((Icon, i) => (
                    <a
                      key={i}
                      href="#"
                      aria-label="sosial media"
                      className="flex h-10 w-10 items-center justify-center rounded-full transition-transform hover:-translate-y-0.5"
                      style={{ backgroundColor: C.cream }}
                    >
                      <Icon size={18} color={C.navy} />
                    </a>
                  ))}
                </div>
              </div>
            </div>

            {/* Kartu jam operasional */}
            <div
              className="rounded-3xl border p-6 md:p-8"
              style={{ backgroundColor: C.white, borderColor: C.line }}
            >
              <div className="flex items-center gap-3">
                <div
                  className="flex h-11 w-11 items-center justify-center rounded-2xl"
                  style={{ backgroundColor: C.orangeSoft }}
                >
                  <Clock size={20} color={C.orange} />
                </div>
                <h2 className="text-lg font-bold" style={{ color: C.navy }}>Jam Operasional</h2>
              </div>
              <ul className="mt-5 flex flex-col gap-3">
                {JAM.map((j) => (
                  <li
                    key={j.hari}
                    className="flex items-center justify-between border-b pb-3 last:border-b-0 last:pb-0"
                    style={{ borderColor: C.line }}
                  >
                    <span className="text-sm font-medium" style={{ color: C.grey }}>
                      {j.hari}
                    </span>
                    <span className="text-sm font-semibold" style={{ color: C.navy }}>
                      {j.jam}
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          </div>

          {/* Kolom kanan: peta lokasi */}
          <div
            className="overflow-hidden rounded-3xl border"
            style={{ borderColor: C.line, minHeight: "420px" }}
          >
            <iframe
              title="Peta lokasi RS Medika Sentosa"
              src="https://www.google.com/maps?q=Jl.+Sehat+No.+123+Jakarta&output=embed"
              width="100%"
              height="100%"
              style={{ border: 0, minHeight: "420px" }}
              loading="lazy"
              referrerPolicy="no-referrer-when-downgrade"
              allowFullScreen
            />
          </div>
        </div>

        {/* CTA bawah */}
        <div
          className="mt-10 flex flex-col items-center gap-4 rounded-3xl px-6 py-10 text-center md:flex-row md:justify-between md:text-left"
          style={{ backgroundColor: C.navy }}
        >
          <div className="flex items-center gap-4">
            <div
              className="flex h-14 w-14 flex-shrink-0 items-center justify-center rounded-full"
              style={{ backgroundColor: C.orange }}
            >
              <HeartPulse size={26} color={C.white} />
            </div>
            <div>
              <h2 className="text-xl font-bold text-white">Pelayanan 24 jam</h2>
              <p className="mt-1 text-sm" style={{ color: "#C9D2EC" }}>
                Unit Gawat Darurat (IGD) siap melayani Anda sepanjang hari.
              </p>
            </div>
          </div>
          <a
            href="tel:02112345678"
            className="inline-flex items-center gap-2 rounded-full px-7 py-3.5 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5"
            style={{ backgroundColor: C.orange }}
          >
            <Phone size={17} /> Hubungi IGD
          </a>
        </div>
      </section>
    </div>
  );
}