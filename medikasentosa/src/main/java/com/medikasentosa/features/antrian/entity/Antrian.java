package com.medikasentosa.features.antrian.entity;

import com.medikasentosa.features.appointment.entity.Appointment;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity yang merepresentasikan nomor antrian untuk sebuah janji temu (appointment).
 * Setiap appointment memiliki paling banyak satu antrian (relasi satu-ke-satu),
 * dengan {@code nomorUrut} yang unik per jadwal dan tanggal appointment tersebut.
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "antrian")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Antrian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "appointment_id", unique = true)
    private Appointment appointment;

    @Column(nullable = false)
    private Integer nomorUrut;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Mengisi otomatis waktu pembuatan antrian tepat sebelum entity disimpan pertama kali.
     */
    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
