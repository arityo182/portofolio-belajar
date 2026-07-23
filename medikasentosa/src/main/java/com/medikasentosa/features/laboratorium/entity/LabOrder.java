package com.medikasentosa.features.laboratorium.entity;

import com.medikasentosa.features.rekammedis.entity.RekamMedis;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity yang merepresentasikan permintaan (order) pemeriksaan laboratorium
 * dari sebuah rekam medis (FR-27).
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "lab_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rekam_medis_id")
    private RekamMedis rekamMedis;

    /** Jenis pemeriksaan, mis. "Darah Lengkap", "Gula Darah Puasa" */
    @Column(nullable = false)
    private String jenisPemeriksaan;

    @Column(columnDefinition = "TEXT")
    private String catatan;

    /** Status siklus hidup: MENUNGGU → PROSES → SELESAI */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLab status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}