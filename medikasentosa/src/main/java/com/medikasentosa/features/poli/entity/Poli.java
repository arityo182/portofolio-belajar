package com.medikasentosa.features.poli.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity yang merepresentasikan poli (poliklinik) rumah sakit.
 * Penonaktifan dilakukan secara soft delete melalui field {@code isActive}.
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Poli {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    @Column(columnDefinition = "TEXT")
    private String deskripsi;

    @Column(nullable = false)
    private Boolean isActive;
}
