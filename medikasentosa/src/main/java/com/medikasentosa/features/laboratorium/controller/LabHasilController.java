package com.medikasentosa.features.laboratorium.controller;

import com.medikasentosa.features.laboratorium.dto.LabHasilRequest;
import com.medikasentosa.features.laboratorium.dto.LabHasilResponse;
import com.medikasentosa.features.laboratorium.service.LabHasilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller untuk manajemen hasil pemeriksaan laboratorium (FR-28).
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/lab/hasil")
@Tag(name = "Laboratorium", description = "API manajemen order & hasil laboratorium")
public class LabHasilController {

    private final LabHasilService labHasilService;

    public LabHasilController(LabHasilService labHasilService) {
        this.labHasilService = labHasilService;
    }

    @GetMapping("/order/{labOrderId}")
    @Operation(summary = "Ambil hasil lab berdasarkan order",
               description = "Mengembalikan hasil laboratorium untuk order tertentu.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200"),
        @ApiResponse(responseCode = "404", description = "Hasil belum tersedia")
    })
    public LabHasilResponse getByLabOrder(@PathVariable Long labOrderId) {
        return labHasilService.getByLabOrder(labOrderId);
    }

    @PostMapping
    @Operation(summary = "Isi hasil lab",
               description = "Mengisi hasil pemeriksaan laboratorium. Status order otomatis "
                       + "berubah menjadi SELESAI. Satu order hanya dapat memiliki satu hasil.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Hasil berhasil disimpan"),
        @ApiResponse(responseCode = "404", description = "Order tidak ditemukan"),
        @ApiResponse(responseCode = "409", description = "Order sudah memiliki hasil")
    })
    public ResponseEntity<LabHasilResponse> create(@Valid @RequestBody LabHasilRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labHasilService.create(request));
    }
}