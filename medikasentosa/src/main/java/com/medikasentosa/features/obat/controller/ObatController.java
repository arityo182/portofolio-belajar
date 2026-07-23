package com.medikasentosa.features.obat.controller;

import com.medikasentosa.features.obat.dto.ObatRequest;
import com.medikasentosa.features.obat.dto.ObatResponse;
import com.medikasentosa.features.obat.service.ObatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller untuk manajemen data master obat (FR-37).
 * Endpoint GET tersedia untuk semua pengguna terautentikasi;
 * POST/PUT/DELETE dibatasi untuk role ADMIN.
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/obat")
@Tag(name = "Obat Management", description = "API manajemen data master obat")
public class ObatController {

    private final ObatService obatService;

    public ObatController(ObatService obatService) {
        this.obatService = obatService;
    }

    /**
     * Mengambil seluruh daftar obat.
     *
     * @return daftar semua obat
     */
    @GetMapping
    @Operation(summary = "Ambil semua obat",
               description = "Mengembalikan seluruh daftar obat yang tersimpan.")
    @ApiResponse(responseCode = "200", description = "Daftar obat berhasil diambil")
    public List<ObatResponse> getAll() {
        return obatService.getAll();
    }

    /**
     * Mengambil detail satu obat berdasarkan ID.
     *
     * @param id ID obat yang dicari
     * @return data obat yang ditemukan
     */
    @GetMapping("/{id}")
    @Operation(summary = "Ambil obat berdasarkan ID",
               description = "Mengembalikan detail satu obat sesuai ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Obat ditemukan"),
        @ApiResponse(responseCode = "404", description = "Obat tidak ditemukan")
    })
    public ObatResponse getById(@PathVariable Long id) {
        return obatService.getById(id);
    }

    /**
     * Membuat data obat baru (khusus admin).
     *
     * @param request data obat baru (nama, kategori, satuan, stok, harga, deskripsi)
     * @return data obat yang berhasil dibuat (HTTP 201)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buat obat baru (admin)",
               description = "Membuat data master obat baru. Nama obat harus unik.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Obat berhasil dibuat"),
        @ApiResponse(responseCode = "400", description = "Data obat tidak valid"),
        @ApiResponse(responseCode = "409", description = "Nama obat sudah ada")
    })
    public ResponseEntity<ObatResponse> create(@Valid @RequestBody ObatRequest request) {
        ObatResponse response = obatService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Memperbarui data obat yang sudah ada (khusus admin).
     *
     * @param id      ID obat yang akan diperbarui
     * @param request data obat baru
     * @return data obat setelah diperbarui
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Perbarui obat (admin)",
               description = "Memperbarui data master obat berdasarkan ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Obat berhasil diperbarui"),
        @ApiResponse(responseCode = "400", description = "Data obat tidak valid"),
        @ApiResponse(responseCode = "404", description = "Obat tidak ditemukan"),
        @ApiResponse(responseCode = "409", description = "Nama obat sudah dipakai")
    })
    public ObatResponse update(@PathVariable Long id, @Valid @RequestBody ObatRequest request) {
        return obatService.update(id, request);
    }

    /**
     * Menonaktifkan sebuah obat (soft delete, khusus admin).
     *
     * @param id ID obat yang akan dinonaktifkan
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Nonaktifkan obat (admin)",
               description = "Soft delete — menyetel isActive=false. Obat yang dinonaktifkan "
                       + "tidak muncul di pilihan resep baru.")
    @ApiResponse(responseCode = "204", description = "Obat berhasil dinonaktifkan")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        obatService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}