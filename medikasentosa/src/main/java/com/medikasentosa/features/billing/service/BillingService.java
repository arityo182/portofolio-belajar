package com.medikasentosa.features.billing.service;

import com.medikasentosa.features.appointment.entity.Appointment;
import com.medikasentosa.features.appointment.repository.AppointmentRepository;
import com.medikasentosa.features.billing.dto.*;
import com.medikasentosa.features.billing.entity.*;
import com.medikasentosa.features.billing.repository.*;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.shared.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for billing logic (FR-32).
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class BillingService {

    private final BillingRepository billingRepo;
    private final BillingItemRepository itemRepo;
    private final AppointmentRepository appointmentRepo;

    public BillingService(BillingRepository br, BillingItemRepository ir, AppointmentRepository ar) {
        this.billingRepo = br; this.itemRepo = ir; this.appointmentRepo = ar;
    }

    /**
     * Generate billing automatically from a completed appointment.
     * Creates one BillingItem: "Biaya Konsultasi - {namaPoli}".
     */
    @Transactional
    public BillingResponse generateDariAppointment(Long appointmentId) {
        Appointment a = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment tidak ditemukan"));
        Pasien p = a.getPasien();
        String poli = a.getDokter().getPoli().getNama();
        BigDecimal fee = BigDecimal.valueOf(150000); // default consultation fee

        Billing b = Billing.builder().pasien(p).totalHarga(fee)
                .status(StatusBilling.BELUM_BAYAR).tanggalTagihan(LocalDate.now()).build();
        b = billingRepo.save(b);

        BillingItem item = BillingItem.builder().billing(b)
                .deskripsi("Biaya Konsultasi - " + poli).jumlah(1)
                .hargaSatuan(fee).subtotal(fee).build();
        itemRepo.save(item);

        return toResponse(b);
    }

    /**
     * Add manual item to billing. Recalculates total.
     */
    @Transactional
    public BillingResponse tambahItem(Long billingId, BillingItemRequest req) {
        Billing b = findOrThrow(billingId);
        if (b.getStatus() != StatusBilling.BELUM_BAYAR)
            throw new IllegalStateException("Hanya billing BELUM_BAYAR yang bisa ditambah item");

        BigDecimal sub = req.hargaSatuan().multiply(BigDecimal.valueOf(req.jumlah()));
        BillingItem item = BillingItem.builder().billing(b)
                .deskripsi(req.deskripsi()).jumlah(req.jumlah())
                .hargaSatuan(req.hargaSatuan()).subtotal(sub).build();
        itemRepo.save(item);

        return hitungTotal(billingId);
    }

    /** Recalculate total from all items. */
    @Transactional
    public BillingResponse hitungTotal(Long billingId) {
        Billing b = findOrThrow(billingId);
        BigDecimal total = itemRepo.findByBillingId(billingId).stream()
                .map(BillingItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        b.setTotalHarga(total);
        billingRepo.save(b);
        return toResponse(b);
    }

    public List<BillingResponse> getAll() {
        return billingRepo.findAll().stream().map(this::toResponse).toList();
    }

    public List<BillingResponse> getByPasien(Long pasienId) {
        return billingRepo.findByPasienId(pasienId).stream().map(this::toResponse).toList();
    }

    public BillingResponse getById(Long id) { return toResponse(findOrThrow(id)); }

    @Transactional
    public BillingResponse batalkan(Long id) {
        Billing b = findOrThrow(id);
        b.setStatus(StatusBilling.DIBATALKAN);
        billingRepo.save(b);
        return toResponse(b);
    }

    private Billing findOrThrow(Long id) {
        return billingRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Billing tidak ditemukan"));
    }

    private BillingResponse toResponse(Billing b) {
        List<BillingItemResponse> items = itemRepo.findByBillingId(b.getId()).stream()
                .map(i->new BillingItemResponse(i.getId(),i.getBilling().getId(),i.getDeskripsi(),i.getJumlah(),i.getHargaSatuan(),i.getSubtotal())).toList();
        return new BillingResponse(b.getId(),b.getPasien().getId(),b.getPasien().getUser().getNama(),b.getTotalHarga(),b.getStatus(),b.getTanggalTagihan(),b.getCatatan(),items);
    }
}