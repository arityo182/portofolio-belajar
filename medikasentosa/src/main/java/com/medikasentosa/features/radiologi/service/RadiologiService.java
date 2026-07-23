package com.medikasentosa.features.radiologi.service;

import com.medikasentosa.features.radiologi.dto.RadiologiRequest;
import com.medikasentosa.features.radiologi.dto.RadiologiResponse;
import com.medikasentosa.features.radiologi.entity.Radiologi;
import com.medikasentosa.features.radiologi.entity.StatusRadiologi;
import com.medikasentosa.features.radiologi.repository.RadiologiRepository;
import com.medikasentosa.features.rekammedis.entity.RekamMedis;
import com.medikasentosa.features.rekammedis.repository.RekamMedisRepository;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service untuk logika bisnis manajemen order radiologi konvensional (FR-29).
 *
 * <p>Modul ini berjalan PARALEL dengan modul Screening (osteoporosis AI).
 * Screening adalah skrining berbasis AI dengan model deep learning,
 * sedangkan modul ini adalah order radiologi konvensional dengan hasil
 * deskriptif yang ditulis dokter radiologi.</p>
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class RadiologiService {

    private final RadiologiRepository radiologiRepository;
    private final RekamMedisRepository rekamMedisRepository;

    public RadiologiService(RadiologiRepository radiologiRepository,
                            RekamMedisRepository rekamMedisRepository) {
        this.radiologiRepository = radiologiRepository;
        this.rekamMedisRepository = rekamMedisRepository;
    }

    /**
     * Membuat order radiologi baru dari rekam medis.
     * Status awal: MENUNGGU.
     *
     * @param request data order baru (rekamMedisId, jenisRadiologi, catatanDokter)
     * @return data order yang berhasil dibuat
     * @throws ResourceNotFoundException bila rekam medis tidak ditemukan
     */
    public RadiologiResponse create(RadiologiRequest request) {
        RekamMedis rekamMedis = rekamMedisRepository.findById(request.rekamMedisId())
                .orElseThrow(() -> new ResourceNotFoundException("Rekam medis tidak ditemukan"));

        Radiologi radiologi = Radiologi.builder()
                .rekamMedis(rekamMedis)
                .jenisRadiologi(request.jenisRadiologi())
                .catatanDokter(request.catatanDokter())
                .status(StatusRadiologi.MENUNGGU)
                .build();

        radiologiRepository.save(radiologi);
        return toResponse(radiologi);
    }

    /**
     * Mengambil seluruh daftar order radiologi.
     *
     * @return daftar semua order
     */
    public List<RadiologiResponse> getAll() {
        return radiologiRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    /**
     * Mengambil seluruh order radiologi milik sebuah rekam medis.
     *
     * @param rekamMedisId ID rekam medis
     * @return daftar order radiologi rekam medis tersebut
     */
    public List<RadiologiResponse> getByRekamMedis(Long rekamMedisId) {
        return radiologiRepository.findByRekamMedisId(rekamMedisId)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Mengisi hasil radiologi: hasil deskripsi + gambar URL + set status SELESAI.
     *
     * @param id             ID order radiologi
     * @param hasilDeskripsi hasil bacaan dokter radiologi
     * @param gambarUrl      URL/path gambar hasil radiologi (opsional)
     * @return data order setelah hasil diisi
     * @throws ResourceNotFoundException bila order tidak ditemukan
     */
    public RadiologiResponse updateHasil(Long id, String hasilDeskripsi, String gambarUrl) {
        Radiologi radiologi = findOrThrow(id);
        radiologi.setHasilDeskripsi(hasilDeskripsi);
        radiologi.setGambarUrl(gambarUrl);
        radiologi.setStatus(StatusRadiologi.SELESAI);
        radiologiRepository.save(radiologi);
        return toResponse(radiologi);
    }

    private Radiologi findOrThrow(Long id) {
        return radiologiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order radiologi tidak ditemukan"));
    }

    private RadiologiResponse toResponse(Radiologi r) {
        return new RadiologiResponse(
                r.getId(), r.getRekamMedis().getId(),
                r.getRekamMedis().getPasien().getUser().getNama(),
                r.getRekamMedis().getPasien().getNoRekamMedis(),
                r.getRekamMedis().getPasien().getNik(),
                r.getJenisRadiologi(), r.getHasilDeskripsi(),
                r.getCatatanDokter(), r.getGambarUrl(),
                r.getStatus(), r.getCreatedAt());
    }
}