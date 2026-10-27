package com.medikasentosa.features.rawatinap.controller;

import com.medikasentosa.features.rawatinap.dto.RawatInapRequest;
import com.medikasentosa.features.rawatinap.dto.RawatInapResponse;
import com.medikasentosa.features.rawatinap.service.RawatInapService;
import com.medikasentosa.shared.security.AuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller untuk manajemen rawat inap pasien (FR-30).
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/rawat-inap")
@Tag(name = "Rawat Inap", description = "API manajemen kamar & rawat inap pasien")
public class RawatInapController {

    private final RawatInapService rawatInapService;
    private final AuthorizationService authorization;

    public RawatInapController(RawatInapService rawatInapService, AuthorizationService authorization) {
        this.rawatInapService = rawatInapService;
        this.authorization = authorization;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Semua rawat inap (admin)")
    public List<RawatInapResponse> getAll() {
        return rawatInapService.getAll();
    }

    @GetMapping("/pasien/{pasienId}")
    @Operation(summary = "Rawat inap by pasien")
    public List<RawatInapResponse> getByPasien(@PathVariable Long pasienId) {
        authorization.requireRawatInapOfPasien(pasienId);
        return rawatInapService.getByPasien(pasienId);
    }

    @GetMapping("/aktif")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rawat inap yang masih aktif (admin)")
    public List<RawatInapResponse> getAktif() {
        return rawatInapService.getAktif();
    }

    @PostMapping("/masuk")
    @Operation(summary = "Pasien masuk rawat inap",
               description = "Mencatat pasien masuk kamar. Validasi: pasien & dokter & kamar ada, "
                       + "kamar masih tersedia (terisi < kapasitas). Kamar.terisi bertambah 1 otomatis.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Rawat inap berhasil dicatat"),
        @ApiResponse(responseCode = "404", description = "Pasien/dokter/kamar tidak ditemukan"),
        @ApiResponse(responseCode = "409", description = "Kamar penuh atau tidak tersedia")
    })
    public ResponseEntity<RawatInapResponse> masuk(@Valid @RequestBody RawatInapRequest request) {
        authorization.requireStaff();
        authorization.requireDokterSelfOrAdmin(request.dokterId());
        return ResponseEntity.status(HttpStatus.CREATED).body(rawatInapService.masuk(request));
    }

    @PatchMapping("/{id}/keluar")
    @Operation(summary = "Pasien keluar rawat inap",
               description = "Mencatat pasien pulang. Status → SELESAI, kamar.terisi berkurang 1, "
                       + "tanggalKeluar diisi.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rawat inap selesai"),
        @ApiResponse(responseCode = "404", description = "Rawat inap tidak ditemukan"),
        @ApiResponse(responseCode = "409", description = "Rawat inap sudah selesai sebelumnya")
    })
    public RawatInapResponse keluar(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tanggalKeluar) {
        authorization.requireRawatInapWrite(id);
        return rawatInapService.keluar(id, tanggalKeluar);
    }
}