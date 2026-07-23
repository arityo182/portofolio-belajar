package com.medikasentosa.features.rawatinap.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medikasentosa.features.rawatinap.entity.StatusRawatInap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO respons data rawat inap dalam bentuk datar (flat).
 *
 * @author Ari
 * @since 1.0.0
 */
public record RawatInapResponse(
        Long id,
        Long pasienId,
        String namaPasien,
        Long dokterId,
        String namaDokter,
        Long kamarId,
        String nomorKamar,
        BigDecimal hargaPerMalam,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate tanggalMasuk,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate tanggalKeluar,
        String diagnosaAwal,
        String catatan,
        StatusRawatInap status,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt) {
}