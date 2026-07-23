package com.medikasentosa.features.laboratorium.service;

import com.medikasentosa.features.laboratorium.dto.LabHasilRequest;
import com.medikasentosa.features.laboratorium.dto.LabHasilResponse;
import com.medikasentosa.features.laboratorium.entity.LabHasil;
import com.medikasentosa.features.laboratorium.entity.LabOrder;
import com.medikasentosa.features.laboratorium.entity.StatusLab;
import com.medikasentosa.features.laboratorium.repository.LabHasilRepository;
import com.medikasentosa.features.laboratorium.repository.LabOrderRepository;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service untuk logika bisnis pencatatan hasil laboratorium (FR-28).
 * Menangani pengisian hasil, pembacaan, dan otomatis menutup order
 * laboratorium menjadi SELESAI saat hasil diisi.
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class LabHasilService {

    private final LabHasilRepository labHasilRepository;
    private final LabOrderRepository labOrderRepository;

    public LabHasilService(LabHasilRepository labHasilRepository,
                           LabOrderRepository labOrderRepository) {
        this.labHasilRepository = labHasilRepository;
        this.labOrderRepository = labOrderRepository;
    }

    /**
     * Mengisi hasil pemeriksaan laboratorium.
     *
     * Setelah hasil disimpan, status order laboratorium otomatis diubah menjadi
     * SELESAI. Seluruh proses dilakukan dalam satu transaksi.
     *
     * @param request data hasil (labOrderId, hasilText, nilaiNormal, interpretasi)
     * @return data hasil yang berhasil dibuat
     * @throws ResourceNotFoundException  bila order laboratorium tidak ditemukan
     * @throws DuplicateResourceException bila order sudah memiliki hasil
     */
    @Transactional
    public LabHasilResponse create(LabHasilRequest request) {
        LabOrder order = labOrderRepository.findById(request.labOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order laboratorium tidak ditemukan"));

        if (labHasilRepository.existsByLabOrderId(request.labOrderId())) {
            throw new DuplicateResourceException("Order laboratorium ini sudah memiliki hasil");
        }

        LabHasil hasil = LabHasil.builder()
                .labOrder(order)
                .hasilText(request.hasilText())
                .nilaiNormal(request.nilaiNormal())
                .interpretasi(request.interpretasi())
                .build();

        labHasilRepository.save(hasil);

        // Tutup order laboratorium menjadi SELESAI
        order.setStatus(StatusLab.SELESAI);
        labOrderRepository.save(order);

        return toResponse(hasil);
    }

    /**
     * Mengambil hasil laboratorium berdasarkan ID order.
     *
     * @param labOrderId ID order laboratorium
     * @return data hasil laboratorium
     * @throws ResourceNotFoundException bila hasil belum ada
     */
    public LabHasilResponse getByLabOrder(Long labOrderId) {
        LabHasil hasil = labHasilRepository.findByLabOrderId(labOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Hasil laboratorium belum tersedia"));
        return toResponse(hasil);
    }

    private LabHasilResponse toResponse(LabHasil hasil) {
        LabOrder order = hasil.getLabOrder();
        return new LabHasilResponse(
                hasil.getId(),
                order.getId(),
                order.getJenisPemeriksaan(),
                order.getRekamMedis().getPasien().getUser().getNama(),
                hasil.getHasilText(),
                hasil.getNilaiNormal(),
                hasil.getInterpretasi(),
                hasil.getCreatedAt());
    }
}