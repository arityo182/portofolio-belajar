package com.medikasentosa.features.admin.dto;

/**
 * DTO respons statistik ringkas untuk dashboard admin.
 *
 * @author Ari
 * @since 1.0.0
 */
public record DashboardResponse(
        long totalPasien,
        long totalDokter,
        long totalPoli,
        long totalAppointment) {
}
