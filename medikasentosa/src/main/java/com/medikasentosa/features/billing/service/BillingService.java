package com.medikasentosa.features.billing.service;

import com.medikasentosa.features.appointment.entity.Appointment;
import com.medikasentosa.features.appointment.entity.StatusAppointment;
import com.medikasentosa.features.appointment.repository.AppointmentRepository;
import com.medikasentosa.features.billing.dto.*;
import com.medikasentosa.features.billing.entity.*;
import com.medikasentosa.features.billing.repository.*;
import com.medikasentosa.features.laboratorium.repository.LabOrderRepository;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.radiologi.repository.RadiologiRepository;
import com.medikasentosa.features.rekammedis.repository.RekamMedisRepository;
import com.medikasentosa.features.resep.entity.Resep;
import com.medikasentosa.features.resep.repository.ResepRepository;
import com.medikasentosa.shared.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for billing logic (FR-32).
 *
 * <p>Tarif penunjang memakai tarif default internal karena aplikasi belum
 * memiliki master tarif laboratorium/radiologi. Tarif konsultasi Rp150.000,
 * laboratorium Rp75.000 per order, radiologi Rp100.000 per order, dan obat
 * dihitung dari harga master obat × jumlah resep. Item-item ini otomatis
 * dimasukkan saat billing digenerate dari appointment yang sudah memiliki
 * rekam medis.</p>
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    private static final BigDecimal TARIF_KONSULTASI = new BigDecimal("150000");
    private static final BigDecimal TARIF_LAB = new BigDecimal("75000");
    private static final BigDecimal TARIF_RADIOLOGI = new BigDecimal("100000");

    private final BillingRepository billingRepo;
    private final BillingItemRepository itemRepo;
    private final AppointmentRepository appointmentRepo;
    private final RekamMedisRepository rekamMedisRepo;
    private final LabOrderRepository labOrderRepo;
    private final RadiologiRepository radiologiRepo;
    private final ResepRepository resepRepo;

    public BillingService(BillingRepository br, BillingItemRepository ir,
                          AppointmentRepository ar, RekamMedisRepository rr,
                          LabOrderRepository lor, RadiologiRepository radr,
                          ResepRepository resr) {
        this.billingRepo = br;
        this.itemRepo = ir;
        this.appointmentRepo = ar;
        this.rekamMedisRepo = rr;
        this.labOrderRepo = lor;
        this.radiologiRepo = radr;
        this.resepRepo = resr;
    }

    /**
     * Generate billing otomatis dari sebuah appointment. Billing memuat biaya
     * konsultasi serta seluruh item penunjang yang tercatat pada rekam medis
     * appointment tersebut (lab, radiologi, dan obat). Satu appointment hanya
     * boleh memiliki satu billing.
     *
     * @param appointmentId ID appointment
     * @return billing yang dibuat beserta item-itemnya
     * @throws ResourceNotFoundException  bila appointment tidak ditemukan
     * @throws DuplicateResourceException bila billing appointment sudah ada
     * @throws IllegalArgumentException   bila appointment berstatus BATAL
     */
    @Transactional
    public BillingResponse generateDariAppointment(Long appointmentId) {
        log.info("Generating billing for appointmentId={}", appointmentId);
        Appointment a = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment tidak ditemukan"));

        if (a.getStatus() == StatusAppointment.BATAL) {
            throw new IllegalArgumentException("Tidak dapat membuat billing untuk appointment yang dibatalkan");
        }
        if (billingRepo.existsByAppointmentId(appointmentId)) {
            throw new DuplicateResourceException("Billing untuk appointment ini sudah ada");
        }

        Pasien p = a.getPasien();
        String poli = a.getDokter().getPoli().getNama();

        Billing b = Billing.builder()
                .pasien(p)
                .appointment(a)
                .totalHarga(BigDecimal.ZERO)
                .status(StatusBilling.BELUM_BAYAR)
                .tanggalTagihan(LocalDate.now())
                .build();
        try {
            // flush agar unique constraint appointment_id langsung terdeteksi
            b = billingRepo.saveAndFlush(b);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Billing untuk appointment ini sudah ada");
        }

        final Billing billing = b;
        List<BillingItem> items = new ArrayList<>();
        items.add(item(billing, "Biaya Konsultasi - " + poli, 1, TARIF_KONSULTASI));

        Long billingId = billing.getId();
        rekamMedisRepo.findByAppointmentId(appointmentId).ifPresent(rm -> {
            labOrderRepo.findByRekamMedisId(rm.getId()).forEach(order ->
                    items.add(item(billing, "Biaya Laboratorium - " + order.getJenisPemeriksaan(),
                            1, TARIF_LAB)));

            radiologiRepo.findByRekamMedisId(rm.getId()).forEach(rad ->
                    items.add(item(billing, "Biaya Radiologi - " + rad.getJenisRadiologi(),
                            1, TARIF_RADIOLOGI)));

            for (Resep resep : resepRepo.findByRekamMedisId(rm.getId())) {
                if (resep.getObat() == null || resep.getObat().getHarga() == null
                        || resep.getJumlah() == null) {
                    continue;
                }
                items.add(item(billing, "Biaya Obat - " + resep.getNamaObatTampil(),
                        resep.getJumlah(), resep.getObat().getHarga()));
            }
        });

        itemRepo.saveAll(items);
        BigDecimal total = items.stream()
                .map(BillingItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        b.setTotalHarga(total);
        billingRepo.save(b);

        log.info("Billing {} generated for appointment {} with {} item(s)",
                billingId, appointmentId, items.size());
        return toResponse(b);
    }

    /**
     * Add manual item to billing. Recalculates total.
     */
    @Transactional
    public BillingResponse tambahItem(Long billingId, BillingItemRequest req) {
        log.info("Adding item to billing id={}", billingId);
        Billing b = findOrThrow(billingId);
        if (b.getStatus() != StatusBilling.BELUM_BAYAR) {
            log.warn("Cannot add item to billing {} with status {}", billingId, b.getStatus());
            throw new IllegalStateException("Hanya billing BELUM_BAYAR yang bisa ditambah item");
        }

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

    /** Billing milik sebuah appointment (untuk navigasi dari appointment/rekam medis). */
    public BillingResponse getByAppointment(Long appointmentId) {
        return billingRepo.findByAppointmentId(appointmentId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Billing untuk appointment ini belum dibuat"));
    }

    @Transactional
    public BillingResponse batalkan(Long id) {
        log.info("Cancelling billing id={}", id);
        Billing b = findOrThrow(id);
        b.setStatus(StatusBilling.DIBATALKAN);
        billingRepo.save(b);
        return toResponse(b);
    }

    private Billing findOrThrow(Long id) {
        return billingRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Billing tidak ditemukan"));
    }

    private BillingItem item(Billing billing, String deskripsi, int jumlah, BigDecimal hargaSatuan) {
        BigDecimal subtotal = hargaSatuan.multiply(BigDecimal.valueOf(jumlah));
        return BillingItem.builder()
                .billing(billing)
                .deskripsi(deskripsi)
                .jumlah(jumlah)
                .hargaSatuan(hargaSatuan)
                .subtotal(subtotal)
                .build();
    }

    private BillingResponse toResponse(Billing b) {
        List<BillingItemResponse> items = itemRepo.findByBillingId(b.getId()).stream()
                .map(i -> new BillingItemResponse(i.getId(), i.getBilling().getId(), i.getDeskripsi(),
                        i.getJumlah(), i.getHargaSatuan(), i.getSubtotal())).toList();
        return new BillingResponse(b.getId(), b.getPasien().getId(), b.getPasien().getUser().getNama(),
                b.getAppointment() != null ? b.getAppointment().getId() : null,
                b.getTotalHarga(), b.getStatus(), b.getTanggalTagihan(), b.getCatatan(), items);
    }
}
