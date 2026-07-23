package com.medikasentosa.features.rawatinap.repository;

import com.medikasentosa.features.rawatinap.entity.Kamar;
import com.medikasentosa.features.rawatinap.entity.TipeKamar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository akses data untuk entity {@link Kamar}.
 *
 * @author Ari
 * @since 1.0.0
 */
public interface KamarRepository extends JpaRepository<Kamar, Long> {

    /**
     * Mengambil seluruh kamar yang masih aktif.
     */
    List<Kamar> findByIsActiveTrue();

    /**
     * Mengambil kamar aktif berdasarkan tipe.
     */
    List<Kamar> findByTipeKamarAndIsActiveTrue(TipeKamar tipeKamar);

    /**
     * Mengambil kamar yang tersedia (aktif & terisi lebih kecil dari kapasitas).
     */
    @Query("SELECT k FROM Kamar k WHERE k.isActive = true AND k.terisi < k.kapasitas")
    List<Kamar> findAvailableKamar();

    /**
     * Mengecek apakah nomor kamar sudah dipakai.
     */
    boolean existsByNomorKamarIgnoreCase(String nomorKamar);
}