/**
 * @module features/screening/types
 *
 * Tipe data untuk fitur skrining osteoporosis.
 */

/**
 * Hasil skrining osteoporosis yang dikembalikan backend
 * (Spring Boot yang meneruskan prediksi dari layanan ML FastAPI).
 */
export interface ScreeningResult {
  /** ID record hasil skrining (ada bila diambil dari riwayat) */
  id?: number;
  /** Label kelas prediksi: "Normal" | "Osteopenia" | "Osteoporosis" */
  label: string;
  /** Tingkat keyakinan prediksi kelas terpilih, skala 0-100 */
  confidence: number;
  /** Pesan/kategori risiko yang mendampingi hasil (opsional) */
  risk?: string;
  /**
   * Distribusi probabilitas per kelas dalam persen.
   * Contoh: `{ Normal: 4.2, Osteopenia: 8.3, Osteoporosis: 87.5 }`
   */
  probabilities?: Record<string, number>;
  /** Citra heatmap Grad-CAM sebagai data URL base64 */
  gradcamImage?: string;
  /** Latensi (waktu inferensi) model dalam milidetik */
  latencyMs?: number;
  /** Timestamp pembuatan record (ISO string), diisi pada data riwayat */
  createdAt?: string;
}
