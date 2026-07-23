import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../core/context/AuthContext";

/**
 * Route guard untuk halaman dokter (FR-04).
 *
 * Hanya mengizinkan akses bila user terautentikasi DAN berperan DOKTER.
 * Bila belum login → redirect ke `/login`. Bila login tapi bukan dokter →
 * redirect ke `/`.
 *
 * @returns `<Outlet />` bila dokter, atau redirect
 */
export default function DokterRoute() {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  if (user?.role !== "DOKTER") {
    return <Navigate to="/" replace />;
  }
  return <Outlet />;
}
