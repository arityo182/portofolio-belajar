package com.medikasentosa.features.rawatinap.dto;

import com.medikasentosa.features.rawatinap.entity.TipeKamar;

import java.math.BigDecimal;

/**
 * DTO respons data kamar yang dikembalikan ke klien.
 *
 * @author Ari
 * @since 1.0.0
 */
public record KamarResponse(
        Long id,
        String nomorKamar,
        TipeKamar tipeKamar,
        Integer kapasitas,
        Integer terisi,
        BigDecimal hargaPerMalam,
        Boolean isActive) {
}