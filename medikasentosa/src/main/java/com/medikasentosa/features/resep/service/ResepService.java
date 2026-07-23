package com.medikasentosa.features.resep.service;

import com.medikasentosa.features.obat.entity.Obat;
import com.medikasentosa.features.obat.repository.ObatRepository;
import com.medikasentosa.features.rekammedis.entity.RekamMedis;
import com.medikasentosa.features.rekammedis.repository.RekamMedisRepository;
import com.medikasentosa.features.resep.dto.ResepRequest;
import com.medikasentosa.features.resep.dto.ResepResponse;
import com.medikasentosa.features.resep.entity.Resep;
import com.medikasentosa.features.resep.repository.ResepRepository;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service untuk logika bisnis manajemen resep obat.
 * Menangani penambahan resep pada rekam medis, pembacaan resep per rekam medis,
 * dan penghapusan resep.
 *
 * Sejak Fase 2, resep dapat merujuk ke data master obat melalui {@code obatId},
 * dengan fallback {@code namaObat} untuk data resep lama.
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class ResepService {

    private final ResepRepository resepRepository;
    private final RekamMedisRepository rekamMedisRepository;
    private final ObatRepository obatRepository;

    public ResepService(ResepRepository resepRepository,
                        RekamMedisRepository rekamMedisRepository,
                        ObatRepository obatRepository) {
        this.resepRepository = resepRepository;
        this.rekamMedisRepository = rekamMedisRepository;
        this.obatRepository = obatRepository;
    }

    /**
     * Menambahkan resep obat pada sebuah rekam medis.
     *
     * Sejak Fase 2: bila {@code obatId} diisi, ambil data dari tabel obat;
     * bila null, gunakan {@code namaObat} sebagai fallback.
     *
     * @param request data resep baru (rekamMedisId, obatId opsional, namaObat, dosis, jumlah, aturanPakai)
     * @return data resep yang berhasil dibuat
     * @throws ResourceNotFoundException bila rekam medis atau obat tidak ditemukan
     * @throws IllegalArgumentException bila obatId dan namaObat keduanya kosong
     */
    @Transactional
    public ResepResponse create(ResepRequest request) {
        RekamMedis rekamMedis = rekamMedisRepository.findById(request.rekamMedisId())
                .orElseThrow(() -> new ResourceNotFoundException("Rekam medis tidak ditemukan"));

        // Resolve Obat jika obatId diisi
        Obat obat = null;
        if (request.obatId() != null) {
            obat = obatRepository.findById(request.obatId())
                    .orElseThrow(() -> new ResourceNotFoundException("Obat tidak ditemukan"));
            // === AUTO-REDUCE STOK ===
            if (obat.getStok() < request.jumlah()) {
                throw new IllegalArgumentException(
                        "Stok obat tidak mencukupi (tersisa " + obat.getStok() + ")");
            }
            obat.setStok(obat.getStok() - request.jumlah());
            obatRepository.save(obat);
        }

        // Validasi: minimal salah satu dari obatId atau namaObat harus ada
        String namaObatFallback = request.namaObat();
        if (obat == null && (namaObatFallback == null || namaObatFallback.isBlank())) {
            throw new IllegalArgumentException(
                    "namaObat wajib diisi jika obatId tidak diberikan");
        }

        Resep resep = Resep.builder()
                .rekamMedis(rekamMedis)
                .obat(obat)
                .namaObat(namaObatFallback)
                .dosis(request.dosis())
                .jumlah(request.jumlah())
                .aturanPakai(request.aturanPakai())
                .build();

        resepRepository.save(resep);
        return toResponse(resep);
    }

    /**
     * Mengambil seluruh resep milik sebuah rekam medis.
     *
     * @param rekamMedisId ID rekam medis yang dicari
     * @return daftar resep rekam medis tersebut
     */
    public List<ResepResponse> getByRekamMedis(Long rekamMedisId) {
        return resepRepository.findByRekamMedisId(rekamMedisId)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Menghapus sebuah resep berdasarkan ID.
     *
     * @param id ID resep yang akan dihapus
     * @throws ResourceNotFoundException bila resep tidak ditemukan
     */
    public void delete(Long id) {
        Resep resep = resepRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resep tidak ditemukan"));
        resepRepository.delete(resep);
    }

    private ResepResponse toResponse(Resep resep) {
        return new ResepResponse(
                resep.getId(),
                resep.getRekamMedis().getId(),
                resep.getObat() != null ? resep.getObat().getId() : null,
                resep.getNamaObatTampil(),
                resep.getDosis(),
                resep.getJumlah(),
                resep.getAturanPakai());
    }
}
