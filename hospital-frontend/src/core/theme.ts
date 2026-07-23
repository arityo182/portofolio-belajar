/**
 * @module core/theme
 *
 * Token tema global (palet warna) dan konstanta identitas RS yang dipakai
 * untuk styling inline di seluruh aplikasi.
 */

/** Palet warna brand RS Medika Sentosa (readonly). */
export const C = {
  navy: "#172E74",
  navyDark: "#0F1F52",
  orange: "#EF7A00",
  orangeSoft: "#FDEBD7",
  blue: "#3EAFEF",
  blueSoft: "#E4F4FD",
  cream: "#F8F8EF",
  grey: "#545454",
  line: "#E7E7DE",
  white: "#FFFFFF",
} as const;

/** Nama resmi rumah sakit untuk ditampilkan di UI. */
export const RS_NAME = "RS Medika Sentosa";
