package com.medikasentosa.features.radiologi.entity;

import com.medikasentosa.features.rekammedis.entity.RekamMedis;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity yang merepresentasikan order radiologi konvensional
 * dari sebuah rekam medis (FR-29).
 *
 * <p>Ini BERBEDA dari modul Screening (osteoporosis AI). Screening
 * adalah skrining berbasis AI dengan model deep learning, sedangkan
 * modul ini adalah order radiologi konvensional (X-Ray, CT-Scan, MRI,
 * USG) dengan hasil deskriptif yang ditulis dokter radiologi. Kedua
 * modul berjalan paralel dan saling melengkapi.</p>
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "radiologi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Radiologi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rekam_medis_id")
    private RekamMedis rekamMedis;

    /** Jenis pemeriksaan, mis. "X-Ray Dada", "CT-Scan Kepala", "MRI Lutut", "USG Abdomen" */
    @Column(nullable = false)
    private String jenisRadiologi;

    /** Hasil deskriptif dari dokter radiologi */
    @Column(columnDefinition = "TEXT")
    private String hasilDeskripsi;

    /** Catatan tambahan dari dokter pengirim */
    @Column(columnDefinition = "TEXT")
    private String catatanDokter;

    /** Path atau URL gambar hasil radiologi (opsional) */
    @Column(columnDefinition = "TEXT")
    private String gambarUrl;

    /** Status: MENUNGGU → SELESAI */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRadiologi status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}