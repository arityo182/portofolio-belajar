import { BrowserRouter, Routes, Route } from "react-router-dom";
import Layout from "../components/Layout";
import ProtectedRoute from "./ProtectedRoute";
import AdminRoute from "./AdminRoute";
import AdminLayout from "../components/admin/AdminLayout";
import DokterRoute from "./DokterRoute";
import DokterLayout from "../components/dokter/DokterLayout";
import Home from "../pages/Home";
import Layanan from "../pages/Layanan";
import Kontak from "../pages/Kontak";
import TentangKami from "../pages/TentangKami";
import Radiologi from "../features/screening/pages/Radiologi";
import OsteoScreening from "../features/screening/pages/OsteoScreening";
import JadwalPraktik from "../features/jadwal/pages/JadwalPraktik";
import BookingPage from "../features/appointment/pages/BookingPage";
import AppointmentSayaPage from "../features/appointment/pages/AppointmentSayaPage";
import ProfilPasienPage from "../features/pasien/pages/ProfilPasienPage";
import RiwayatRekamMedisPage from "../features/rekammedis/pages/RiwayatRekamMedisPage";
import AdminDasbor from "../features/admin/pages/AdminDasbor";
import AdminPoli from "../features/admin/pages/AdminPoli";
import AdminDokter from "../features/admin/pages/AdminDokter";
import AdminJadwal from "../features/admin/pages/AdminJadwal";
import AdminPasien from "../features/admin/pages/AdminPasien";
import AdminAntrian from "../features/admin/pages/AdminAntrian";
import AdminPengguna from "../features/admin/pages/AdminPengguna";
import AdminObat from "../features/admin/pages/AdminObat";
import AdminLab from "../features/laboratorium/pages/AdminLab";
import AdminRadiologi from "../features/radiologi/pages/AdminRadiologi";
import AdminRawatInap from "../features/rawatinap/pages/AdminRawatInap";
import AdminOperasi from "../features/jadwaloperasi/pages/AdminOperasi";
import AdminContent from "../features/admin/pages/AdminContent";
import AdminBilling from "../features/billing/pages/AdminBilling";
import TagihanSaya from "../features/billing/pages/TagihanSaya";
import BayarTagihan from "../features/pembayaran/pages/BayarTagihan";
import DokterAntrian from "../features/dokterpanel/pages/DokterAntrian";
import DokterRekamMedis from "../features/dokterpanel/pages/DokterRekamMedis";
import Login from "../features/auth/pages/Login";
import Register from "../features/auth/pages/Register";

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Publik + pasien (pakai navbar & footer) */}
        <Route element={<Layout />}>
          <Route path="/" element={<Home />} />
          <Route path="/layanan" element={<Layanan />} />
          <Route path="/kontak" element={<Kontak />} />
          <Route path="/tentang" element={<TentangKami />} />
          <Route path="/osteoporosis" element={<Radiologi />} />
          {/* Jadwal praktik dokter per poli (alur data booking) */}
          <Route path="/jadwal-praktik" element={<JadwalPraktik />} />
          <Route path="/dokter" element={<JadwalPraktik />} />
          <Route path="/login" element={<Login />} />
          <Route path="/registration" element={<Register />} />

          {/* Butuh login (JWT) */}
          <Route element={<ProtectedRoute />}>
            <Route path="/osteoporosis/unggah" element={<OsteoScreening />} />
            <Route path="/booking" element={<BookingPage />} />
            <Route path="/appointment-saya" element={<AppointmentSayaPage />} />
            <Route path="/profil-pasien" element={<ProfilPasienPage />} />
            <Route path="/rekam-medis" element={<RiwayatRekamMedisPage />} />
            <Route path="/tagihan-saya" element={<TagihanSaya />} />
            <Route path="/bayar-tagihan" element={<BayarTagihan />} />
          </Route>
        </Route>

        {/* Admin (role ADMIN) — layout sendiri, tanpa navbar/footer publik */}
        <Route element={<AdminRoute />}>
          <Route element={<AdminLayout />}>
            <Route path="/admin/dasbor" element={<AdminDasbor />} />
            <Route path="/admin/poli" element={<AdminPoli />} />
            <Route path="/admin/dokter" element={<AdminDokter />} />
            <Route path="/admin/jadwal" element={<AdminJadwal />} />
            <Route path="/admin/pasien" element={<AdminPasien />} />
            <Route path="/admin/antrian" element={<AdminAntrian />} />
            <Route path="/admin/obat" element={<AdminObat />} />
            <Route path="/admin/lab" element={<AdminLab />} />
            <Route path="/admin/radiologi" element={<AdminRadiologi />} />
            <Route path="/admin/rawat-inap" element={<AdminRawatInap />} />
            <Route path="/admin/operasi" element={<AdminOperasi />} />
            <Route path="/admin/content" element={<AdminContent />} />
            <Route path="/admin/billing" element={<AdminBilling />} />
            <Route path="/admin/pengguna" element={<AdminPengguna />} />
          </Route>
        </Route>

        {/* Dokter (role DOKTER) — layout sendiri */}
        <Route element={<DokterRoute />}>
          <Route element={<DokterLayout />}>
            <Route path="/dokter-panel/antrian" element={<DokterAntrian />} />
            <Route path="/dokter-panel/rekam-medis" element={<DokterRekamMedis />} />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
