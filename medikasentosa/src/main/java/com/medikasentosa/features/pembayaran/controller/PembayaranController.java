package com.medikasentosa.features.pembayaran.controller;

import com.medikasentosa.features.pembayaran.dto.*;
import com.medikasentosa.features.pembayaran.service.PembayaranService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for payment processing (FR-33, FR-34).
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/pembayaran")
@Tag(name = "Pembayaran", description = "API pemrosesan pembayaran")
public class PembayaranController {

    private final PembayaranService service;
    public PembayaranController(PembayaranService s) { this.service = s; }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Semua pembayaran (admin)")
    public List<PembayaranResponse> getAll() { return service.getAll(); }

    @GetMapping("/billing/{billingId}")
    @Operation(summary = "Pembayaran by billing")
    public PembayaranResponse getByBilling(@PathVariable Long billingId) {
        return service.getByBilling(billingId);
    }

    @PostMapping("/proses")
    @Operation(summary = "Proses pembayaran",
               description = "Validasi: billing ada, belum lunas, jumlah >= total. "
                       + "Sukses → billing LUNAS.")
    public ResponseEntity<PembayaranResponse> proses(@Valid @RequestBody PembayaranRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.proses(req));
    }
}