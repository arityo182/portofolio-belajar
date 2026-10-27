import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../core/context/AuthContext";

/**
 * Route guard untuk halaman dokter (FR-04).
 *
 * Hanya mengizinkan akses bila user terautentikasi DAN berperan DOKTER.
 */
export default function DokterRoute() {
    const { isAuthenticated, user, loading } = useAuth();
    if (loading) {
        return <div className="flex min-h-screen items-center justify-center text-slate-500">Memuat sesi…</div>;
    }
    if (!isAuthenticated) {
        return <Navigate to="/login" replace/>;
    }
    if (user?.role !== "DOKTER") {
        return <Navigate to="/" replace/>;
    }
    return <Outlet />;
}
