package com.medikasentosa.features.laboratorium.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity yang merepresentasikan hasil dari sebuah permintaan laboratorium
 * (FR-28). Satu order laboratorium memiliki tepat satu hasil.
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "lab_hasil")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabHasil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "lab_order_id", unique = true)
    private LabOrder labOrder;

    /** Isi hasil pemeriksaan (teks deskriptif) */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String hasilText;

    /** Nilai normal sebagai referensi, mis. "12-16 g/dL" */
    @Column(columnDefinition = "TEXT")
    private String nilaiNormal;

    /** Interpretasi / catatan dokter lab */
    @Column(columnDefinition = "TEXT")
    private String interpretasi;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}