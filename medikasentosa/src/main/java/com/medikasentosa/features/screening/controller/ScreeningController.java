package com.medikasentosa.features.screening.controller;

import com.medikasentosa.features.screening.dto.ScreeningResponse;
import com.medikasentosa.features.screening.service.ScreeningService;
import com.medikasentosa.shared.audit.AuditLogger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Controller untuk fitur screening osteoporosis berbasis machine learning.
 * Menangani upload citra X-ray untuk diprediksi serta pengambilan riwayat screening pengguna.
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/screening")
@Tag(name = "Screening", description = "API screening osteoporosis berbasis machine learning")
public class ScreeningController {

    private final ScreeningService screeningService;
    private final AuditLogger auditLogger;

    public ScreeningController(ScreeningService screeningService, AuditLogger auditLogger) {
        this.screeningService = screeningService;
        this.auditLogger = auditLogger;
    }

    /**
     * Mengunggah citra X-ray untuk diprediksi oleh model machine learning (FastAPI),
     * lalu menyimpan hasilnya ke riwayat pengguna.
     */
    @PostMapping("/upload")
    @Operation(summary = "Upload citra X-ray untuk screening",
               description = "Mengirim citra X-ray ke model ML dan menyimpan hasil prediksi ke riwayat.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Screening berhasil diproses"),
        @ApiResponse(responseCode = "400", description = "File tidak valid"),
        @ApiResponse(responseCode = "404", description = "User tidak ditemukan")
    })
    public ScreeningResponse upload(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        String email = authentication.getName();
        ScreeningResponse response = screeningService.screen(file, email);
        auditLogger.record("CREATE", "SCREENING", response.id());
        return response;
    }

    /**
     * Mengambil riwayat screening milik pengguna yang sedang login.
     */
    @GetMapping("/history")
    @Operation(summary = "Riwayat screening",
               description = "Mengembalikan daftar riwayat screening pengguna, terbaru lebih dulu.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Riwayat berhasil diambil"),
        @ApiResponse(responseCode = "404", description = "User tidak ditemukan")
    })
    public List<ScreeningResponse> history(Authentication authentication) {
        String email = authentication.getName();
        auditLogger.record("READ_LIST", "SCREENING_HISTORY", email);
        return screeningService.getHistory(email);
    }
}
