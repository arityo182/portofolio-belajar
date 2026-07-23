package com.medikasentosa.features.jadwaloperasi.controller;

import com.medikasentosa.features.jadwaloperasi.dto.JadwalOperasiRequest;
import com.medikasentosa.features.jadwaloperasi.dto.JadwalOperasiResponse;
import com.medikasentosa.features.jadwaloperasi.entity.StatusOperasi;
import com.medikasentosa.features.jadwaloperasi.service.JadwalOperasiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
 * Controller untuk penjadwalan operasi (FR-31).
 * GET: semua user; POST & PATCH status: admin only.
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/operasi")
@Tag(name = "Jadwal Operasi", description = "API penjadwalan operasi")
public class JadwalOperasiController {

    private final JadwalOperasiService service;

    public JadwalOperasiController(JadwalOperasiService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Semua jadwal operasi")
    public List<JadwalOperasiResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/pasien/{pasienId}")
    @Operation(summary = "Jadwal operasi by pasien")
    public List<JadwalOperasiResponse> getByPasien(@PathVariable Long pasienId) {
        return service.getByPasien(pasienId);
    }

    @GetMapping("/dokter/{dokterId}")
    @Operation(summary = "Jadwal operasi by dokter")
    public List<JadwalOperasiResponse> getByDokter(@PathVariable Long dokterId) {
        return service.getByDokter(dokterId);
    }

    @GetMapping("/tanggal")
    @Operation(summary = "Jadwal operasi by tanggal")
    public List<JadwalOperasiResponse> getByTanggal(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tanggal) {
        return service.getByTanggal(tanggal);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buat jadwal operasi (admin)",
               description = "Status awal: TERJADWAL. Validasi: tanggal tidak lampau.")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<JadwalOperasiResponse> create(@Valid @RequestBody JadwalOperasiRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update status operasi (admin)",
               description = "Status: TERJADWAL → BERLANGSUNG → SELESAI / BATAL.")
    public JadwalOperasiResponse updateStatus(@PathVariable Long id,
                                               @RequestParam StatusOperasi status) {
        return service.updateStatus(id, status);
    }
}