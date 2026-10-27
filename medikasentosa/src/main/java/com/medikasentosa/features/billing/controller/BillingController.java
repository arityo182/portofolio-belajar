package com.medikasentosa.features.billing.controller;

import com.medikasentosa.features.billing.dto.*;
import com.medikasentosa.features.billing.service.BillingService;
import com.medikasentosa.shared.security.AuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for billing management (FR-32).
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/billing")
@Tag(name = "Billing", description = "API manajemen tagihan & pembayaran")
public class BillingController {

    private final BillingService service;
    private final AuthorizationService authorization;

    public BillingController(BillingService s, AuthorizationService authorization) {
        this.service = s;
        this.authorization = authorization;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Semua billing (admin)")
    public List<BillingResponse> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    @Operation(summary = "Detail billing")
    public BillingResponse getById(@PathVariable Long id) {
        authorization.requireBillingAccess(id);
        return service.getById(id);
    }

    @GetMapping("/pasien/{pasienId}")
    @Operation(summary = "Billing by pasien")
    public List<BillingResponse> getByPasien(@PathVariable Long pasienId) {
        authorization.requireBillingOfPasien(pasienId);
        return service.getByPasien(pasienId);
    }

    @GetMapping("/appointment/{appointmentId}")
    @Operation(summary = "Billing by appointment",
               description = "Mengembalikan billing milik appointment tertentu. Pasien hanya "
                       + "dapat mengakses appointment miliknya.")
    public BillingResponse getByAppointment(@PathVariable Long appointmentId) {
        authorization.requireAppointmentAccess(appointmentId);
        return service.getByAppointment(appointmentId);
    }

    @PostMapping("/generate/appointment/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate billing dari appointment (admin)")
    public ResponseEntity<BillingResponse> generate(@PathVariable Long appointmentId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.generateDariAppointment(appointmentId));
    }

    @PostMapping("/{billingId}/item")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tambah item ke billing (admin)")
    public BillingResponse tambahItem(@PathVariable Long billingId, @Valid @RequestBody BillingItemRequest req) {
        return service.tambahItem(billingId, req);
    }

    @PatchMapping("/{id}/batal")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Batalkan billing (admin)")
    public BillingResponse batalkan(@PathVariable Long id) { return service.batalkan(id); }
}