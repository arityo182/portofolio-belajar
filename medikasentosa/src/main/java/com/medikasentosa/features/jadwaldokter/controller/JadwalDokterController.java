package com.medikasentosa.features.jadwaldokter.controller;

import com.medikasentosa.features.jadwaldokter.dto.JadwalDokterRequest;
import com.medikasentosa.features.jadwaldokter.dto.JadwalDokterResponse;
import com.medikasentosa.features.jadwaldokter.service.JadwalDokterService;
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
 * Controller untuk manajemen jadwal praktik dokter.
 * Menyediakan operasi CRUD serta penonaktifan (soft delete) jadwal.
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/jadwal")
@Tag(name = "Jadwal Dokter", description = "API manajemen jadwal praktik dokter")
public class JadwalDokterController {

    private final JadwalDokterService jadwalDokterService;

    public JadwalDokterController(JadwalDokterService jadwalDokterService) {
        this.jadwalDokterService = jadwalDokterService;
    }

    /**
     * Mengambil seluruh daftar jadwal praktik yang tersimpan.
     *
     * @return daftar semua jadwal
     */
    @GetMapping
    @Operation(summary = "Ambil semua jadwal",
               description = "Mengembalikan seluruh daftar jadwal praktik yang tersimpan.")
    @ApiResponse(responseCode = "200", description = "Daftar jadwal berhasil diambil")
    public List<JadwalDokterResponse> getAll() {
        return jadwalDokterService.getAll();
    }

    /**
     * Mengambil seluruh jadwal aktif milik seorang dokter.
     *
     * @param dokterId ID dokter yang dicari
     * @return daftar jadwal aktif dokter tersebut
     */
    @GetMapping("/dokter/{dokterId}")
    @Operation(summary = "Ambil jadwal berdasarkan dokter",
               description = "Mengembalikan seluruh jadwal aktif milik dokter tertentu.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Daftar jadwal berhasil diambil"),
        @ApiResponse(responseCode = "404", description = "Dokter tidak ditemukan")
    })
    public List<JadwalDokterResponse> getByDokter(@PathVariable Long dokterId) {
        return jadwalDokterService.getByDokter(dokterId);
    }

    /**
     * Membuat jadwal praktik baru.
     *
     * @param request data jadwal baru (dokterId, hari, jamMulai, jamSelesai, kuota)
     * @return data jadwal yang berhasil dibuat (HTTP 201)
     */
    @PostMapping
    @Operation(summary = "Buat jadwal baru",
               description = "Membuat jadwal praktik baru untuk seorang dokter.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Jadwal berhasil dibuat"),
        @ApiResponse(responseCode = "400", description = "Data jadwal tidak valid"),
        @ApiResponse(responseCode = "404", description = "Dokter tidak ditemukan")
    })
    public ResponseEntity<JadwalDokterResponse> create(@Valid @RequestBody JadwalDokterRequest request) {
        JadwalDokterResponse response = jadwalDokterService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Memperbarui data jadwal praktik yang sudah ada.
     *
     * @param id      ID jadwal yang akan diperbarui
     * @param request data jadwal baru (hari, jamMulai, jamSelesai, kuota)
     * @return data jadwal setelah diperbarui
     */
    @PutMapping("/{id}")
    @Operation(summary = "Perbarui jadwal",
               description = "Memperbarui hari, jam, dan kuota jadwal berdasarkan ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Jadwal berhasil diperbarui"),
        @ApiResponse(responseCode = "400", description = "Data jadwal tidak valid"),
        @ApiResponse(responseCode = "404", description = "Jadwal tidak ditemukan")
    })
    public JadwalDokterResponse update(@PathVariable Long id, @Valid @RequestBody JadwalDokterRequest request) {
        return jadwalDokterService.update(id, request);
    }

    /**
     * Menonaktifkan (soft delete) sebuah jadwal berdasarkan ID.
     *
     * @param id ID jadwal yang akan dinonaktifkan
     * @return respons kosong (HTTP 204 No Content)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Nonaktifkan jadwal",
               description = "Menonaktifkan jadwal (soft delete) sehingga tidak lagi aktif.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Jadwal berhasil dinonaktifkan"),
        @ApiResponse(responseCode = "404", description = "Jadwal tidak ditemukan")
    })
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        jadwalDokterService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
