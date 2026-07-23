package com.medikasentosa.features.rawatinap.service;

import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.dokter.repository.DokterRepository;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.pasien.repository.PasienRepository;
import com.medikasentosa.features.rawatinap.dto.RawatInapRequest;
import com.medikasentosa.features.rawatinap.dto.RawatInapResponse;
import com.medikasentosa.features.rawatinap.entity.Kamar;
import com.medikasentosa.features.rawatinap.entity.RawatInap;
import com.medikasentosa.features.rawatinap.entity.StatusRawatInap;
import com.medikasentosa.features.rawatinap.repository.RawatInapRepository;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service untuk logika bisnis manajemen rawat inap pasien (FR-30).
 *
 * <p>Menangani alur masuk rawat inap (validasi kamar tersedia + increment terisi)
 * dan keluar (set SELESAI + decrement terisi + set tanggalKeluar).</p>
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class RawatInapService {

    private final RawatInapRepository rawatInapRepository;
    private final KamarService kamarService;
    private final PasienRepository pasienRepository;
    private final DokterRepository dokterRepository;

    public RawatInapService(RawatInapRepository rawatInapRepository,
                            KamarService kamarService,
                            PasienRepository pasienRepository,
                            DokterRepository dokterRepository) {
        this.rawatInapRepository = rawatInapRepository;
        this.kamarService = kamarService;
        this.pasienRepository = pasienRepository;
        this.dokterRepository = dokterRepository;
    }

    /**
     * Mencatat pasien masuk rawat inap.
     *
     * <p>Alur bisnis:</p>
     * <ol>
     *   <li>Validasi pasien & dokter ada</li>
     *   <li>Validasi kamar ada & masih tersedia (terisi < kapasitas)</li>
     *   <li>Tambah kamar.terisi +1</li>
     *   <li>Simpan rawat inap dengan status AKTIF</li>
     * </ol>
     *
     * @param request data masuk rawat inap
     * @return data rawat inap yang berhasil dibuat
     * @throws ResourceNotFoundException bila pasien/dokter/kamar tidak ditemukan
     * @throws IllegalStateException    bila kamar penuh
     */
    @Transactional
    public RawatInapResponse masuk(RawatInapRequest request) {
        Pasien pasien = pasienRepository.findById(request.pasienId())
                .orElseThrow(() -> new ResourceNotFoundException("Pasien tidak ditemukan"));
        Dokter dokter = dokterRepository.findById(request.dokterId())
                .orElseThrow(() -> new ResourceNotFoundException("Dokter tidak ditemukan"));

        Kamar kamar = kamarService.findOrThrow(request.kamarId());
        if (!kamar.getIsActive()) {
            throw new IllegalStateException("Kamar tidak tersedia (dinonaktifkan)");
        }
        if (kamar.getTerisi() >= kamar.getKapasitas()) {
            throw new IllegalStateException(
                    "Kamar " + kamar.getNomorKamar() + " sudah penuh ("
                            + kamar.getTerisi() + "/" + kamar.getKapasitas() + ")");
        }

        // Tambah jumlah terisi
        kamar.setTerisi(kamar.getTerisi() + 1);

        RawatInap ri = RawatInap.builder()
                .pasien(pasien)
                .dokter(dokter)
                .kamar(kamar)
                .tanggalMasuk(request.tanggalMasuk())
                .diagnosaAwal(request.diagnosaAwal())
                .catatan(request.catatan())
                .status(StatusRawatInap.AKTIF)
                .build();

        rawatInapRepository.save(ri);
        return toResponse(ri);
    }

    /**
     * Mencatat pasien keluar rawat inap (pulang).
     *
     * <p>Alur bisnis:</p>
     * <ol>
     *   <li>Validasi rawat inap masih AKTIF</li>
     *   <li>Set status SELESAI</li>
     *   <li>Kurangi kamar.terisi -1</li>
     *   <li>Set tanggalKeluar</li>
     * </ol>
     *
     * @param id            ID rawat inap
     * @param tanggalKeluar tanggal keluar/pulang
     * @return data rawat inap setelah diperbarui
     * @throws ResourceNotFoundException bila rawat inap tidak ditemukan
     * @throws IllegalStateException    bila rawat inap sudah selesai
     */
    @Transactional
    public RawatInapResponse keluar(Long id, LocalDate tanggalKeluar) {
        RawatInap ri = rawatInapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rawat inap tidak ditemukan"));

        if (ri.getStatus() != StatusRawatInap.AKTIF) {
            throw new IllegalStateException(
                    "Rawat inap ini sudah " + ri.getStatus().name().toLowerCase());
        }

        ri.setStatus(StatusRawatInap.SELESAI);
        ri.setTanggalKeluar(tanggalKeluar);

        // Kurangi terisi kamar
        Kamar kamar = ri.getKamar();
        kamar.setTerisi(Math.max(0, kamar.getTerisi() - 1));

        rawatInapRepository.save(ri);
        return toResponse(ri);
    }

    /**
     * Mengambil seluruh riwayat rawat inap.
     */
    public List<RawatInapResponse> getAll() {
        return rawatInapRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Mengambil riwayat rawat inap milik seorang pasien.
     */
    public List<RawatInapResponse> getByPasien(Long pasienId) {
        return rawatInapRepository.findByPasienId(pasienId)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Mengambil rawat inap yang masih aktif.
     */
    public List<RawatInapResponse> getAktif() {
        return rawatInapRepository.findByStatus(StatusRawatInap.AKTIF)
                .stream().map(this::toResponse).toList();
    }

    private RawatInapResponse toResponse(RawatInap ri) {
        return new RawatInapResponse(
                ri.getId(),
                ri.getPasien().getId(),
                ri.getPasien().getUser().getNama(),
                ri.getDokter().getId(),
                ri.getDokter().getUser().getNama(),
                ri.getKamar().getId(),
                ri.getKamar().getNomorKamar(),
                ri.getKamar().getHargaPerMalam(),
                ri.getTanggalMasuk(),
                ri.getTanggalKeluar(),
                ri.getDiagnosaAwal(),
                ri.getCatatan(),
                ri.getStatus(),
                ri.getCreatedAt());
    }
}