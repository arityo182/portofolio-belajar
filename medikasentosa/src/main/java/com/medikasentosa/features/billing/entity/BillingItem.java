package com.medikasentosa.features.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity yang merepresentasikan satu item dalam tagihan (billing_item).
 * Subtotal dihitung sebagai {@code jumlah * hargaSatuan}.
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "billing_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "billing_id")
    private Billing billing;

    /** Deskripsi item, mis. "Biaya Konsultasi - Poli Jantung" */
    @Column(nullable = false)
    private String deskripsi;

    /** Jumlah item */
    @Column(nullable = false)
    private Integer jumlah;

    /** Harga per satuan */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal hargaSatuan;

    /** Subtotal = jumlah * hargaSatuan */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}