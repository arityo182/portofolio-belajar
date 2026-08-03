package com.medikasentosa.features.obat.service;

import com.medikasentosa.features.obat.dto.ObatRequest;
import com.medikasentosa.features.obat.dto.ObatResponse;
import com.medikasentosa.features.obat.entity.Obat;
import com.medikasentosa.features.obat.repository.ObatRepository;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service untuk logika bisnis manajemen data master obat.
 * Menangani pembuatan, pembacaan, pembaruan, dan penonaktifan (soft delete) data obat.
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class ObatService {

    private static final Logger log = LoggerFactory.getLogger(ObatService.class);

    private final ObatRepository obatRepository;

    public ObatService(ObatRepository obatRepository) {
        this.obatRepository = obatRepository;
    }

    /**
     * Membuat data obat baru setelah memastikan nama obat belum digunakan.
     *
     * @param request data obat baru (nama, kategori, satuan, stok, harga, deskripsi)
     * @return data obat yang berhasil dibuat
     * @throws DuplicateResourceException bila obat dengan nama tersebut sudah ada
     */
    @CacheEvict(value = "obat-all", allEntries = true)
    public ObatResponse create(ObatRequest request) {
        log.info("Creating obat: {}", request.nama());
        if (obatRepository.existsByNamaIgnoreCase(request.nama())) {
            log.warn("Obat with name '{}' already exists", request.nama());
            throw new DuplicateResourceException("Obat dengan nama ini sudah ada");
        }

        Obat obat = Obat.builder()
                .nama(request.nama())
                .kategori(request.kategori())
                .satuan(request.satuan())
                .stok(request.stok())
                .harga(request.harga())
                .deskripsi(request.deskripsi())
                .isActive(true)
                .build();

        obatRepository.save(obat);
        return toResponse(obat);
    }

    /**
     * Mengambil seluruh daftar obat.
     *
     * @return daftar semua obat
     */
    @Cacheable(value = "obat-all") // master data jarang berubah
    public List<ObatResponse> getAll() {
        return obatRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    /**
     * Mengambil satu obat berdasarkan ID.
     *
     * @param id ID obat yang dicari
     * @return data obat yang ditemukan
     * @throws ResourceNotFoundException bila obat tidak ditemukan
     */
    public ObatResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Memperbarui data obat yang sudah ada.
     *
     * @param id      ID obat yang akan diperbarui
     * @param request data obat baru (nama, kategori, satuan, stok, harga, deskripsi)
     * @return data obat setelah diperbarui
     * @throws ResourceNotFoundException bila obat tidak ditemukan
     * @throws DuplicateResourceException bila nama baru sudah dipakai obat lain
     */
    @CacheEvict(value = "obat-all", allEntries = true)
    public ObatResponse update(Long id, ObatRequest request) {
        log.info("Updating obat id={}", id);
        Obat obat = findOrThrow(id);

        // Cek duplikat nama hanya jika nama diubah
        if (!obat.getNama().equalsIgnoreCase(request.nama())
                && obatRepository.existsByNamaIgnoreCase(request.nama())) {
            log.warn("Obat with name '{}' already exists", request.nama());
            throw new DuplicateResourceException("Obat dengan nama ini sudah ada");
        }

        obat.setNama(request.nama());
        obat.setKategori(request.kategori());
        obat.setSatuan(request.satuan());
        obat.setStok(request.stok());
        obat.setHarga(request.harga());
        obat.setDeskripsi(request.deskripsi());
        obatRepository.save(obat);
        return toResponse(obat);
    }

    /**
     * Menonaktifkan sebuah obat (soft delete) dengan menyetel {@code isActive} menjadi false.
     *
     * @param id ID obat yang akan dinonaktifkan
     * @throws ResourceNotFoundException bila obat tidak ditemukan
     */
    @CacheEvict(value = "obat-all", allEntries = true)
    public void deactivate(Long id) {
        log.info("Deactivating obat id={}", id);
        Obat obat = findOrThrow(id);
        obat.setIsActive(false);
        obatRepository.save(obat);
    }

    /** Kurangi stok obat (saat diberikan ke pasien). */
    @Transactional
    public ObatResponse kurangiStok(Long id, int jumlah) {
        log.info("Reducing stok obat id={} by {}", id, jumlah);
        Obat obat = findOrThrow(id);
        if (obat.getStok() < jumlah) {
            log.warn("Stok obat id={} tidak mencukupi: requested={}, available={}", id, jumlah, obat.getStok());
            throw new IllegalArgumentException("Stok tidak mencukupi (" + obat.getStok() + " tersedia)");
        }
        obat.setStok(obat.getStok() - jumlah);
        obatRepository.save(obat);
        return toResponse(obat);
    }

    private Obat findOrThrow(Long id) {
        return obatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obat tidak ditemukan"));
    }

    private ObatResponse toResponse(Obat obat) {
        return new ObatResponse(
                obat.getId(), obat.getNama(), obat.getKategori(),
                obat.getSatuan(), obat.getStok(), obat.getHarga(),
                obat.getDeskripsi(),
                obat.getTanggalProduksi(), obat.getTanggalKadaluarsa(),
                obat.getIsActive());
    }
}