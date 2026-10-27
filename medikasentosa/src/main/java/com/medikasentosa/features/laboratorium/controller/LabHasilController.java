package com.medikasentosa.features.laboratorium.controller;

import com.medikasentosa.features.laboratorium.dto.LabHasilRequest;
import com.medikasentosa.features.laboratorium.dto.LabHasilResponse;
import com.medikasentosa.shared.audit.AuditLogger;
import com.medikasentosa.features.laboratorium.service.LabHasilService;
import com.medikasentosa.shared.security.AuthorizationService;
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
    private final AuthorizationService authorization;
    private final AuditLogger auditLogger;

    public LabHasilController(LabHasilService labHasilService,
                              AuthorizationService authorization,
                              AuditLogger auditLogger) {
        this.labHasilService = labHasilService;
        this.authorization = authorization;
        this.auditLogger = auditLogger;
    }

    @GetMapping("/order/{labOrderId}")
    @Operation(summary = "Ambil hasil lab berdasarkan order",
               description = "Mengembalikan hasil laboratorium untuk order tertentu.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200"),
        @ApiResponse(responseCode = "404", description = "Hasil belum tersedia")
    })
    public LabHasilResponse getByLabOrder(@PathVariable Long labOrderId) {
        authorization.requireLabOrderAccess(labOrderId);
        auditLogger.record("READ", "LAB_HASIL", labOrderId);
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
        authorization.requireLabOrderWrite(request.labOrderId());
        LabHasilResponse response = labHasilService.create(request);
        auditLogger.record("CREATE", "LAB_HASIL", request.labOrderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}