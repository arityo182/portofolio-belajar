import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../core/context/AuthContext";

/**
 * Route guard untuk halaman yang butuh autentikasi.
 *
 * Bila pengguna terautentikasi, merender child route lewat `<Outlet />`;
 * jika tidak, mengalihkan ke `/login` (mengganti entri history, bukan
 * menambah). Dipakai sebagai element pembungkus pada konfigurasi router.
 *
 * @returns `<Outlet />` bila terautentikasi, atau redirect ke `/login`
 *
 * @example
 * <Route element={<ProtectedRoute />}>
 *   <Route path="/osteoporosis/unggah" element={<OsteoScreening />} />
 * </Route>
 */
export default function ProtectedRoute() {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
}
