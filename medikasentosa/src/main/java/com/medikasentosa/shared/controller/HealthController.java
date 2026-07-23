package com.medikasentosa.shared.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller untuk pengecekan kesehatan (health check) layanan backend.
 * Berguna untuk monitoring dan memastikan service berjalan.
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "API pengecekan status layanan")
public class HealthController {

    /**
     * Mengembalikan status kesehatan layanan backend.
     *
     * @return map berisi status ("UP") dan nama service
     */
    @GetMapping("/health")
    @Operation(summary = "Health check",
               description = "Mengembalikan status layanan backend untuk keperluan monitoring.")
    @ApiResponse(responseCode = "200", description = "Layanan aktif")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "Medika Sentosa Backend");
    }
}