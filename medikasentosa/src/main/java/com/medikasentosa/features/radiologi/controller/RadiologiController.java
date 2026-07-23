package com.medikasentosa.features.radiologi.controller;

import com.medikasentosa.features.radiologi.dto.RadiologiRequest;
import com.medikasentosa.features.radiologi.dto.RadiologiResponse;
import com.medikasentosa.features.radiologi.service.RadiologiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller untuk manajemen order radiologi konvensional (FR-29).
 *
 * <p>Modul ini BERBEDA dari endpoint skrining osteoporosis AI
 * ({@code /api/screening/upload}). Keduanya berjalan paralel —
 * pasien dapat memiliki order radiologi konvensional DAN skrining AI.</p>
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/radiologi")
@Tag(name = "Radiologi", description = "API manajemen order radiologi konvensional")
public class RadiologiController {

    private final RadiologiService radiologiService;

    public RadiologiController(RadiologiService radiologiService) {
        this.radiologiService = radiologiService;
    }

    @GetMapping
    @Operation(summary = "Ambil semua order radiologi",
               description = "Mengembalikan seluruh daftar order radiologi konvensional.")
    @ApiResponse(responseCode = "200")
    public List<RadiologiResponse> getAll() {
        return radiologiService.getAll();
    }

    @GetMapping("/rekam-medis/{rekamMedisId}")
    @Operation(summary = "Ambil order radiologi berdasarkan rekam medis",
               description = "Mengembalikan seluruh order radiologi milik rekam medis tertentu.")
    @ApiResponse(responseCode = "200")
    public List<RadiologiResponse> getByRekamMedis(@PathVariable Long rekamMedisId) {
        return radiologiService.getByRekamMedis(rekamMedisId);
    }

    @PostMapping
    @Operation(summary = "Buat order radiologi baru",
               description = "Membuat permintaan radiologi konvensional untuk sebuah rekam medis. "
                       + "Status awal: MENUNGGU.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order radiologi berhasil dibuat"),
        @ApiResponse(responseCode = "404", description = "Rekam medis tidak ditemukan")
    })
    public ResponseEntity<RadiologiResponse> create(@Valid @RequestBody RadiologiRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(radiologiService.create(request));
    }

    @PutMapping("/{id}/hasil")
    @Operation(summary = "Isi hasil radiologi",
               description = "Mengisi hasil deskripsi + URL gambar radiologi. "
                       + "Status order otomatis berubah menjadi SELESAI.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hasil berhasil disimpan"),
        @ApiResponse(responseCode = "404", description = "Order tidak ditemukan")
    })
    public RadiologiResponse updateHasil(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String hasilDeskripsi = body.get("hasilDeskripsi");
        String gambarUrl = body.get("gambarUrl");
        return radiologiService.updateHasil(id, hasilDeskripsi, gambarUrl);
    }
}