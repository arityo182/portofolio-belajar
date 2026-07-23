package com.medikasentosa.features.appointment.controller;

import com.medikasentosa.features.appointment.dto.AppointmentRequest;
import com.medikasentosa.features.appointment.dto.AppointmentResponse;
import com.medikasentosa.features.appointment.entity.StatusAppointment;
import com.medikasentosa.features.appointment.service.AppointmentService;
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

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Mengambil seluruh daftar appointment yang tersimpan.
     *
     * @return daftar semua appointment
     */
    @GetMapping
    @Operation(summary = "Ambil semua appointment",
               description = "Mengembalikan seluruh daftar janji temu yang tersimpan.")
    @ApiResponse(responseCode = "200", description = "Daftar appointment berhasil diambil")
    public List<AppointmentResponse> getAll() {
        return appointmentService.getAll();
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
        return appointmentService.getByPasien(pasienId);
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
        return appointmentService.updateStatus(id, status);
    }
}
