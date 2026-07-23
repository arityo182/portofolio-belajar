/**
 * @module core/types
 *
 * Definisi tipe data inti yang dipakai lintas fitur.
 */

/**
 * Merepresentasikan data pengguna yang terautentikasi.
 */
export interface User {
  /** ID unik pengguna dari database */
  id: number;
  /** Nama lengkap pengguna */
  nama: string;
  /** Email pengguna (identitas login) */
  email: string;
  /** Peran/role pengguna (mis. "USER", "ADMIN") untuk kontrol akses */
  role: string;
}
