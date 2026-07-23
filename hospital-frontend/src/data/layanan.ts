/**
 * @module data/layanan
 *
 * Sumber data tunggal untuk daftar layanan RS Medika Sentosa.
 * Dipakai bersama oleh halaman Beranda (section Services) dan halaman
 * Layanan (`/layanan`) agar konten selalu sinkron antar halaman.
 */
import {
  Stethoscope, Bone, HeartPulse, Microscope, Syringe, Activity,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";

/**
 * Satu item layanan untuk ditampilkan pada kartu layanan.
 */
export interface Layanan {
  /** Ikon lucide untuk layanan */
  icon: LucideIcon;
  /** Nama layanan */
  nama: string;
  /** Deskripsi singkat layanan */
  deskripsi: string;
  /** `true` bila layanan unggulan (kartu gelap + badge "AI" di Home) */
  unggulan?: boolean;
  /** Deskripsi panjang untuk halaman detail layanan */
  detail: string;
}

/**
 * Daftar seluruh layanan RS Medika Sentosa.
 * Urutan ini menentukan tampilan di Beranda & halaman Layanan.
 */
export const DAFTAR_LAYANAN: Layanan[] = [
  {
    icon: Stethoscope,
    nama: "Poli Umum",
    deskripsi: "Pemeriksaan & konsultasi dokter umum setiap hari.",
    detail:
      "Poli Umum menyediakan layanan pemeriksaan kesehatan dasar dan konsultasi " +
      "untuk seluruh anggota keluarga. Ditangani oleh dokter umum berpengalaman, " +
      "buka setiap hari termasuk akhir pekan.",
  },
  {
    icon: Bone,
    nama: "Radiologi & Osteoporosis",
    deskripsi: "Skrining tulang dengan bantuan analisis AI.",
    unggulan: true,
    detail:
      "Layanan unggulan kami: skrining osteoporosis dari citra X-ray menggunakan " +
      "model deep learning (EfficientNetV2) yang memprediksi tiga kelas " +
      "(Normal, Osteopenia, Osteoporosis) beserta visualisasi Grad-CAM area " +
      "tulang acuan. Hasil tersedia cepat dengan tingkat keyakinan prediksi.",
  },
  {
    icon: HeartPulse,
    nama: "Poli Jantung",
    deskripsi: "Penanganan kesehatan jantung oleh spesialis.",
    detail:
      "Poli Jantung menangani pemeriksaan dan penatalaksanaan gangguan " +
      "kardiovaskular oleh dokter spesialis jantung. Meliputi EKG, echocardiography, " +
      "dan konsultasi rutin untuk pasien hipertensi maupun pasca serangan jantung.",
  },
  {
    icon: Microscope,
    nama: "Laboratorium",
    deskripsi: "Cek darah & uji laboratorium lengkap akurat.",
    detail:
      "Laboratorium kami menyediakan pemeriksaan darah, urin, dan uji laboratorium " +
      "lainnya dengan peralatan modern dan akreditasi terjamin. Hasil dapat " +
      "diakses digital oleh pasien.",
  },
  {
    icon: Syringe,
    nama: "Vaksinasi",
    deskripsi: "Imunisasi anak hingga dewasa terjadwal.",
    detail:
      "Layanan vaksinasi mencakup imunisasi rutin anak, vaksin dewasa, dan " +
      "vaksin tambahan (influenza, HPV, dll). Diberikan oleh perawat terlatih " +
      "dengan jadwal yang dapat dipesan sebelumnya.",
  },
  {
    icon: Activity,
    nama: "Medical Check-Up",
    deskripsi: "Paket pemeriksaan kesehatan menyeluruh.",
    detail:
      "Medical Check-Up menawarkan paket pemeriksaan kesehatan menyeluruh " +
      "sesuai kebutuhan (individu, prakerja, perusahaan). Hasil dirangkum dalam " +
      "laporan lengkap dengan rekomendasi tindak lanjut dari dokter.",
  },
];
