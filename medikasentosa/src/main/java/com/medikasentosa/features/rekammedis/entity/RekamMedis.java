package com.medikasentosa.features.rekammedis.entity;

import com.medikasentosa.features.appointment.entity.Appointment;
import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.pasien.entity.Pasien;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity yang merepresentasikan catatan rekam medis satu kali kunjungan pasien.
 * Setiap rekam medis dibuat dari sebuah janji temu (appointment) — relasi satu-ke-satu —
 * dan menurunkan pasien serta dokter dari appointment tersebut.
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "rekam_medis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class RekamMedis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pasien_id")
    private Pasien pasien;

    @ManyToOne
    @JoinColumn(name = "dokter_id")
    private Dokter dokter;

    @OneToOne
    @JoinColumn(name = "appointment_id", unique = true)
    private Appointment appointment;

    @Column(columnDefinition = "TEXT")
    private String diagnosa;

    @Column(columnDefinition = "TEXT")
    private String tindakan;

    @Column(columnDefinition = "TEXT")
    private String catatan;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Mengisi otomatis waktu pembuatan rekam medis tepat sebelum entity disimpan pertama kali.
     */
    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
