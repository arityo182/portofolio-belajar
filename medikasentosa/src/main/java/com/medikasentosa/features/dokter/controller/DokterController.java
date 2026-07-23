package com.medikasentosa.features.dokter.controller;

import com.medikasentosa.features.dokter.dto.DokterRequest;
import com.medikasentosa.features.dokter.dto.DokterResponse;
import com.medikasentosa.features.dokter.service.DokterService;
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
 * Controller untuk manajemen data dokter rumah sakit.
 * Menyediakan operasi CRUD serta pencarian dokter berdasarkan poli.
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/dokter")
@Tag(name = "Dokter", description = "API manajemen data dokter")
public class DokterController {

    private final DokterService dokterService;

    public DokterController(DokterService dokterService) {
        this.dokterService = dokterService;
    }

    /**
     * Mengambil seluruh daftar dokter yang tersimpan.
     *
     * @return daftar semua dokter
     */
    @GetMapping
    @Operation(summary = "Ambil semua dokter",
               description = "Mengembalikan seluruh daftar dokter yang tersimpan.")
    @ApiResponse(responseCode = "200", description = "Daftar dokter berhasil diambil")
    public List<DokterResponse> getAll() {
        return dokterService.getAll();
    }

    /**
     * Mengambil detail satu dokter berdasarkan ID.
     *
     * @param id ID dokter yang dicari
     * @return data dokter yang ditemukan
     */
    @GetMapping("/{id}")
    @Operation(summary = "Ambil dokter berdasarkan ID",
               description = "Mengembalikan detail satu dokter sesuai ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dokter ditemukan"),
        @ApiResponse(responseCode = "404", description = "Dokter tidak ditemukan")
    })
    public DokterResponse getById(@PathVariable Long id) {
        return dokterService.getById(id);
    }

    /**
     * Mengambil profil dokter berdasarkan ID user terkait.
     *
     * @param userId ID user yang dicari
     * @return data dokter milik user tersebut
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Ambil dokter berdasarkan user",
               description = "Mengembalikan profil dokter milik user tertentu.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dokter ditemukan"),
        @ApiResponse(responseCode = "404", description = "Dokter tidak ditemukan")
    })
    public DokterResponse getByUserId(@PathVariable Long userId) {
        return dokterService.getByUserId(userId);
    }

    /**
     * Mengambil seluruh dokter yang bertugas pada sebuah poli.
     *
     * @param poliId ID poli yang dicari
     * @return daftar dokter pada poli tersebut
     */
    @GetMapping("/poli/{poliId}")
    @Operation(summary = "Ambil dokter berdasarkan poli",
               description = "Mengembalikan seluruh dokter yang bertugas pada poli tertentu.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Daftar dokter berhasil diambil"),
        @ApiResponse(responseCode = "404", description = "Poli tidak ditemukan")
    })
    public List<DokterResponse> getByPoli(@PathVariable Long poliId) {
        return dokterService.getByPoli(poliId);
    }

    /**
     * Membuat data dokter baru.
     *
     * @param request data dokter baru (userId, poliId, noStr, spesialisasi, noHp)
     * @return data dokter yang berhasil dibuat (HTTP 201)
     */
    @PostMapping
    @Operation(summary = "Buat dokter baru",
               description = "Membuat profil dokter baru dan menyetel peran user menjadi DOKTER.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Dokter berhasil dibuat"),
        @ApiResponse(responseCode = "400", description = "Data dokter tidak valid"),
        @ApiResponse(responseCode = "404", description = "User atau poli tidak ditemukan"),
        @ApiResponse(responseCode = "409", description = "User sudah terdaftar sebagai dokter")
    })
    public ResponseEntity<DokterResponse> create(@Valid @RequestBody DokterRequest request) {
        DokterResponse response = dokterService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Memperbarui data dokter yang sudah ada.
     *
     * @param id      ID dokter yang akan diperbarui
     * @param request data dokter baru (userId, poliId, noStr, spesialisasi, noHp)
     * @return data dokter setelah diperbarui
     */
    @PutMapping("/{id}")
    @Operation(summary = "Perbarui dokter",
               description = "Memperbarui data profil dokter berdasarkan ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dokter berhasil diperbarui"),
        @ApiResponse(responseCode = "400", description = "Data dokter tidak valid"),
        @ApiResponse(responseCode = "404", description = "Dokter atau poli tidak ditemukan")
    })
    public DokterResponse update(@PathVariable Long id, @Valid @RequestBody DokterRequest request) {
        return dokterService.update(id, request);
    }

    /**
     * Menghapus sebuah dokter berdasarkan ID.
     *
     * @param id ID dokter yang akan dihapus
     * @return respons kosong (HTTP 204 No Content)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Hapus dokter",
               description = "Menghapus data dokter berdasarkan ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Dokter berhasil dihapus"),
        @ApiResponse(responseCode = "404", description = "Dokter tidak ditemukan")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dokterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
