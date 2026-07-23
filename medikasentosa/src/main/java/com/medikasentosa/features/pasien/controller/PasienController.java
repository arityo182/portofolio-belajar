package com.medikasentosa.features.pasien.controller;

import com.medikasentosa.features.pasien.dto.PasienRequest;
import com.medikasentosa.features.pasien.dto.PasienResponse;
import com.medikasentosa.features.pasien.service.PasienService;
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
 * Controller untuk manajemen data pasien rumah sakit.
 * Menyediakan operasi pembuatan, pembacaan, dan pembaruan profil pasien.
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/pasien")
@Tag(name = "Pasien", description = "API manajemen data pasien")
public class PasienController {

    private final PasienService pasienService;

    public PasienController(PasienService pasienService) {
        this.pasienService = pasienService;
    }

    /**
     * Mengambil seluruh daftar pasien yang tersimpan.
     *
     * @return daftar semua pasien
     */
    @GetMapping
    @Operation(summary = "Ambil semua pasien",
               description = "Mengembalikan seluruh daftar pasien yang tersimpan.")
    @ApiResponse(responseCode = "200", description = "Daftar pasien berhasil diambil")
    public List<PasienResponse> getAll() {
        return pasienService.getAll();
    }

    /**
     * Mengambil detail satu pasien berdasarkan ID.
     *
     * @param id ID pasien yang dicari
     * @return data pasien yang ditemukan
     */
    @GetMapping("/{id}")
    @Operation(summary = "Ambil pasien berdasarkan ID",
               description = "Mengembalikan detail satu pasien sesuai ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pasien ditemukan"),
        @ApiResponse(responseCode = "404", description = "Pasien tidak ditemukan")
    })
    public PasienResponse getById(@PathVariable Long id) {
        return pasienService.getById(id);
    }

    /**
     * Mengambil profil pasien berdasarkan ID user terkait.
     *
     * @param userId ID user yang dicari
     * @return data pasien milik user tersebut
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Ambil pasien berdasarkan user",
               description = "Mengembalikan profil pasien milik user tertentu.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pasien ditemukan"),
        @ApiResponse(responseCode = "404", description = "Pasien tidak ditemukan")
    })
    public PasienResponse getByUserId(@PathVariable Long userId) {
        return pasienService.getByUserId(userId);
    }

    /**
     * Membuat data pasien baru dengan nomor rekam medis unik yang digenerate otomatis.
     *
     * @param request data pasien baru
     * @return data pasien yang berhasil dibuat (HTTP 201)
     */
    @PostMapping
    @Operation(summary = "Buat pasien baru",
               description = "Membuat profil pasien baru dan menghasilkan nomor rekam medis unik.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pasien berhasil dibuat"),
        @ApiResponse(responseCode = "400", description = "Data pasien tidak valid"),
        @ApiResponse(responseCode = "404", description = "User tidak ditemukan"),
        @ApiResponse(responseCode = "409", description = "User sudah terdaftar sebagai pasien")
    })
    public ResponseEntity<PasienResponse> create(@Valid @RequestBody PasienRequest request) {
        PasienResponse response = pasienService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Memperbarui data pasien yang sudah ada.
     *
     * @param id      ID pasien yang akan diperbarui
     * @param request data pasien baru
     * @return data pasien setelah diperbarui
     */
    @PutMapping("/{id}")
    @Operation(summary = "Perbarui pasien",
               description = "Memperbarui data profil pasien berdasarkan ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pasien berhasil diperbarui"),
        @ApiResponse(responseCode = "400", description = "Data pasien tidak valid"),
        @ApiResponse(responseCode = "404", description = "Pasien tidak ditemukan")
    })
    public PasienResponse update(@PathVariable Long id, @Valid @RequestBody PasienRequest request) {
        return pasienService.update(id, request);
    }
}
