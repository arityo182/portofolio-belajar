package com.medikasentosa.features.rawatinap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity yang merepresentasikan master data kamar rawat inap (FR-30).
 * Penonaktifan dilakukan secara soft delete melalui field {@code isActive}.
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "kamar")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kamar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nomor kamar (unik), mis. "VIP-101", "ICU-02" */
    @Column(nullable = false, unique = true)
    private String nomorKamar;

    /** Tipe/kelas kamar: REGULER, VIP, VVIP, ICU */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipeKamar tipeKamar;

    /** Kapasitas maksimum kamar */
    @Column(nullable = false)
    private Integer kapasitas;

    /** Jumlah tempat tidur yang sedang terisi (default 0) */
    @Builder.Default
    @Column(nullable = false)
    private Integer terisi = 0;

    /** Harga per malam */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal hargaPerMalam;

    /** Status keaktifan; false = kamar dinonaktifkan (soft delete) */
    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;
}