package com.medikasentosa.features.dokter.dto;

/**
 * DTO respons data dokter yang dikembalikan ke klien.
 *
 * @author Ari
 * @since 1.0.0
 */
public record DokterResponse(
        Long id,
        Long userId,
        String nama,
        String email,
        Long poliId,
        String namaPoli,
        String noStr,
        String spesialisasi,
        String noHp) {

}
