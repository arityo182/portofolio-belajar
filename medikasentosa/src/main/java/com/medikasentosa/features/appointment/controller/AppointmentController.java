package com.medikasentosa.features.appointment.controller;

import com.medikasentosa.features.appointment.dto.AppointmentRequest;
import com.medikasentosa.features.appointment.dto.AppointmentResponse;
import com.medikasentosa.features.appointment.entity.StatusAppointment;
import com.medikasentosa.features.appointment.service.AppointmentService;
import com.medikasentosa.shared.security.AuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller untuk manajemen janji temu (appointment).
 * Menyediakan operasi pembuatan, pembacaan, dan pembaruan status appointment.
 * Nomor antrian digenerate terpisah melalui modul antrian.
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/appointment")
@Tag(name = "Appointment", description = "API manajemen janji temu pasien dengan dokter")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AuthorizationService authorization;

    public AppointmentController(AppointmentService appointmentService, AuthorizationService authorization) {
        this.appointmentService = appointmentService;
        this.authorization = authorization;
    }

    /**
     * Mengambil seluruh daftar appointment yang tersimpan.
     *
     * @return daftar semua appointment
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ambil semua appointment (admin)",
               description = "Mengembalikan seluruh daftar janji temu. Khusus admin.")
    @ApiResponse(responseCode = "200", description = "Daftar appointment berhasil diambil")
    public List<AppointmentResponse> getAll() {
        return appointmentService.getAll();
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ambil semua appointment (paginated, admin)",
               description = "Mengembalikan daftar appointment dengan pagination. Khusus admin.")
    @ApiResponse(responseCode = "200", description = "Daftar appointment berhasil diambil")
    public Page<AppointmentResponse> getAllPaginated(Pageable pageable) {
        return appointmentService.getAll(pageable);
    }

    /**
     * Mengambil detail satu appointment berdasarkan ID.
     *
     * @param id ID appointment yang dicari
     * @return data appointment yang ditemukan
     */
    @GetMapping("/{id}")
    @Operation(summary = "Ambil appointment berdasarkan ID",
               description = "Mengembalikan detail satu janji temu sesuai ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointment ditemukan"),
        @ApiResponse(responseCode = "404", description = "Appointment tidak ditemukan")
    })
    public AppointmentResponse getById(@PathVariable Long id) {
        authorization.requireAppointmentAccess(id);
        return appointmentService.getById(id);
    }

    /**
     * Mengambil seluruh appointment milik seorang pasien.
     *
     * @param pasienId ID pasien yang dicari
     * @return daftar appointment pasien tersebut
     */
    @GetMapping("/pasien/{pasienId}")
    @Operation(summary = "Ambil appointment berdasarkan pasien",
               description = "Mengembalikan seluruh janji temu milik pasien tertentu.")
    @ApiResponse(responseCode = "200", description = "Daftar appointment berhasil diambil")
    public List<AppointmentResponse> getByPasien(@PathVariable Long pasienId) {
        authorization.requirePasienAccess(pasienId);
        return appointmentService.getByPasien(pasienId);
    }

    @GetMapping("/pasien/{pasienId}/paginated")
    @Operation(summary = "Ambil appointment berdasarkan pasien (paginated)",
               description = "Mengembalikan janji temu milik pasien tertentu dengan pagination.")
    @ApiResponse(responseCode = "200", description = "Daftar appointment berhasil diambil")
    public Page<AppointmentResponse> getByPasienPaginated(@PathVariable Long pasienId, Pageable pageable) {
        authorization.requirePasienAccess(pasienId);
        return appointmentService.getByPasien(pasienId, pageable);
    }

    /**
     * Mengambil seluruh appointment milik seorang dokter (untuk panel dokter).
     *
     * @param dokterId ID dokter yang dicari
     * @return daftar appointment dokter tersebut
     */
    @GetMapping("/dokter/{dokterId}")
    @Operation(summary = "Ambil appointment berdasarkan dokter",
               description = "Mengembalikan seluruh janji temu milik dokter tertentu.")
    @ApiResponse(responseCode = "200", description = "Daftar appointment berhasil diambil")
    public List<AppointmentResponse> getByDokter(@PathVariable Long dokterId) {
        authorization.requireDokterSelfOrAdmin(dokterId);
        return appointmentService.getByDokter(dokterId);
    }

    /**
     * Membuat janji temu baru dengan validasi jadwal, hari, duplikasi, dan kuota.
     *
     * @param request data appointment baru (pasienId, jadwalId, tanggal, keluhan)
     * @return data appointment yang berhasil dibuat (HTTP 201)
     */
    @PostMapping
    @Operation(summary = "Buat appointment baru",
               description = "Membuat janji temu baru. Dokter diturunkan dari jadwal, "
                       + "status awal MENUNGGU. Divalidasi terhadap keaktifan jadwal, "
                       + "kecocokan hari, duplikasi booking, dan kuota.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Appointment berhasil dibuat"),
        @ApiResponse(responseCode = "400", description = "Jadwal tidak aktif, hari tidak cocok, atau kuota penuh"),
        @ApiResponse(responseCode = "404", description = "Pasien atau jadwal tidak ditemukan"),
        @ApiResponse(responseCode = "409", description = "Pasien sudah memesan jadwal dan tanggal yang sama")
    })
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentRequest request) {
        authorization.requireAppointmentCreate(request.pasienId());
        AppointmentResponse response = appointmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Memperbarui status sebuah appointment (mis. MENUNGGU → DIPERIKSA → SELESAI, atau BATAL).
     *
     * @param id     ID appointment yang akan diperbarui
     * @param status status baru appointment
     * @return data appointment setelah diperbarui
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Perbarui status appointment",
               description = "Mengubah status janji temu menjadi salah satu dari "
                       + "MENUNGGU, DIPERIKSA, SELESAI, atau BATAL.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status appointment berhasil diperbarui"),
        @ApiResponse(responseCode = "400", description = "Status tidak valid"),
        @ApiResponse(responseCode = "404", description = "Appointment tidak ditemukan")
    })
    public AppointmentResponse updateStatus(@PathVariable Long id,
                                            @RequestParam StatusAppointment status) {
        authorization.requireAppointmentStatusChange(id, status);
        return appointmentService.updateStatus(id, status);
    }
}
