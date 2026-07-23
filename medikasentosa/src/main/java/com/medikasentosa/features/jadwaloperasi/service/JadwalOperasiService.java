package com.medikasentosa.features.jadwaloperasi.service;

import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.dokter.repository.DokterRepository;
import com.medikasentosa.features.jadwaloperasi.dto.JadwalOperasiRequest;
import com.medikasentosa.features.jadwaloperasi.dto.JadwalOperasiResponse;
import com.medikasentosa.features.jadwaloperasi.entity.JadwalOperasi;
import com.medikasentosa.features.jadwaloperasi.entity.StatusOperasi;
import com.medikasentosa.features.jadwaloperasi.repository.JadwalOperasiRepository;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.pasien.repository.PasienRepository;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service untuk logika bisnis penjadwalan operasi (FR-31).
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class JadwalOperasiService {

    private final JadwalOperasiRepository repo;
    private final PasienRepository pasienRepo;
    private final DokterRepository dokterRepo;

    public JadwalOperasiService(JadwalOperasiRepository repo,
                                PasienRepository pasienRepo,
                                DokterRepository dokterRepo) {
        this.repo = repo;
        this.pasienRepo = pasienRepo;
        this.dokterRepo = dokterRepo;
    }

    /**
     * Membuat jadwal operasi baru. Validasi: pasien & dokter ada, tanggal tidak lampau.
     */
    public JadwalOperasiResponse create(JadwalOperasiRequest req) {
        Pasien pasien = pasienRepo.findById(req.pasienId())
                .orElseThrow(() -> new ResourceNotFoundException("Pasien tidak ditemukan"));
        Dokter dokter = dokterRepo.findById(req.dokterId())
                .orElseThrow(() -> new ResourceNotFoundException("Dokter tidak ditemukan"));

        if (req.tanggalOperasi().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Tanggal operasi tidak boleh di masa lalu");
        }

        JadwalOperasi jo = JadwalOperasi.builder()
                .pasien(pasien).dokter(dokter)
                .tanggalOperasi(req.tanggalOperasi()).jamMulai(req.jamMulai())
                .jenisOperasi(req.jenisOperasi()).ruangOperasi(req.ruangOperasi())
                .catatan(req.catatan()).status(StatusOperasi.TERJADWAL)
                .build();

        repo.save(jo);
        return toResponse(jo);
    }

    public List<JadwalOperasiResponse> getAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    public List<JadwalOperasiResponse> getByPasien(Long pasienId) {
        return repo.findByPasienId(pasienId).stream().map(this::toResponse).toList();
    }

    public List<JadwalOperasiResponse> getByDokter(Long dokterId) {
        Dokter d = dokterRepo.findById(dokterId)
                .orElseThrow(() -> new ResourceNotFoundException("Dokter tidak ditemukan"));
        return repo.findByDokter(d).stream().map(this::toResponse).toList();
    }

    public List<JadwalOperasiResponse> getByTanggal(LocalDate tanggal) {
        return repo.findByTanggalOperasi(tanggal).stream().map(this::toResponse).toList();
    }

    /**
     * Memperbarui status jadwal operasi.
     */
    public JadwalOperasiResponse updateStatus(Long id, StatusOperasi status) {
        JadwalOperasi jo = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jadwal operasi tidak ditemukan"));
        jo.setStatus(status);
        repo.save(jo);
        return toResponse(jo);
    }

    private JadwalOperasiResponse toResponse(JadwalOperasi jo) {
        return new JadwalOperasiResponse(
                jo.getId(), jo.getPasien().getId(), jo.getPasien().getUser().getNama(),
                jo.getDokter().getId(), jo.getDokter().getUser().getNama(),
                jo.getDokter().getPoli().getNama(),
                jo.getTanggalOperasi(), jo.getJamMulai(), jo.getJenisOperasi(),
                jo.getRuangOperasi(), jo.getStatus(), jo.getCatatan(), jo.getCreatedAt());
    }
}