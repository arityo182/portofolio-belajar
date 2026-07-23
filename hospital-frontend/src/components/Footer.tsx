import { HeartPulse, MapPin, Phone, Mail } from "lucide-react";
import { FaInstagram, FaFacebook, FaYoutube } from "react-icons/fa";
import type { IconType } from "react-icons";
import { C, RS_NAME } from "../core/theme";

interface FooterColumn {
  h: string;
  items: string[];
}

export default function Footer() {
  const columns: FooterColumn[] = [
    { h: "Layanan", items: ["Poli Umum", "Radiologi", "Laboratorium", "Skrining AI"] },
    { h: "Perusahaan", items: ["Tentang Kami", "Tim Dokter", "Karier", "Berita"] },
  ];

  const socials: IconType[] = [FaInstagram, FaFacebook, FaYoutube];

  return (
    <footer style={{ backgroundColor: C.navyDark }}>
      <div className="mx-auto max-w-6xl px-5 py-14">
        <div className="grid gap-10 md:grid-cols-4">
          <div>
            <div className="flex items-center gap-2.5">
              <div
                className="flex h-10 w-10 items-center justify-center rounded-full"
                style={{ backgroundColor: C.orange }}
              >
                <HeartPulse size={20} color={C.white} />
              </div>
              <div className="leading-tight">
                <p className="text-base font-bold text-white">MEDIKA</p>
                <p className="text-[10px] font-medium tracking-[0.2em]" style={{ color: C.orange }}>
                  SENTOSA
                </p>
              </div>
            </div>
            <p className="mt-4 text-sm leading-relaxed" style={{ color: "#9FABCF" }}>
              Rumah sakit modern dengan layanan terintegrasi dan teknologi AI
              untuk kesehatan Anda.
            </p>
            <div className="mt-4 flex gap-3">
              {socials.map((Icon, i) => (
                <a
                  key={i}
                  href="#"
                  className="flex h-9 w-9 items-center justify-center rounded-full transition-opacity hover:opacity-70"
                  style={{ backgroundColor: "rgba(255,255,255,0.1)" }}
                >
                  <Icon size={17} color={C.white} />
                </a>
              ))}
            </div>
          </div>

          {columns.map((col) => (
            <div key={col.h}>
              <p className="text-sm font-bold text-white">{col.h}</p>
              <ul className="mt-4 space-y-2.5">
                {col.items.map((it) => (
                  <li key={it}>
                    <a
                      href="#"
                      className="text-sm transition-colors hover:text-white"
                      style={{ color: "#9FABCF" }}
                    >
                      {it}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}

          <div>
            <p className="text-sm font-bold text-white">Kontak</p>
            <ul className="mt-4 space-y-3">
              <li className="flex items-start gap-2.5">
                <MapPin size={17} color={C.orange} className="mt-0.5 flex-shrink-0" />
                <span className="text-sm" style={{ color: "#9FABCF" }}>
                  Jl. Sehat No. 123, Jakarta
                </span>
              </li>
              <li className="flex items-center gap-2.5">
                <Phone size={17} color={C.orange} className="flex-shrink-0" />
                <span className="text-sm" style={{ color: "#9FABCF" }}>(021) 1234-5678</span>
              </li>
              <li className="flex items-center gap-2.5">
                <Mail size={17} color={C.orange} className="flex-shrink-0" />
                <span className="text-sm" style={{ color: "#9FABCF" }}>info@medikasentosa.id</span>
              </li>
            </ul>
          </div>
        </div>

        <div
          className="mt-10 flex flex-col items-center justify-between gap-3 border-t pt-6 md:flex-row"
          style={{ borderColor: "rgba(255,255,255,0.1)" }}
        >
          <p className="text-xs" style={{ color: "#9FABCF" }}>
            © 2026 {RS_NAME}. Hak cipta dilindungi.
          </p>
          <div className="flex gap-5">
            <a href="#" className="text-xs" style={{ color: "#9FABCF" }}>Kebijakan Privasi</a>
            <a href="#" className="text-xs" style={{ color: "#9FABCF" }}>Syarat &amp; Ketentuan</a>
          </div>
        </div>
      </div>
    </footer>
  );
}
