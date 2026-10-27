import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../core/context/AuthContext";

/**
 * Route guard untuk halaman admin (FR-04).
 *
 * Hanya mengizinkan akses bila user terautentikasi DAN berperan ADMIN.
 * Catatan: ini HANYA guard UX; otorisasi sebenarnya ada di backend.
 */
export default function AdminRoute() {
    const { isAuthenticated, user, loading } = useAuth();
    if (loading) {
        return <div className="flex min-h-screen items-center justify-center text-slate-500">Memuat sesi…</div>;
    }
    if (!isAuthenticated) {
        return <Navigate to="/login" replace/>;
    }
    if (user?.role !== "ADMIN") {
        return <Navigate to="/" replace/>;
    }
    return <Outlet />;
}
