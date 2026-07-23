import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../core/context/AuthContext";

/**
 * Route guard untuk halaman admin (FR-04).
 *
 * Hanya mengizinkan akses bila user terautentikasi DAN berperan ADMIN.
 * Bila belum login → redirect ke `/login`. Bila login tapi bukan admin →
 * redirect ke `/` (beranda) dengan indikasi akses ditolak.
 *
 * Catatan: ini HANYA guard UX di sisi frontend. Keamanan sebenarnya tetap
 * di backend (`/api/admin/**` dibatasi `hasRole('ADMIN')`).
 *
 * @returns `<Outlet />` bila admin, atau redirect
 */
export default function AdminRoute() {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  if (user?.role !== "ADMIN") {
    return <Navigate to="/" replace />;
  }
  return <Outlet />;
}
