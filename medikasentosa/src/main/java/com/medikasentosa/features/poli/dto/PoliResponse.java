package com.medikasentosa.features.poli.dto;

/**
 * DTO respons data poli yang dikembalikan ke klien.
 *
 * @author Ari
 * @since 1.0.0
 */
public record PoliResponse(
        Long id,
        String nama,
        String deskripsi,
        Boolean isActive) {

}
