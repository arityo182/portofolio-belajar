package com.medikasentosa.features.obat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity yang merepresentasikan data master obat di rumah sakit.
 * Penonaktifan dilakukan secara soft delete melalui field {@code isActive}.
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "obat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Obat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nama obat (unik), mis. "Paracetamol 500mg" */
    @Column(nullable = false, unique = true)
    private String nama;

    /** Kategori obat, mis. "Analgesik", "Antibiotik" */
    @Column(nullable = false)
    private String kategori;

    /** Satuan obat, mis. "tablet", "kapsul", "ml", "botol" */
    @Column(nullable = false)
    private String satuan;

    /** Stok obat tersedia */
    @Column(nullable = false)
    private Integer stok;

    /** Harga obat per satuan */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal harga;

    /** Deskripsi / keterangan obat */
    @Column(columnDefinition = "TEXT")
    private String deskripsi;

    /** Tanggal produksi obat */
    private LocalDate tanggalProduksi;

    /** Tanggal kadaluarsa obat */
    private LocalDate tanggalKadaluarsa;

    /** Status keaktifan; false = obat dinonaktifkan (soft delete) */
    @Column(nullable = false)
    private Boolean isActive;
}