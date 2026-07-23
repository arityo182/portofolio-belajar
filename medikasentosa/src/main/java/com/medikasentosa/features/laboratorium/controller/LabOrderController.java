package com.medikasentosa.features.laboratorium.controller;

import com.medikasentosa.features.laboratorium.dto.LabOrderRequest;
import com.medikasentosa.features.laboratorium.dto.LabOrderResponse;
import com.medikasentosa.features.laboratorium.entity.StatusLab;
import com.medikasentosa.features.laboratorium.service.LabOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller untuk manajemen order pemeriksaan laboratorium (FR-27).
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/lab/order")
@Tag(name = "Laboratorium", description = "API manajemen order & hasil laboratorium")
public class LabOrderController {

    private final LabOrderService labOrderService;

    public LabOrderController(LabOrderService labOrderService) {
        this.labOrderService = labOrderService;
    }

    @GetMapping
    @Operation(summary = "Ambil semua order lab",
               description = "Mengembalikan seluruh daftar order laboratorium.")
    @ApiResponse(responseCode = "200")
    public List<LabOrderResponse> getAll() {
        return labOrderService.getAll();
    }

    @GetMapping("/rekam-medis/{rekamMedisId}")
    @Operation(summary = "Ambil order lab berdasarkan rekam medis",
               description = "Mengembalikan seluruh order laboratorium milik rekam medis tertentu.")
    @ApiResponse(responseCode = "200")
    public List<LabOrderResponse> getByRekamMedis(@PathVariable Long rekamMedisId) {
        return labOrderService.getByRekamMedis(rekamMedisId);
    }

    @PostMapping
    @Operation(summary = "Buat order lab baru",
               description = "Membuat permintaan pemeriksaan laboratorium untuk sebuah rekam medis. "
                       + "Status awal: MENUNGGU.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order lab berhasil dibuat"),
        @ApiResponse(responseCode = "404", description = "Rekam medis tidak ditemukan")
    })
    public ResponseEntity<LabOrderResponse> create(@Valid @RequestBody LabOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labOrderService.create(request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update status order lab",
               description = "Memperbarui status order laboratorium (MENUNGGU → PROSES → SELESAI).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200"),
        @ApiResponse(responseCode = "404", description = "Order tidak ditemukan")
    })
    public LabOrderResponse updateStatus(@PathVariable Long id, @RequestParam StatusLab status) {
        return labOrderService.updateStatus(id, status);
    }
}