package com.medikasentosa.features.jadwaldokter.service;

import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.dokter.repository.DokterRepository;
import com.medikasentosa.features.jadwaldokter.dto.JadwalDokterRequest;
import com.medikasentosa.features.jadwaldokter.dto.JadwalDokterResponse;
import com.medikasentosa.features.jadwaldokter.entity.JadwalDokter;
import com.medikasentosa.features.jadwaldokter.repository.JadwalDokterRepository;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service untuk logika bisnis manajemen jadwal praktik dokter.
 * Menangani pembuatan, pembacaan, pembaruan, dan penonaktifan (soft delete) jadwal.
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class JadwalDokterService {

    private final JadwalDokterRepository jadwalDokterRepository;
    private final DokterRepository dokterRepository;

    public JadwalDokterService(JadwalDokterRepository jadwalDokterRepository,
                               DokterRepository dokterRepository) {
        this.jadwalDokterRepository = jadwalDokterRepository;
        this.dokterRepository = dokterRepository;
    }

    /**
     * Membuat jadwal praktik baru untuk seorang dokter.
     *
     * @param request data jadwal baru (dokterId, hari, jamMulai, jamSelesai, kuota)
     * @return data jadwal yang berhasil dibuat
     * @throws ResourceNotFoundException bila dokter tidak ditemukan
     * @throws IllegalArgumentException  bila jam mulai tidak sebelum jam selesai
     */
    @CacheEvict(value = "jadwal-by-dokter", key = "#request.dokterId()")
    public JadwalDokterResponse create(JadwalDokterRequest request) {
        Dokter dokter = findDokterOrThrow(request.dokterId());
        validateJam(request);

        JadwalDokter jadwal = JadwalDokter.builder()
                .dokter(dokter)
                .hari(request.hari())
                .jamMulai(request.jamMulai())
                .jamSelesai(request.jamSelesai())
                .kuota(request.kuota())
                .isActive(true)
                .build();

        jadwalDokterRepository.save(jadwal);
        return toResponse(jadwal);
    }

    /**
     * Mengambil seluruh daftar jadwal praktik.
     *
     * @return daftar semua jadwal
     */
    public List<JadwalDokterResponse> getAll() {
        return jadwalDokterRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    /**
     * Mengambil satu jadwal berdasarkan ID.
     *
     * @param id ID jadwal yang dicari
     * @return data jadwal yang ditemukan
     * @throws ResourceNotFoundException bila jadwal tidak ditemukan
     */
    public JadwalDokterResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Mengambil seluruh jadwal aktif milik seorang dokter.
     *
     * @param dokterId ID dokter yang dicari
     * @return daftar jadwal aktif dokter tersebut
     * @throws ResourceNotFoundException bila dokter tidak ditemukan
     */
    @Cacheable(value = "jadwal-by-dokter", key = "#dokterId",
               unless = "#result.isEmpty()") // cache jadwal dokter (sering diakses booking)
    public List<JadwalDokterResponse> getByDokter(Long dokterId) {
        Dokter dokter = findDokterOrThrow(dokterId);
        return jadwalDokterRepository.findByDokterAndIsActiveTrue(dokter)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Memperbarui data jadwal praktik yang sudah ada.
     *
     * @param id      ID jadwal yang akan diperbarui
     * @param request data jadwal baru (hari, jamMulai, jamSelesai, kuota)
     * @return data jadwal setelah diperbarui
     * @throws ResourceNotFoundException bila jadwal tidak ditemukan
     * @throws IllegalArgumentException  bila jam mulai tidak sebelum jam selesai
     */
    @CacheEvict(value = "jadwal-by-dokter", key = "#request.dokterId()")
    public JadwalDokterResponse update(Long id, JadwalDokterRequest request) {
        JadwalDokter jadwal = findOrThrow(id);
        validateJam(request);

        jadwal.setHari(request.hari());
        jadwal.setJamMulai(request.jamMulai());
        jadwal.setJamSelesai(request.jamSelesai());
        jadwal.setKuota(request.kuota());
        jadwalDokterRepository.save(jadwal);
        return toResponse(jadwal);
    }

    /**
     * Menonaktifkan sebuah jadwal (soft delete) dengan menyetel {@code isActive} menjadi false.
     *
     * @param id ID jadwal yang akan dinonaktifkan
     * @throws ResourceNotFoundException bila jadwal tidak ditemukan
     */
    @CacheEvict(value = "jadwal-by-dokter", allEntries = true) // invalidasi seluruh cache jadwal
    public void deactivate(Long id) {
        JadwalDokter jadwal = findOrThrow(id);
        jadwal.setIsActive(false);
        jadwalDokterRepository.save(jadwal);
    }

    private void validateJam(JadwalDokterRequest request) {
        if (!request.jamMulai().isBefore(request.jamSelesai())) {
            throw new IllegalArgumentException("Jam mulai harus sebelum jam selesai");
        }
    }

    private JadwalDokter findOrThrow(Long id) {
        return jadwalDokterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jadwal tidak ditemukan"));
    }

    private Dokter findDokterOrThrow(Long id) {
        return dokterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dokter tidak ditemukan"));
    }

    private JadwalDokterResponse toResponse(JadwalDokter jadwal) {
        return new JadwalDokterResponse(
                jadwal.getId(),
                jadwal.getDokter().getId(),
                jadwal.getDokter().getUser().getNama(),
                jadwal.getHari(),
                jadwal.getJamMulai(),
                jadwal.getJamSelesai(),
                jadwal.getKuota(),
                jadwal.getIsActive());
    }
}
