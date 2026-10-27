package com.medikasentosa.features.antrian.controller;

import com.medikasentosa.features.antrian.dto.AntrianResponse;
import com.medikasentosa.features.antrian.service.AntrianService;
import com.medikasentosa.shared.security.AuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    private final AuthorizationService authorization;

    public AntrianController(AntrianService antrianService, AuthorizationService authorization) {
        this.antrianService = antrianService;
        this.authorization = authorization;
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
        authorization.requireAppointmentAccess(appointmentId);
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
        authorization.requireAppointmentAccess(appointmentId);
        return antrianService.getByAppointment(appointmentId);
    }

    /**
     * Mengambil antrian hari ini. Dokter hanya dapat melihat antrian miliknya
     * sendiri; admin dapat memfilter berdasarkan {@code dokterId}.
     *
     * @param dokterId filter dokter (opsional, hanya admin)
     * @return daftar antrian hari ini
     */
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN','DOKTER')")
    @Operation(summary = "Ambil antrian hari ini",
               description = "Mengembalikan antrian untuk tanggal hari ini. Dokter hanya "
                       + "mendapat antrian miliknya; admin dapat memfilter dengan dokterId.")
    @ApiResponse(responseCode = "200", description = "Daftar antrian hari ini berhasil diambil")
    public List<AntrianResponse> getToday(@RequestParam(required = false) Long dokterId) {
        Long filter = dokterId;
        if (!authorization.isAdmin()) {
            Long ownDokterId = authorization.currentDokterId();
            if (dokterId != null && !dokterId.equals(ownDokterId)) {
                throw new AccessDeniedException("Anda hanya dapat melihat antrian sendiri");
            }
            filter = ownDokterId;
        }
        return antrianService.getByTanggal(LocalDate.now(), filter);
    }

    /**
     * Memanggil antrian (dokter penanggung jawab atau admin).
     *
     * @param id ID antrian yang dipanggil
     * @return data antrian yang dipanggil
     */
    @PatchMapping("/{id}/panggil")
    @PreAuthorize("hasAnyRole('ADMIN','DOKTER')")
    @Operation(summary = "Panggil antrian",
               description = "Mem-broadcast panggilan antrian ke pasien dan panel dokter. "
                       + "Hanya antrian hari ini yang belum selesai/batal yang dapat dipanggil.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Antrian berhasil dipanggil"),
        @ApiResponse(responseCode = "400", description = "Antrian bukan untuk hari ini atau sudah selesai"),
        @ApiResponse(responseCode = "404", description = "Antrian tidak ditemukan")
    })
    public AntrianResponse panggil(@PathVariable Long id) {
        AntrianResponse antrian = antrianService.getById(id);
        authorization.requireAppointmentAccess(antrian.appointmentId());
        return antrianService.panggil(id);
    }

    /**
     * Mengambil seluruh daftar antrian yang tersimpan.
     *
     * @return daftar semua antrian
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ambil semua antrian (admin)",
               description = "Mengembalikan seluruh daftar antrian. Khusus admin.")
    @ApiResponse(responseCode = "200", description = "Daftar antrian berhasil diambil")
    public List<AntrianResponse> getAll() {
        return antrianService.getAll();
    }
}
