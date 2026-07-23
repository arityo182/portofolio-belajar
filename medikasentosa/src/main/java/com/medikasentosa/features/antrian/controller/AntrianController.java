package com.medikasentosa.features.antrian.controller;

import com.medikasentosa.features.antrian.dto.AntrianResponse;
import com.medikasentosa.features.antrian.service.AntrianService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller untuk manajemen antrian.
 * Menyediakan operasi generasi nomor antrian untuk sebuah appointment
 * serta pembacaan antrian.
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/antrian")
@Tag(name = "Antrian", description = "API manajemen nomor antrian janji temu")
public class AntrianController {

    private final AntrianService antrianService;

    public AntrianController(AntrianService antrianService) {
        this.antrianService = antrianService;
    }

    /**
     * Menggenerate nomor antrian untuk sebuah appointment.
     *
     * @param appointmentId ID appointment yang akan diberi antrian
     * @return data antrian yang berhasil dibuat (HTTP 201)
     */
    @PostMapping("/generate/{appointmentId}")
    @Operation(summary = "Generate antrian",
               description = "Menggenerate nomor antrian untuk sebuah appointment. "
                       + "Nomor urut mengikuti jumlah antrian pada jadwal dan tanggal yang sama.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Antrian berhasil dibuat"),
        @ApiResponse(responseCode = "404", description = "Appointment tidak ditemukan"),
        @ApiResponse(responseCode = "409", description = "Antrian untuk appointment ini sudah ada")
    })
    public ResponseEntity<AntrianResponse> generate(@PathVariable Long appointmentId) {
        AntrianResponse response = antrianService.generateAntrian(appointmentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Mengambil antrian milik sebuah appointment.
     *
     * @param appointmentId ID appointment yang dicari
     * @return data antrian appointment tersebut
     */
    @GetMapping("/appointment/{appointmentId}")
    @Operation(summary = "Ambil antrian berdasarkan appointment",
               description = "Mengembalikan antrian milik appointment tertentu.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Antrian ditemukan"),
        @ApiResponse(responseCode = "404", description = "Antrian tidak ditemukan")
    })
    public AntrianResponse getByAppointment(@PathVariable Long appointmentId) {
        return antrianService.getByAppointment(appointmentId);
    }

    /**
     * Mengambil seluruh daftar antrian yang tersimpan.
     *
     * @return daftar semua antrian
     */
    @GetMapping
    @Operation(summary = "Ambil semua antrian",
               description = "Mengembalikan seluruh daftar antrian yang tersimpan.")
    @ApiResponse(responseCode = "200", description = "Daftar antrian berhasil diambil")
    public List<AntrianResponse> getAll() {
        return antrianService.getAll();
    }
}
