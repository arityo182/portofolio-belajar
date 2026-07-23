package com.medikasentosa.features.appointment.entity;

/**
 * Enumerasi status siklus hidup sebuah janji temu (appointment).
 * <ul>
 *     <li>{@code MENUNGGU} — janji temu baru dibuat, menunggu giliran pemeriksaan.</li>
 *     <li>{@code DIPERIKSA} — pasien sedang diperiksa oleh dokter.</li>
 *     <li>{@code SELESAI} — pemeriksaan telah selesai.</li>
 *     <li>{@code BATAL} — janji temu dibatalkan.</li>
 * </ul>
 *
 * @author Ari
 * @since 1.0.0
 */
public enum StatusAppointment {
    MENUNGGU,
    DIPERIKSA,
    SELESAI,
    BATAL
}
