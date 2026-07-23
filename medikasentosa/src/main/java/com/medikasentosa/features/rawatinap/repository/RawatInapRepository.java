package com.medikasentosa.features.rawatinap.repository;

import com.medikasentosa.features.rawatinap.entity.RawatInap;
import com.medikasentosa.features.rawatinap.entity.StatusRawatInap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository akses data untuk entity {@link RawatInap}.
 *
 * @author Ari
 * @since 1.0.0
 */
public interface RawatInapRepository extends JpaRepository<RawatInap, Long> {

    /**
     * Mengambil seluruh riwayat rawat inap milik seorang pasien.
     */
    List<RawatInap> findByPasienId(Long pasienId);

    /**
     * Mengambil rawat inap dengan status tertentu.
     */
    List<RawatInap> findByStatus(StatusRawatInap status);

    /**
     * Mengambil rawat inap aktif di sebuah kamar.
     */
    List<RawatInap> findByKamarIdAndStatus(Long kamarId, StatusRawatInap status);
}