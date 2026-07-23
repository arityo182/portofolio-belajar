package com.medikasentosa.features.screening.entity;

import com.medikasentosa.features.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Entity yang menyimpan hasil satu kali screening osteoporosis milik seorang pengguna.
 * Dipetakan ke tabel {@code screenings} dan menyimpan output model machine learning.
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "screenings") // ← kasih nama eksplisit
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private Double confidence;

    @Column(columnDefinition = "TEXT")
    private String risk; // ← pesan risiko dari ML (FR-21)

    @Column(columnDefinition = "TEXT")
    private String gradcamImage; // ← c kecil, samakan semua

    private Long latencyMs;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() { // (typo onCrate → onCreate, kosmetik)
        this.createdAt = Instant.now();
    }
}
