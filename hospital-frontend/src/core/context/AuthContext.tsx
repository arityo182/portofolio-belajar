/**
 * @module core/context/AuthContext
 *
 * Context autentikasi global. Menyimpan sesi user (token + data user)
 * dengan persistensi ke `localStorage`, sehingga sesi bertahan setelah
 * refresh halaman. Sediakan {@link AuthProvider} di root aplikasi dan
 * akses state/aksinya lewat hook {@link useAuth}.
 */
import { createContext, useContext, useState } from "react";
import type { ReactNode } from "react";
import type { User } from "../types";

/**
 * Bentuk nilai yang disediakan oleh {@link AuthContext}.
 */
interface AuthContextType {
  /** User yang sedang login, atau `null` bila belum login */
  user: User | null;
  /** JWT token aktif, atau `null` bila belum login */
  token: string | null;
  /** `true` jika terdapat token aktif (user dianggap terautentikasi) */
  isAuthenticated: boolean;
  /** Menyimpan sesi login (token + user) ke state dan localStorage */
  login: (token: string, user: User) => void;
  /** Menghapus sesi login dari state dan localStorage */
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * Provider yang membungkus aplikasi dan menyediakan state autentikasi.
 *
 * Nilai awal `token` dan `user` diinisialisasi (lazy) dari `localStorage`
 * agar sesi tetap ada setelah halaman di-reload.
 *
 * @param props.children - subtree React yang boleh mengakses context auth
 * @returns Provider yang membungkus `children`
 *
 * @example
 * <AuthProvider>
 *   <App />
 * </AuthProvider>
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() =>
    localStorage.getItem("token")
  );
  const [user, setUser] = useState<User | null>(() => {
    const raw = localStorage.getItem("user");
    return raw ? (JSON.parse(raw) as User) : null;
  });

  const login = (newToken: string, newUser: User) => {
    localStorage.setItem("token", newToken);
    localStorage.setItem("user", JSON.stringify(newUser));
    setToken(newToken);
    setUser(newUser);
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{ user, token, isAuthenticated: !!token, login, logout }}
    >
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
 * @returns Nilai {@link AuthContextType} (user, token, isAuthenticated, login, logout)
 * @throws Error - jika dipakai di luar {@link AuthProvider}
 *
 * @example
 * const { isAuthenticated, login, logout } = useAuth();
 */
// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth harus dipakai di dalam AuthProvider");
  return ctx;
}
