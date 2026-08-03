package com.medikasentosa.features.rawatinap.service;

import com.medikasentosa.features.rawatinap.dto.KamarRequest;
import com.medikasentosa.features.rawatinap.dto.KamarResponse;
import com.medikasentosa.features.rawatinap.entity.Kamar;
import com.medikasentosa.features.rawatinap.repository.KamarRepository;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service untuk logika bisnis manajemen data master kamar rawat inap (FR-30).
 * CRUD penuh dengan soft delete, khusus admin.
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class KamarService {

    private static final Logger log = LoggerFactory.getLogger(KamarService.class);

    private final KamarRepository kamarRepository;

    public KamarService(KamarRepository kamarRepository) {
        this.kamarRepository = kamarRepository;
    }

    /**
     * Membuat data kamar baru.
     */
    public KamarResponse create(KamarRequest request) {
        log.info("Creating kamar: {}", request.nomorKamar());
        if (kamarRepository.existsByNomorKamarIgnoreCase(request.nomorKamar())) {
            log.warn("Kamar with nomor '{}' already exists", request.nomorKamar());
            throw new DuplicateResourceException("Nomor kamar sudah ada");
        }

        Kamar kamar = Kamar.builder()
                .nomorKamar(request.nomorKamar())
                .tipeKamar(request.tipeKamar())
                .kapasitas(request.kapasitas())
                .terisi(0)
                .hargaPerMalam(request.hargaPerMalam())
                .isActive(true)
                .build();

        kamarRepository.save(kamar);
        return toResponse(kamar);
    }

    /**
     * Mengambil seluruh kamar (termasuk nonaktif).
     */
    public List<KamarResponse> getAll() {
        return kamarRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Mengambil kamar yang tersedia (aktif + terisi < kapasitas).
     */
    public List<KamarResponse> getAvailable() {
        return kamarRepository.findAvailableKamar().stream().map(this::toResponse).toList();
    }

    /**
     * Memperbarui data kamar.
     */
    public KamarResponse update(Long id, KamarRequest request) {
        log.info("Updating kamar id={}", id);
        Kamar kamar = findOrThrow(id);

        if (!kamar.getNomorKamar().equalsIgnoreCase(request.nomorKamar())
                && kamarRepository.existsByNomorKamarIgnoreCase(request.nomorKamar())) {
            log.warn("Kamar with nomor '{}' already exists", request.nomorKamar());
            throw new DuplicateResourceException("Nomor kamar sudah ada");
        }

        kamar.setNomorKamar(request.nomorKamar());
        kamar.setTipeKamar(request.tipeKamar());
        kamar.setKapasitas(request.kapasitas());
        kamar.setHargaPerMalam(request.hargaPerMalam());
        kamarRepository.save(kamar);
        return toResponse(kamar);
    }

    /**
     * Menonaktifkan kamar (soft delete).
     */
    public void deactivate(Long id) {
        log.info("Deactivating kamar id={}", id);
        Kamar kamar = findOrThrow(id);
        kamar.setIsActive(false);
        kamarRepository.save(kamar);
    }

    Kamar findOrThrow(Long id) {
        return kamarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kamar tidak ditemukan"));
    }

    KamarResponse toResponse(Kamar k) {
        return new KamarResponse(
                k.getId(), k.getNomorKamar(), k.getTipeKamar(),
                k.getKapasitas(), k.getTerisi(), k.getHargaPerMalam(), k.getIsActive());
    }
}