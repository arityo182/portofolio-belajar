import { lazy, Suspense } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Layout from "../components/Layout";
import ProtectedRoute from "./ProtectedRoute";
import AdminRoute from "./AdminRoute";
import AdminLayout from "../components/admin/AdminLayout";
import DokterRoute from "./DokterRoute";
import DokterLayout from "../components/dokter/DokterLayout";

// Route-level code splitting: halaman publik, pasien, admin, dan dokter
// dimuat sesuai kebutuhan sehingga bundle awal tidak membawa seluruh aplikasi.
const Home = lazy(() => import("../pages/Home"));
const NotFound = lazy(() => import("../pages/NotFound"));
const Layanan = lazy(() => import("../pages/Layanan"));
const Kontak = lazy(() => import("../pages/Kontak"));
const TentangKami = lazy(() => import("../pages/TentangKami"));
const Radiologi = lazy(() => import("../features/screening/pages/Radiologi"));
const OsteoScreening = lazy(() => import("../features/screening/pages/OsteoScreening"));
const RiwayatSkrining = lazy(() => import("../features/screening/pages/RiwayatSkrining"));
const JadwalPraktik = lazy(() => import("../features/jadwal/pages/JadwalPraktik"));
const BookingPage = lazy(() => import("../features/appointment/pages/BookingPage"));
const AppointmentSayaPage = lazy(() => import("../features/appointment/pages/AppointmentSayaPage"));
const ProfilPasienPage = lazy(() => import("../features/pasien/pages/ProfilPasienPage"));
const RiwayatRekamMedisPage = lazy(() => import("../features/rekammedis/pages/RiwayatRekamMedisPage"));
const AdminDasbor = lazy(() => import("../features/admin/pages/AdminDasbor"));
const AdminPoli = lazy(() => import("../features/admin/pages/AdminPoli"));
const AdminDokter = lazy(() => import("../features/admin/pages/AdminDokter"));
const AdminJadwal = lazy(() => import("../features/admin/pages/AdminJadwal"));
const AdminPasien = lazy(() => import("../features/admin/pages/AdminPasien"));
const AdminAntrian = lazy(() => import("../features/admin/pages/AdminAntrian"));
const AdminPengguna = lazy(() => import("../features/admin/pages/AdminPengguna"));
const AdminObat = lazy(() => import("../features/admin/pages/AdminObat"));
const AdminLab = lazy(() => import("../features/laboratorium/pages/AdminLab"));
const AdminRadiologi = lazy(() => import("../features/radiologi/pages/AdminRadiologi"));
const AdminRawatInap = lazy(() => import("../features/rawatinap/pages/AdminRawatInap"));
const AdminOperasi = lazy(() => import("../features/jadwaloperasi/pages/AdminOperasi"));
const AdminContent = lazy(() => import("../features/admin/pages/AdminContent"));
const AdminBilling = lazy(() => import("../features/billing/pages/AdminBilling"));
const TagihanSaya = lazy(() => import("../features/billing/pages/TagihanSaya"));
const BayarTagihan = lazy(() => import("../features/pembayaran/pages/BayarTagihan"));
const ChatPage = lazy(() => import("../features/chat/pages/ChatPage"));
const DokterAntrian = lazy(() => import("../features/dokterpanel/pages/DokterAntrian"));
const DokterRekamMedis = lazy(() => import("../features/dokterpanel/pages/DokterRekamMedis"));
const Login = lazy(() => import("../features/auth/pages/Login"));
const Register = lazy(() => import("../features/auth/pages/Register"));

/** Fallback ringan saat chunk halaman sedang dimuat. */
function PageFallback() {
    return (<div className="flex min-h-[50vh] items-center justify-center">
      <div className="h-8 w-8 animate-spin rounded-full border-2 border-slate-200 border-t-slate-700"/>
    </div>);
}

export default function AppRoutes() {
    return (<BrowserRouter>
      <Suspense fallback={<PageFallback />}>
        <Routes>
          {/* Publik + pasien (pakai navbar & footer) */}
          <Route element={<Layout />}>
            <Route path="/" element={<Home />}/>
            <Route path="/layanan" element={<Layanan />}/>
            <Route path="/kontak" element={<Kontak />}/>
            <Route path="/tentang" element={<TentangKami />}/>
            <Route path="/osteoporosis" element={<Radiologi />}/>
            {/* Jadwal praktik dokter per poli (alur data booking) */}
            <Route path="/jadwal-praktik" element={<JadwalPraktik />}/>
            <Route path="/dokter" element={<JadwalPraktik />}/>
            <Route path="/login" element={<Login />}/>
            <Route path="/registration" element={<Register />}/>

            {/* Butuh login (JWT) */}
            <Route element={<ProtectedRoute />}>
              <Route path="/osteoporosis/unggah" element={<OsteoScreening />}/>
              <Route path="/osteoporosis/riwayat" element={<RiwayatSkrining />}/>
              <Route path="/booking" element={<BookingPage />}/>
              <Route path="/appointment-saya" element={<AppointmentSayaPage />}/>
              <Route path="/profil-pasien" element={<ProfilPasienPage />}/>
              <Route path="/rekam-medis" element={<RiwayatRekamMedisPage />}/>
              <Route path="/tagihan-saya" element={<TagihanSaya />}/>
              <Route path="/bayar-tagihan" element={<BayarTagihan />}/>
              <Route path="/chat" element={<ChatPage />}/>

              {/* Alias lama agar tautan /dashboard/{role} tidak berakhir 404. */}
              <Route path="/dashboard/pasien" element={<Navigate to="/appointment-saya" replace/>}/>
              <Route path="/dashboard/dokter" element={<Navigate to="/dokter-panel/antrian" replace/>}/>
              <Route path="/dashboard/admin" element={<Navigate to="/admin/dasbor" replace/>}/>
            </Route>
          </Route>

          {/* Admin (role ADMIN) — layout sendiri, tanpa navbar/footer publik */}
          <Route element={<AdminRoute />}>
            <Route element={<AdminLayout />}>
              <Route path="/admin/dasbor" element={<AdminDasbor />}/>
              <Route path="/admin/poli" element={<AdminPoli />}/>
              <Route path="/admin/dokter" element={<AdminDokter />}/>
              <Route path="/admin/jadwal" element={<AdminJadwal />}/>
              <Route path="/admin/pasien" element={<AdminPasien />}/>
              <Route path="/admin/antrian" element={<AdminAntrian />}/>
              <Route path="/admin/obat" element={<AdminObat />}/>
              <Route path="/admin/lab" element={<AdminLab />}/>
              <Route path="/admin/radiologi" element={<AdminRadiologi />}/>
              <Route path="/admin/rawat-inap" element={<AdminRawatInap />}/>
              <Route path="/admin/operasi" element={<AdminOperasi />}/>
              <Route path="/admin/content" element={<AdminContent />}/>
              <Route path="/admin/billing" element={<AdminBilling />}/>
              <Route path="/admin/pengguna" element={<AdminPengguna />}/>
            </Route>
          </Route>

          {/* Dokter (role DOKTER) — layout sendiri */}
          <Route element={<DokterRoute />}>
            <Route element={<DokterLayout />}>
              <Route path="/dokter-panel/antrian" element={<DokterAntrian />}/>
              <Route path="/dokter-panel/rekam-medis" element={<DokterRekamMedis />}/>
            </Route>
          </Route>

          {/* 404 — Catch-all */}
          <Route path="*" element={<Layout />}>
            <Route path="*" element={<NotFound />}/>
          </Route>
        </Routes>
      </Suspense>
    </BrowserRouter>);
}
