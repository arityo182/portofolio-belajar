package com.medikasentosa.features.resep.controller;

import com.medikasentosa.features.resep.dto.ResepRequest;
import com.medikasentosa.features.resep.dto.ResepResponse;
import com.medikasentosa.features.resep.service.ResepService;
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
 * Controller untuk manajemen resep obat.
 * Menyediakan operasi penambahan resep pada rekam medis, pembacaan resep
 * per rekam medis, dan penghapusan resep.
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/resep")
@Tag(name = "Resep", description = "API manajemen resep obat pada rekam medis")
public class ResepController {

    private final ResepService resepService;

    public ResepController(ResepService resepService) {
        this.resepService = resepService;
    }

    /**
     * Menambahkan resep obat baru pada sebuah rekam medis.
     *
     * @param request data resep baru (rekamMedisId, namaObat, dosis, jumlah, aturanPakai)
     * @return data resep yang berhasil dibuat (HTTP 201)
     */
    @PostMapping
    @Operation(summary = "Tambah resep",
               description = "Menambahkan resep obat pada sebuah rekam medis.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Resep berhasil dibuat"),
        @ApiResponse(responseCode = "400", description = "Data resep tidak valid"),
        @ApiResponse(responseCode = "404", description = "Rekam medis tidak ditemukan")
    })
    public ResponseEntity<ResepResponse> create(@Valid @RequestBody ResepRequest request) {
        ResepResponse response = resepService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Mengambil seluruh resep milik sebuah rekam medis.
     *
     * @param rekamMedisId ID rekam medis yang dicari
     * @return daftar resep rekam medis tersebut
     */
    @GetMapping("/rekam-medis/{rekamMedisId}")
    @Operation(summary = "Ambil resep berdasarkan rekam medis",
               description = "Mengembalikan seluruh resep milik rekam medis tertentu.")
    @ApiResponse(responseCode = "200", description = "Daftar resep berhasil diambil")
    public List<ResepResponse> getByRekamMedis(@PathVariable Long rekamMedisId) {
        return resepService.getByRekamMedis(rekamMedisId);
    }

    /**
     * Menghapus sebuah resep berdasarkan ID.
     *
     * @param id ID resep yang akan dihapus
     * @return respons kosong (HTTP 204 No Content)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Hapus resep",
               description = "Menghapus sebuah resep berdasarkan ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Resep berhasil dihapus"),
        @ApiResponse(responseCode = "404", description = "Resep tidak ditemukan")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resepService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
