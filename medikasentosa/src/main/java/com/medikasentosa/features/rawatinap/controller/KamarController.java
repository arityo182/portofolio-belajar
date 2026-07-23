package com.medikasentosa.features.rawatinap.controller;

import com.medikasentosa.features.rawatinap.dto.KamarRequest;
import com.medikasentosa.features.rawatinap.dto.KamarResponse;
import com.medikasentosa.features.rawatinap.service.KamarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller untuk manajemen data master kamar rawat inap (FR-30).
 * GET tersedia untuk semua user; POST/PUT/DELETE khusus admin.
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/kamar")
@Tag(name = "Rawat Inap", description = "API manajemen kamar & rawat inap pasien")
public class KamarController {

    private final KamarService kamarService;

    public KamarController(KamarService kamarService) {
        this.kamarService = kamarService;
    }

    @GetMapping
    @Operation(summary = "Ambil semua kamar", description = "Mengembalikan seluruh daftar kamar (termasuk nonaktif).")
    public List<KamarResponse> getAll() {
        return kamarService.getAll();
    }

    @GetMapping("/tersedia")
    @Operation(summary = "Kamar tersedia",
               description = "Mengembalikan kamar yang aktif & masih ada tempat (terisi < kapasitas).")
    public List<KamarResponse> getAvailable() {
        return kamarService.getAvailable();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tambah kamar (admin)")
    @ApiResponses({@ApiResponse(responseCode = "201"), @ApiResponse(responseCode = "409")})
    public ResponseEntity<KamarResponse> create(@Valid @RequestBody KamarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kamarService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update kamar (admin)")
    public KamarResponse update(@PathVariable Long id, @Valid @RequestBody KamarRequest request) {
        return kamarService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Nonaktifkan kamar (admin)", description = "Soft delete.")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        kamarService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}