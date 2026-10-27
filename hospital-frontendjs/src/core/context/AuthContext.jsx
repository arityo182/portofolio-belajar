/**
 * @module core/context/AuthContext
 *
 * Context autentikasi global. Access token disimpan hanya di memori; sesi
 * dipulihkan saat halaman dimuat dengan menukar refresh token HttpOnly
 * (cookie) menjadi access token baru. Tidak ada token yang ditulis ke
 * `localStorage`, sehingga XSS tidak dapat mencuri sesi.
 */
import { createContext, useCallback, useContext, useEffect, useState } from "react";
import api, {
    AUTH_EXPIRED_EVENT,
    refreshSession,
    setAccessToken,
} from "../api/client";

const AuthContext = createContext(undefined);

/**
 * Provider yang membungkus aplikasi dan menyediakan state autentikasi.
 *
 * Selama `loading` bernilai true, route guard menahan render agar tidak
 * terlanjur redirect ke /login sebelum percobaan refresh selesai.
 *
 * @param props.children - subtree React yang boleh mengakses context auth
 * @returns Provider yang membungkus `children`
 *
 * @example
 * <AuthProvider>
 *   <App />
 * </AuthProvider>
 */
export function AuthProvider({ children }) {
    const [token, setToken] = useState(null);
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    const clearSession = useCallback(() => {
        setAccessToken(null);
        setToken(null);
        setUser(null);
    }, []);

    // Pulihkan sesi dari refresh cookie saat aplikasi dimuat / di-reload.
    useEffect(() => {
        let active = true;

        refreshSession()
            .then((session) => {
                if (!active)
                    return;
                setToken(session.token);
                setUser(session.user);
            })
            .catch(() => {
                if (active)
                    clearSession();
            })
            .finally(() => {
                if (active)
                    setLoading(false);
            });

        const onExpired = () => clearSession();
        window.addEventListener(AUTH_EXPIRED_EVENT, onExpired);
        return () => {
            active = false;
            window.removeEventListener(AUTH_EXPIRED_EVENT, onExpired);
        };
    }, [clearSession]);

    const login = (newToken, newUser) => {
        setAccessToken(newToken);
        setToken(newToken);
        setUser(newUser);
        setLoading(false);
    };

    const logout = async () => {
        try {
            await api.post("/auth/logout", null, { _skipAuthRefresh: true });
        } catch {
            // Tetap bersihkan state lokal walau server tidak terjangkau.
        } finally {
            clearSession();
        }
    };

    return (
        <AuthContext.Provider value={{ user, token, loading, isAuthenticated: !!token, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

/**
 * Hook untuk mengakses state dan aksi autentikasi.
 *
 * Harus dipanggil di dalam pohon komponen yang dibungkus
 * {@link AuthProvider}, jika tidak akan melempar error.
 *
 * @returns Nilai context (user, token, loading, isAuthenticated, login, logout)
 * @throws Error - jika dipakai di luar {@link AuthProvider}
 *
 * @example
 * const { isAuthenticated, login, logout } = useAuth();
 */
// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx)
        throw new Error("useAuth harus dipakai di dalam AuthProvider");
    return ctx;
}
