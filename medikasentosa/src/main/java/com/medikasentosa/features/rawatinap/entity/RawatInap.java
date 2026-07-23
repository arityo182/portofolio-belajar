package com.medikasentosa.features.rawatinap.entity;

import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.pasien.entity.Pasien;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity yang merepresentasikan rawat inap pasien (FR-30).
 *
 * Mencatat pasien masuk ke kamar rawat inap hingga keluar.
 * Saat pasien masuk, {@link Kamar#getTerisi()} bertambah 1;
 * saat keluar, berkurang 1.
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "rawat_inap")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawatInap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pasien_id")
    private Pasien pasien;

    @ManyToOne
    @JoinColumn(name = "dokter_id")
    private Dokter dokter;

    @ManyToOne
    @JoinColumn(name = "kamar_id")
    private Kamar kamar;

    /** Tanggal masuk rawat inap */
    @Column(nullable = false)
    private LocalDate tanggalMasuk;

    /** Tanggal keluar; null = pasien masih dirawat */
    private LocalDate tanggalKeluar;

    /** Diagnosa awal saat masuk */
    @Column(columnDefinition = "TEXT")
    private String diagnosaAwal;

    @Column(columnDefinition = "TEXT")
    private String catatan;

    /** Status: AKTIF → SELESAI (keluar) / DIPINDAH */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRawatInap status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}