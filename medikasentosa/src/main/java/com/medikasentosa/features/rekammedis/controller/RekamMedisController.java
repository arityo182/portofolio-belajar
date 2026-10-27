package com.medikasentosa.features.rekammedis.controller;

import com.medikasentosa.features.rekammedis.dto.RekamMedisRequest;
import com.medikasentosa.features.rekammedis.dto.RekamMedisResponse;
import com.medikasentosa.features.rekammedis.service.RekamMedisService;
import com.medikasentosa.shared.audit.AuditLogger;
import com.medikasentosa.shared.security.AuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller untuk manajemen rekam medis.
 * Menyediakan operasi pembuatan rekam medis dari appointment serta pembacaan
 * rekam medis berdasarkan ID, pasien, maupun appointment.
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/rekam-medis")
@Tag(name = "Rekam Medis", description = "API manajemen rekam medis pasien")
public class RekamMedisController {

    private final RekamMedisService rekamMedisService;
    private final AuthorizationService authorization;
    private final AuditLogger auditLogger;

    public RekamMedisController(RekamMedisService rekamMedisService,
                                AuthorizationService authorization,
                                AuditLogger auditLogger) {
        this.rekamMedisService = rekamMedisService;
        this.authorization = authorization;
        this.auditLogger = auditLogger;
    }

    /**
     * Mengambil detail satu rekam medis berdasarkan ID.
     *
     * @param id ID rekam medis yang dicari
     * @return data rekam medis yang ditemukan
     */
    @GetMapping("/{id}")
    @Operation(summary = "Ambil rekam medis berdasarkan ID",
               description = "Mengembalikan detail satu rekam medis sesuai ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rekam medis ditemukan"),
        @ApiResponse(responseCode = "404", description = "Rekam medis tidak ditemukan")
    })
    public RekamMedisResponse getById(@PathVariable Long id) {
        authorization.requireRekamMedisAccess(id);
        auditLogger.record("READ", "REKAM_MEDIS", id);
        return rekamMedisService.getById(id);
    }

    /**
     * Mengambil seluruh rekam medis milik seorang pasien (riwayat rekam medis).
     *
     * @param pasienId ID pasien yang dicari
     * @return daftar rekam medis pasien tersebut
     */
    @GetMapping("/pasien/{pasienId}")
    @Operation(summary = "Ambil rekam medis berdasarkan pasien",
               description = "Mengembalikan seluruh riwayat rekam medis milik pasien tertentu.")
    @ApiResponse(responseCode = "200", description = "Daftar rekam medis berhasil diambil")
    public List<RekamMedisResponse> getByPasien(@PathVariable Long pasienId) {
        authorization.requireRekamMedisOfPasien(pasienId);
        auditLogger.record("READ_LIST", "REKAM_MEDIS_PASIEN", pasienId);
        return rekamMedisService.getByPasien(pasienId);
    }

    /**
     * Mengambil seluruh rekam medis yang dibuat oleh seorang dokter (untuk panel dokter).
     *
     * @param dokterId ID dokter yang dicari
     * @return daftar rekam medis yang dibuat dokter tersebut
     */
    @GetMapping("/dokter/{dokterId}")
    @Operation(summary = "Ambil rekam medis berdasarkan dokter",
               description = "Mengembalikan seluruh rekam medis yang dibuat dokter tertentu.")
    @ApiResponse(responseCode = "200", description = "Daftar rekam medis berhasil diambil")
    public List<RekamMedisResponse> getByDokter(@PathVariable Long dokterId) {
        authorization.requireDokterSelfOrAdmin(dokterId);
        auditLogger.record("READ_LIST", "REKAM_MEDIS_DOKTER", dokterId);
        return rekamMedisService.getByDokter(dokterId);
    }

    /**
     * Mengambil rekam medis milik sebuah appointment.
     *
     * @param appointmentId ID appointment yang dicari
     * @return data rekam medis appointment tersebut
     */
    @GetMapping("/appointment/{appointmentId}")
    @Operation(summary = "Ambil rekam medis berdasarkan appointment",
               description = "Mengembalikan rekam medis milik appointment tertentu.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rekam medis ditemukan"),
        @ApiResponse(responseCode = "404", description = "Rekam medis tidak ditemukan")
    })
    public RekamMedisResponse getByAppointment(@PathVariable Long appointmentId) {
        authorization.requireAppointmentAccess(appointmentId);
        auditLogger.record("READ", "REKAM_MEDIS_APPOINTMENT", appointmentId);
        return rekamMedisService.getByAppointment(appointmentId);
    }

    /**
     * Membuat rekam medis baru dari sebuah appointment. Appointment terkait akan
     * ditandai SELESAI setelah rekam medis berhasil dibuat.
     *
     * @param request data rekam medis baru (appointmentId, diagnosa, tindakan, catatan)
     * @return data rekam medis yang berhasil dibuat (HTTP 201)
     */
    @PostMapping
    @Operation(summary = "Buat rekam medis baru",
               description = "Membuat rekam medis dari sebuah appointment. Pasien dan dokter "
                       + "diturunkan dari appointment, dan status appointment menjadi SELESAI.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Rekam medis berhasil dibuat"),
        @ApiResponse(responseCode = "400", description = "Data rekam medis tidak valid"),
        @ApiResponse(responseCode = "404", description = "Appointment tidak ditemukan"),
        @ApiResponse(responseCode = "409", description = "Appointment sudah memiliki rekam medis")
    })
    public ResponseEntity<RekamMedisResponse> create(@Valid @RequestBody RekamMedisRequest request) {
        authorization.requireAppointmentWrite(request.appointmentId());
        RekamMedisResponse response = rekamMedisService.create(request);
        auditLogger.record("CREATE", "REKAM_MEDIS", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
