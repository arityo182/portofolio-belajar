import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../core/context/AuthContext";

/**
 * Route guard untuk halaman yang butuh autentikasi.
 *
 * Menunggu pemulihan sesi (refresh cookie) selesai sebelum memutuskan,
 * agar reload halaman tidak salah redirect ke /login.
 */
export default function ProtectedRoute() {
    const { isAuthenticated, loading } = useAuth();
    if (loading) {
        return <div className="flex min-h-screen items-center justify-center text-slate-500">Memuat sesi…</div>;
    }
    return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace/>;
}
