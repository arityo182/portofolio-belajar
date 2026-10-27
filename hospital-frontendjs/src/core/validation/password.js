/**
 * Kebijakan password yang dipakai konsisten di seluruh form frontend.
 * Backend menolak password < 12 karakter atau tanpa kombinasi huruf & angka,
 * jadi validasi ini mencegah request yang pasti gagal.
 */

/** Teks aturan untuk placeholder/help text. */
export const PASSWORD_RULE_TEXT = "Minimal 12 karakter, kombinasi huruf dan angka";

/**
 * Memvalidasi kekuatan password.
 *
 * @param {string} password password yang akan dicek
 * @returns {string|null} pesan kesalahan, atau null bila valid
 */
export function validatePassword(password) {
    if (!password) {
        return "Password wajib diisi.";
    }
    if (password.length < 12) {
        return "Password minimal 12 karakter.";
    }
    if (!/[A-Za-z]/.test(password) || !/\d/.test(password)) {
        return "Password harus mengandung huruf dan angka.";
    }
    return null;
}
