package com.medikasentosa.features.poli.service;

import com.medikasentosa.features.poli.dto.PoliRequest;
import com.medikasentosa.features.poli.dto.PoliResponse;
import com.medikasentosa.features.poli.entity.Poli;
import com.medikasentosa.features.poli.repository.PoliRepository;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service untuk logika bisnis manajemen poli (poliklinik).
 * Menangani pembuatan, pembacaan, pembaruan, dan penonaktifan (soft delete) data poli.
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class PoliService {

    private final PoliRepository poliRepository;

    public PoliService(PoliRepository poliRepository) {
        this.poliRepository = poliRepository;
    }

    /**
     * Membuat data poli baru setelah memastikan nama poli belum digunakan.
     *
     * @param request data poli baru (nama, deskripsi)
     * @return data poli yang berhasil dibuat
     * @throws DuplicateResourceException bila poli dengan nama tersebut sudah ada
     */
    @CacheEvict(value = "poli-all", allEntries = true) // invalidasi cache daftar poli
    public PoliResponse create(PoliRequest request) {
        if (poliRepository.existsByNamaIgnoreCase(request.nama())) {
            throw new DuplicateResourceException("Poli dengan nama ini sudah ada");
        }

        Poli poli = Poli.builder()
                .nama(request.nama())
                .deskripsi(request.deskripsi())
                .isActive(true)
                .build();

        poliRepository.save(poli);
        return toResponse(poli);
    }

    /**
     * Mengambil seluruh daftar poli.
     *
     * @return daftar semua poli
     */
    @Cacheable(value = "poli-all") // cache daftar poli (jarang berubah, sering diakses)
    public List<PoliResponse> getAll() {
        return poliRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    /**
     * Mengambil satu poli berdasarkan ID.
     *
     * @param id ID poli yang dicari
     * @return data poli yang ditemukan
     * @throws ResourceNotFoundException bila poli tidak ditemukan
     */
    @Cacheable(value = "poli-by-id", key = "#id") // cache per poli (halaman detail)
    public PoliResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Memperbarui nama dan deskripsi poli yang sudah ada.
     *
     * @param id      ID poli yang akan diperbarui
     * @param request data poli baru (nama, deskripsi)
     * @return data poli setelah diperbarui
     * @throws ResourceNotFoundException bila poli tidak ditemukan
     */
    @CacheEvict(value = {"poli-all", "poli-by-id"}, allEntries = true) // invalidasi semua cache poli
    public PoliResponse update(Long id, PoliRequest request) {
        Poli poli = findOrThrow(id);
        poli.setNama(request.nama());
        poli.setDeskripsi(request.deskripsi());
        poliRepository.save(poli);
        return toResponse(poli);
    }

    /**
     * Menonaktifkan sebuah poli (soft delete) dengan menyetel {@code isActive} menjadi false.
     *
     * @param id ID poli yang akan dinonaktifkan
     * @throws ResourceNotFoundException bila poli tidak ditemukan
     */
    @CacheEvict(value = {"poli-all", "poli-by-id"}, allEntries = true) // invalidasi cache
    public void deactivate(Long id) {
        Poli poli = findOrThrow(id);
        poli.setIsActive(false);
        poliRepository.save(poli);
    }

    private Poli findOrThrow(Long id) {
        return poliRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Poli tidak ditemukan"));
    }

    private PoliResponse toResponse(Poli poli) {
        return new PoliResponse(
                poli.getId(),
                poli.getNama(),
                poli.getDeskripsi(),
                poli.getIsActive());
    }
}
