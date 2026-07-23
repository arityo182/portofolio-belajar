package com.medikasentosa.features.obat.repository;

import com.medikasentosa.features.obat.entity.Obat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository akses data untuk entity {@link Obat}.
 *
 * @author Ari
 * @since 1.0.0
 */
public interface ObatRepository extends JpaRepository<Obat, Long> {

    /**
     * Mengecek apakah obat dengan nama (case-insensitive) sudah ada.
     *
     * @param nama nama obat
     * @return {@code true} bila sudah ada
     */
    boolean existsByNamaIgnoreCase(String nama);

    /**
     * Mengambil seluruh daftar obat yang masih aktif.
     *
     * @return daftar obat aktif
     */
    List<Obat> findByIsActiveTrue();
}