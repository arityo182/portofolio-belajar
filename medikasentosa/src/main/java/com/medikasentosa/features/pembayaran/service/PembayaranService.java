package com.medikasentosa.features.pembayaran.service;

import com.medikasentosa.features.billing.entity.*;
import com.medikasentosa.features.billing.repository.BillingRepository;
import com.medikasentosa.features.pembayaran.dto.*;
import com.medikasentosa.features.pembayaran.entity.*;
import com.medikasentosa.features.pembayaran.repository.PembayaranRepository;
import com.medikasentosa.shared.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for payment processing (FR-33, FR-34).
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class PembayaranService {

    private final PembayaranRepository repo;
    private final BillingRepository billingRepo;

    public PembayaranService(PembayaranRepository r, BillingRepository br) { this.repo = r; this.billingRepo = br; }

    /**
     * Process payment for a billing.
     * Validates: billing exists, not yet paid, amount >= total.
     * On success: saves pembayaran + sets billing to LUNAS.
     */
    @Transactional
    public PembayaranResponse proses(PembayaranRequest req) {
        Billing b = billingRepo.findById(req.billingId())
                .orElseThrow(() -> new ResourceNotFoundException("Billing tidak ditemukan"));

        if (b.getStatus() != StatusBilling.BELUM_BAYAR)
            throw new IllegalStateException("Billing sudah " + b.getStatus().name());

        if (req.jumlahBayar().compareTo(b.getTotalHarga()) < 0)
            throw new IllegalArgumentException("Jumlah bayar kurang dari total tagihan (Rp " + b.getTotalHarga() + ")");

        // TODO: Integrasi V-Claim BPJS untuk metode BPJS
        if (req.metodePembayaran() == MetodePembayaran.BPJS) {
            req = new PembayaranRequest(req.billingId(), req.metodePembayaran(),
                    req.jumlahBayar(), "Integrasi V-Claim direncanakan");
        }

        Pembayaran p = Pembayaran.builder().billing(b)
                .metodePembayaran(req.metodePembayaran()).jumlahBayar(req.jumlahBayar())
                .status(StatusPembayaran.BERHASIL).buktiTransfer(req.buktiTransfer())
                .tanggalBayar(LocalDateTime.now()).build();
        repo.save(p);

        b.setStatus(StatusBilling.LUNAS);
        billingRepo.save(b);

        return toResponse(p);
    }

    public PembayaranResponse getByBilling(Long billingId) {
        Pembayaran p = repo.findByBillingId(billingId)
                .orElseThrow(() -> new ResourceNotFoundException("Pembayaran belum ada"));
        return toResponse(p);
    }

    public List<PembayaranResponse> getAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    private PembayaranResponse toResponse(Pembayaran p) {
        return new PembayaranResponse(p.getId(), p.getBilling().getId(),
                p.getBilling().getPasien().getUser().getNama(), p.getBilling().getTotalHarga(),
                p.getMetodePembayaran(), p.getJumlahBayar(), p.getStatus(),
                p.getBuktiTransfer(), p.getTanggalBayar(), p.getCatatan());
    }
}