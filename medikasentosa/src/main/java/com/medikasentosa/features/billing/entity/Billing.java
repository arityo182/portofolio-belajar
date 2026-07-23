package com.medikasentosa.features.billing.entity;

import com.medikasentosa.features.pasien.entity.Pasien;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity yang merepresentasikan tagihan (billing) pasien (FR-32).
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "billing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pasien_id")
    private Pasien pasien;

    /** Total tagihan setelah semua item dijumlahkan */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalHarga;

    /** Status pembayaran */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusBilling status;

    /** Tanggal tagihan */
    @Column(nullable = false)
    private LocalDate tanggalTagihan;

    @Column(columnDefinition = "TEXT")
    private String catatan;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}