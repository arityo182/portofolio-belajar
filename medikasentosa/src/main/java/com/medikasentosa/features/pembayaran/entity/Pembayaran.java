package com.medikasentosa.features.pembayaran.entity;

import com.medikasentosa.features.billing.entity.Billing;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity yang merepresentasikan pembayaran tagihan pasien (FR-33, FR-34).
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "pembayaran")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pembayaran {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "billing_id", unique = true)
    private Billing billing;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodePembayaran metodePembayaran;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal jumlahBayar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPembayaran status;

    /** URL/path bukti transfer (nullable untuk tunai) */
    @Column(columnDefinition = "TEXT")
    private String buktiTransfer;

    @Column(nullable = false)
    private LocalDateTime tanggalBayar;

    @Column(columnDefinition = "TEXT")
    private String catatan;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { this.createdAt = LocalDateTime.now(); }
}