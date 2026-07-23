package com.medikasentosa.features.radiologi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medikasentosa.features.radiologi.entity.StatusRadiologi;

import java.time.LocalDateTime;

/**
 * DTO respons data order radiologi dalam bentuk datar (flat).
 *
 * @author Ari
 * @since 1.0.0
 */
public record RadiologiResponse(
        Long id, Long rekamMedisId, String namaPasien,
        String noRekamMedis, String nik,
        String jenisRadiologi, String hasilDeskripsi,
        String catatanDokter, String gambarUrl,
        StatusRadiologi status,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt) {
}