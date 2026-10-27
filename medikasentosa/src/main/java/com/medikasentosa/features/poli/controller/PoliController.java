package com.medikasentosa.features.poli.controller;

import com.medikasentosa.features.poli.dto.PoliRequest;
import com.medikasentosa.features.poli.dto.PoliResponse;
import com.medikasentosa.features.poli.service.PoliService;
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
 * Controller untuk manajemen data poli (poliklinik) rumah sakit.
 * Menyediakan operasi CRUD serta penonaktifan (soft delete) poli.
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/poli")
@Tag(name = "Poli", description = "API manajemen data poli (poliklinik)")
public class PoliController {

    private final PoliService poliService;

    public PoliController(PoliService poliService) {
        this.poliService = poliService;
    }

    /**
     * Mengambil daftar poli aktif. Poli nonaktif hanya tersedia melalui
     * endpoint admin {@code /api/admin/poli}.
     *
     * @return daftar poli aktif
     */
    @GetMapping
    @Operation(summary = "Ambil semua poli aktif",
               description = "Mengembalikan daftar poli yang masih aktif untuk konsumen publik.")
    @ApiResponse(responseCode = "200", description = "Daftar poli berhasil diambil")
    public List<PoliResponse> getAll() {
        return poliService.getAllActive();
    }

    @GetMapping("/paginated")
    @Operation(summary = "Ambil semua poli aktif (paginated)",
               description = "Mengembalikan daftar poli aktif dengan pagination.")
    @ApiResponse(responseCode = "200", description = "Daftar poli berhasil diambil")
    public Page<PoliResponse> getAllPaginated(Pageable pageable) {
        return poliService.getAllActive(pageable);
    }

    /**
     * Mengambil detail satu poli berdasarkan ID.
     *
     * @param id ID poli yang dicari
     * @return data poli yang ditemukan
     */
    @GetMapping("/{id}")
    @Operation(summary = "Ambil poli berdasarkan ID",
               description = "Mengembalikan detail satu poli sesuai ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Poli ditemukan"),
        @ApiResponse(responseCode = "404", description = "Poli tidak ditemukan")
    })
    public PoliResponse getById(@PathVariable Long id) {
        return poliService.getById(id);
    }

    /**
     * Membuat data poli baru.
     *
     * @param request data poli baru (nama, deskripsi)
     * @return data poli yang berhasil dibuat (HTTP 201)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buat poli baru (admin)",
               description = "Membuat data poli baru dengan nama yang unik.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Poli berhasil dibuat"),
        @ApiResponse(responseCode = "400", description = "Data poli tidak valid"),
        @ApiResponse(responseCode = "409", description = "Poli dengan nama tersebut sudah ada")
    })
    public ResponseEntity<PoliResponse> create(@Valid @RequestBody PoliRequest request) {
        PoliResponse response = poliService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Memperbarui data poli yang sudah ada.
     *
     * @param id      ID poli yang akan diperbarui
     * @param request data poli baru (nama, deskripsi)
     * @return data poli setelah diperbarui
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Perbarui poli (admin)",
               description = "Memperbarui nama dan deskripsi poli berdasarkan ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Poli berhasil diperbarui"),
        @ApiResponse(responseCode = "400", description = "Data poli tidak valid"),
        @ApiResponse(responseCode = "404", description = "Poli tidak ditemukan")
    })
    public PoliResponse update(@PathVariable Long id, @Valid @RequestBody PoliRequest request) {
        return poliService.update(id, request);
    }

    /**
     * Menonaktifkan (soft delete) sebuah poli berdasarkan ID.
     *
     * @param id ID poli yang akan dinonaktifkan
     * @return respons kosong (HTTP 204 No Content)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Nonaktifkan poli (admin)",
               description = "Menonaktifkan poli (soft delete) sehingga tidak lagi aktif.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Poli berhasil dinonaktifkan"),
        @ApiResponse(responseCode = "404", description = "Poli tidak ditemukan")
    })
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        poliService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
