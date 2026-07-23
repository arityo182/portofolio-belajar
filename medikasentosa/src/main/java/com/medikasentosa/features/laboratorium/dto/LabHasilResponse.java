package com.medikasentosa.features.laboratorium.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * DTO respons data hasil laboratorium dalam bentuk datar (flat).
 * Menggabungkan informasi dari order laboratorium terkait (jenis pemeriksaan,
 * nama pasien) agar mudah ditampilkan.
 *
 * @author Ari
 * @since 1.0.0
 */
public record LabHasilResponse(
        Long id,
        Long labOrderId,
        String jenisPemeriksaan,
        String namaPasien,
        String hasilText,
        String nilaiNormal,
        String interpretasi,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt) {
}