package com.medikasentosa.features.laboratorium.service;

import com.medikasentosa.features.laboratorium.dto.LabOrderRequest;
import com.medikasentosa.features.laboratorium.dto.LabOrderResponse;
import com.medikasentosa.features.laboratorium.entity.LabOrder;
import com.medikasentosa.features.laboratorium.entity.StatusLab;
import com.medikasentosa.features.laboratorium.repository.LabOrderRepository;
import com.medikasentosa.features.rekammedis.entity.RekamMedis;
import com.medikasentosa.features.rekammedis.repository.RekamMedisRepository;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service untuk logika bisnis manajemen order laboratorium (FR-27).
 * Menangani pembuatan order, pembacaan, dan pembaruan status.
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class LabOrderService {

    private final LabOrderRepository labOrderRepository;
    private final RekamMedisRepository rekamMedisRepository;

    public LabOrderService(LabOrderRepository labOrderRepository,
                           RekamMedisRepository rekamMedisRepository) {
        this.labOrderRepository = labOrderRepository;
        this.rekamMedisRepository = rekamMedisRepository;
    }

    /**
     * Membuat order pemeriksaan laboratorium baru dari rekam medis.
     * Status awal: MENUNGGU.
     *
     * @param request data order baru (rekamMedisId, jenisPemeriksaan, catatan)
     * @return data order yang berhasil dibuat
     * @throws ResourceNotFoundException bila rekam medis tidak ditemukan
     */
    public LabOrderResponse create(LabOrderRequest request) {
        RekamMedis rekamMedis = rekamMedisRepository.findById(request.rekamMedisId())
                .orElseThrow(() -> new ResourceNotFoundException("Rekam medis tidak ditemukan"));

        LabOrder order = LabOrder.builder()
                .rekamMedis(rekamMedis)
                .jenisPemeriksaan(request.jenisPemeriksaan())
                .catatan(request.catatan())
                .status(StatusLab.MENUNGGU)
                .build();

        labOrderRepository.save(order);
        return toResponse(order);
    }

    /**
     * Mengambil seluruh daftar order laboratorium.
     *
     * @return daftar semua order
     */
    public List<LabOrderResponse> getAll() {
        return labOrderRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    /**
     * Mengambil seluruh order laboratorium milik sebuah rekam medis.
     *
     * @param rekamMedisId ID rekam medis
     * @return daftar order lab rekam medis tersebut
     */
    public List<LabOrderResponse> getByRekamMedis(Long rekamMedisId) {
        return labOrderRepository.findByRekamMedisId(rekamMedisId)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Memperbarui status sebuah order laboratorium.
     *
     * @param id     ID order
     * @param status status baru (MENUNGGU / PROSES / SELESAI)
     * @return data order setelah diperbarui
     * @throws ResourceNotFoundException bila order tidak ditemukan
     */
    public LabOrderResponse updateStatus(Long id, StatusLab status) {
        LabOrder order = findOrThrow(id);
        order.setStatus(status);
        labOrderRepository.save(order);
        return toResponse(order);
    }

    private LabOrder findOrThrow(Long id) {
        return labOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order laboratorium tidak ditemukan"));
    }

    private LabOrderResponse toResponse(LabOrder order) {
        return new LabOrderResponse(
                order.getId(), order.getRekamMedis().getId(),
                order.getRekamMedis().getPasien().getUser().getNama(),
                order.getRekamMedis().getPasien().getNoRekamMedis(),
                order.getRekamMedis().getPasien().getNik(),
                order.getJenisPemeriksaan(), order.getCatatan(),
                order.getStatus(), order.getCreatedAt());
    }
}