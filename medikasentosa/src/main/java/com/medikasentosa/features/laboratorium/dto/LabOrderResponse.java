package com.medikasentosa.features.laboratorium.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medikasentosa.features.laboratorium.entity.StatusLab;

import java.time.LocalDateTime;

/**
 * DTO respons data order laboratorium dalam bentuk datar (flat).
 * Menggabungkan informasi rekam medis & pasien agar mudah ditampilkan.
 *
 * @author Ari
 * @since 1.0.0
 */
public record LabOrderResponse(
        Long id, Long rekamMedisId, String namaPasien,
        String noRekamMedis, String nik,
        String jenisPemeriksaan, String catatan,
        StatusLab status,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt) {
}