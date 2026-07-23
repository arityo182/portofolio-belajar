package com.medikasentosa.features.resep.entity;

import com.medikasentosa.features.obat.entity.Obat;
import com.medikasentosa.features.rekammedis.entity.RekamMedis;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity yang merepresentasikan satu baris resep obat pada sebuah rekam medis.
 *
 * Sejak Fase 2, resep dapat merujuk ke data master obat ({@link Obat}) melalui
 * relasi {@code @ManyToOne} opsional. {@code namaObat} tetap dipertahankan
 * sebagai fallback untuk data resep lama yang belum memiliki relasi ke tabel obat.
 *
 * Aturan resolusi nama obat yang ditampilkan:
 * <ul>
 *   <li>Jika {@code obat} (obat_id) tidak null → gunakan {@code obat.getNama()}</li>
 *   <li>Jika null (resep lama) → gunakan field {@code namaObat}</li>
 * </ul>
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "resep")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Resep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rekam_medis_id")
    private RekamMedis rekamMedis;

    /**
     * Relasi opsional ke data master obat (Fase 2).
     * Nullable agar kompatibel mundur dengan data resep lama.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obat_id", nullable = true)
    private Obat obat;

    /**
     * Nama obat sebagai fallback (digunakan bila {@code obat} null).
     * Sejak Fase 2, nullable karena data baru mengisi dari relasi {@code obat}.
     */
    private String namaObat;

    private String dosis;

    @Column(nullable = false)
    private Integer jumlah;

    private String aturanPakai;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Mengisi otomatis waktu pembuatan resep tepat sebelum entity disimpan pertama kali.
     */
    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Menentukan nama obat yang akan ditampilkan ke klien.
     * Prioritas: nama dari relasi {@code obat} jika tersedia, fallback ke
     * field {@code namaObat}.
     *
     * @return nama obat yang paling akurat untuk ditampilkan
     */
    @Transient
    public String getNamaObatTampil() {
        return (obat != null) ? obat.getNama() : namaObat;
    }
}
