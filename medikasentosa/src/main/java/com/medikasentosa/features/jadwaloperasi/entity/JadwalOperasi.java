package com.medikasentosa.features.jadwaloperasi.entity;

import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.pasien.entity.Pasien;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity yang merepresentasikan penjadwalan operasi pasien (FR-31).
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "jadwal_operasi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JadwalOperasi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pasien_id")
    private Pasien pasien;

    @ManyToOne
    @JoinColumn(name = "dokter_id")
    private Dokter dokter;

    /** Tanggal pelaksanaan operasi */
    @Column(nullable = false)
    private LocalDate tanggalOperasi;

    /** Jam mulai operasi */
    @Column(nullable = false)
    private LocalTime jamMulai;

    /** Jenis operasi, mis. "Operasi Caesar", "Appendektomi" */
    @Column(nullable = false)
    private String jenisOperasi;

    /** Ruang operasi yang digunakan, mis. "OK-1", "OK-Cito" */
    @Column(nullable = false)
    private String ruangOperasi;

    /** Status: TERJADWAL → BERLANGSUNG → SELESAI / BATAL */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOperasi status;

    @Column(columnDefinition = "TEXT")
    private String catatan;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}