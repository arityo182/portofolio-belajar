package com.medikasentosa.features.jadwaldokter.entity;

import com.medikasentosa.features.dokter.entity.Dokter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * Entity yang merepresentasikan jadwal praktik dokter per hari beserta kuota pasien.
 * Penonaktifan dilakukan secara soft delete melalui field {@code isActive}.
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "jadwal_dokter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class JadwalDokter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dokter_id")
    private Dokter dokter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Hari hari;

    @Column(nullable = false)
    private LocalTime jamMulai;

    @Column(nullable = false)
    private LocalTime jamSelesai;

    @Column(nullable = false)
    private Integer kuota;

    @Column(nullable = false)
    private Boolean isActive;
}
