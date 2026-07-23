package com.medikasentosa.features.appointment.entity;

import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.jadwaldokter.entity.JadwalDokter;
import com.medikasentosa.features.pasien.entity.Pasien;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity yang merepresentasikan janji temu (appointment) antara pasien dan dokter
 * pada sebuah jadwal praktik di tanggal tertentu.
 * Dokter tidak diisi langsung oleh klien melainkan diturunkan dari jadwal terpilih.
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "appointment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Appointment {

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
    @JoinColumn(name = "jadwal_id")
    private JadwalDokter jadwal;

    @Column(nullable = false)
    private LocalDate tanggal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAppointment status;

    @Column(columnDefinition = "TEXT")
    private String keluhan;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Mengisi otomatis waktu pembuatan dan status default {@code MENUNGGU}
     * (bila status belum diset) tepat sebelum entity disimpan pertama kali.
     */
    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = StatusAppointment.MENUNGGU;
        }
    }
}
