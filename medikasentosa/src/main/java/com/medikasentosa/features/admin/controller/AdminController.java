package com.medikasentosa.features.admin.controller;

import com.medikasentosa.features.admin.dto.DashboardResponse;
import com.medikasentosa.features.admin.dto.DokterCreateRequest;
import com.medikasentosa.features.admin.dto.PasienCreateRequest;
import com.medikasentosa.features.admin.dto.UserAdminResponse;
import com.medikasentosa.features.admin.service.AdminService;
import com.medikasentosa.features.antrian.dto.AntrianResponse;
import com.medikasentosa.features.antrian.service.AntrianService;
import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.dokter.dto.DokterRequest;
import com.medikasentosa.features.dokter.dto.DokterResponse;
import com.medikasentosa.features.dokter.service.DokterService;
import com.medikasentosa.features.jadwaldokter.dto.JadwalDokterRequest;
import com.medikasentosa.features.jadwaldokter.dto.JadwalDokterResponse;
import com.medikasentosa.features.jadwaldokter.service.JadwalDokterService;
import com.medikasentosa.features.pasien.dto.PasienRequest;
import com.medikasentosa.features.pasien.dto.PasienResponse;
import com.medikasentosa.features.pasien.service.PasienService;
import com.medikasentosa.features.poli.dto.PoliRequest;
import com.medikasentosa.features.poli.dto.PoliResponse;
import com.medikasentosa.features.poli.service.PoliService;
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
 * Controller untuk seluruh operasi administratif (FR-35, FR-36, FR-04).
 *
 * Seluruh endpoint di sini dibatasi untuk role {@code ADMIN} melalui
 * {@code @PreAuthorize("hasRole('ADMIN')")} di level kelas, ditambah
 * rule URL {@code /api/admin/**} di SecurityConfig.
 *
 * Mencakup: dashboard, manajemen poli, manajemen dokter (termasuk pembuatan
 * user dokter baru), manajemen jadwal, dan manajemen pengguna.
 *
 * @author Ari
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "API operasi administratif (khusus ADMIN)")
public class AdminController {

    private final AdminService adminService;
    private final PoliService poliService;
    private final DokterService dokterService;
    private final JadwalDokterService jadwalDokterService;
    private final PasienService pasienService;
    private final AntrianService antrianService;

    public AdminController(AdminService adminService,
                           PoliService poliService,
                           DokterService dokterService,
                           JadwalDokterService jadwalDokterService,
                           PasienService pasienService,
                           AntrianService antrianService) {
        this.adminService = adminService;
        this.poliService = poliService;
        this.dokterService = dokterService;
        this.jadwalDokterService = jadwalDokterService;
        this.pasienService = pasienService;
        this.antrianService = antrianService;
    }

    /* ================= DASHBOARD ================= */

    @GetMapping("/dashboard")
    @Operation(summary = "Statistik dashboard admin",
               description = "Mengembalikan jumlah pasien, dokter, poli, dan appointment.")
    public DashboardResponse getDashboard() {
        return adminService.getDashboard();
    }

    /* ================= MANAJEMEN POLI (FR-35) ================= */

    @GetMapping("/poli")
    @Operation(summary = "Daftar semua poli (admin)",
               description = "Mengembalikan seluruh poli termasuk yang nonaktif.")
    public List<PoliResponse> getAllPoli() {
        return poliService.getAll();
    }

    @PostMapping("/poli")
    @Operation(summary = "Buat poli baru (FR-35)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Poli berhasil dibuat"),
        @ApiResponse(responseCode = "409", description = "Nama poli sudah ada")
    })
    public ResponseEntity<PoliResponse> createPoli(@Valid @RequestBody PoliRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(poliService.create(request));
    }

    @PutMapping("/poli/{id}")
    @Operation(summary = "Perbarui poli (FR-35)")
    public PoliResponse updatePoli(@PathVariable Long id, @Valid @RequestBody PoliRequest request) {
        return poliService.update(id, request);
    }

    @DeleteMapping("/poli/{id}")
    @Operation(summary = "Nonaktifkan poli (FR-35)",
               description = "Soft delete — menyetel isActive=false.")
    @ApiResponse(responseCode = "204", description = "Poli dinonaktifkan")
    public ResponseEntity<Void> deactivatePoli(@PathVariable Long id) {
        poliService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    /* ================= MANAJEMEN DOKTER (FR-36) ================= */

    @GetMapping("/dokter")
    @Operation(summary = "Daftar semua dokter (admin)",
               description = "Mengembalikan seluruh dokter termasuk yang nonaktif.")
    public List<DokterResponse> getAllDokter() {
        return adminService.getAllDokterForAdmin();
    }

    @PostMapping("/dokter")
    @Operation(summary = "Buat user dokter baru + profil (FR-36)",
               description = "Membuat akun User(role=DOKTER) dan profil Dokter dalam satu transaksi.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Dokter berhasil dibuat"),
        @ApiResponse(responseCode = "409", description = "Email sudah terdaftar"),
        @ApiResponse(responseCode = "404", description = "Poli tidak ditemukan")
    })
    public ResponseEntity<DokterResponse> createDokter(@Valid @RequestBody DokterCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createDokterWithUser(request));
    }

    @PutMapping("/dokter/{id}")
    @Operation(summary = "Perbarui profil dokter (FR-36)")
    public DokterResponse updateDokter(@PathVariable Long id, @Valid @RequestBody DokterRequest request) {
        return dokterService.update(id, request);
    }

    @DeleteMapping("/dokter/{id}")
    @Operation(summary = "Nonaktifkan dokter (FR-36)",
               description = "Soft delete — menyetel isActive=false.")
    @ApiResponse(responseCode = "204", description = "Dokter dinonaktifkan")
    public ResponseEntity<Void> deactivateDokter(@PathVariable Long id) {
        dokterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /* ================= MANAJEMEN JADWAL (FR-36) ================= */

    @GetMapping("/jadwal")
    @Operation(summary = "Daftar semua jadwal (admin)")
    public List<JadwalDokterResponse> getAllJadwal() {
        return jadwalDokterService.getAll();
    }

    @PostMapping("/jadwal")
    @Operation(summary = "Buat jadwal baru (FR-36)")
    @ApiResponse(responseCode = "201", description = "Jadwal berhasil dibuat")
    public ResponseEntity<JadwalDokterResponse> createJadwal(@Valid @RequestBody JadwalDokterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jadwalDokterService.create(request));
    }

    @PutMapping("/jadwal/{id}")
    @Operation(summary = "Perbarui jadwal (FR-36)")
    public JadwalDokterResponse updateJadwal(@PathVariable Long id, @Valid @RequestBody JadwalDokterRequest request) {
        return jadwalDokterService.update(id, request);
    }

    @DeleteMapping("/jadwal/{id}")
    @Operation(summary = "Nonaktifkan jadwal (FR-36)")
    @ApiResponse(responseCode = "204", description = "Jadwal dinonaktifkan")
    public ResponseEntity<Void> deactivateJadwal(@PathVariable Long id) {
        jadwalDokterService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    /* ================= MANAJEMEN PASIEN ================= */

    @GetMapping("/pasien")
    @Operation(summary = "Daftar semua pasien (admin)")
    public List<PasienResponse> getAllPasien() {
        return pasienService.getAll();
    }

    @PostMapping("/pasien")
    @Operation(summary = "Buat user pasien baru + profil",
               description = "Membuat akun User(role=PASIEN) dan profil Pasien dalam satu transaksi.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pasien berhasil dibuat"),
        @ApiResponse(responseCode = "409", description = "Email sudah terdaftar")
    })
    public ResponseEntity<PasienResponse> createPasien(@Valid @RequestBody PasienCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createPasienWithUser(request));
    }

    @PutMapping("/pasien/{id}")
    @Operation(summary = "Perbarui profil pasien (admin)")
    public PasienResponse updatePasien(@PathVariable Long id, @Valid @RequestBody PasienRequest request) {
        return adminService.updatePasien(id, request);
    }

    @DeleteMapping("/pasien/{id}")
    @Operation(summary = "Nonaktifkan akun pasien (admin)",
               description = "Menonaktifkan akun User terkait pasien. Profil pasien tetap ada.")
    @ApiResponse(responseCode = "204", description = "Akun pasien dinonaktifkan")
    public ResponseEntity<Void> deactivatePasien(@PathVariable Long id) {
        // Ambil pasien, lalu nonaktifkan user-nya
        com.medikasentosa.features.pasien.entity.Pasien pasien = pasienService.getEntityById(id);
        adminService.setUserStatus(pasien.getUser().getId(), false);
        return ResponseEntity.noContent().build();
    }

    /* ================= MANAJEMEN ANTRIAN ================= */

    @GetMapping("/antrian")
    @Operation(summary = "Daftar semua antrian (admin)",
               description = "Mengembalikan seluruh daftar antrian untuk papan antrian admin.")
    public List<AntrianResponse> getAllAntrian() {
        return antrianService.getAll();
    }

    /* ================= MANAJEMEN PENGGUNA (FR-04) ================= */

    @GetMapping("/users")
    @Operation(summary = "Daftar semua pengguna (FR-04)",
               description = "Mengembalikan daftar pengguna, opsional difilter per role via query param.")
    public List<UserAdminResponse> getAllUsers(@RequestParam(required = false) Role role) {
        return adminService.getAllUsers(role);
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Aktif/nonaktifkan akun (FR-04)",
               description = "Mengubah status aktif akun pengguna. Akun nonaktif tidak dapat login.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status diperbarui"),
        @ApiResponse(responseCode = "404", description = "User tidak ditemukan")
    })
    public UserAdminResponse setUserStatus(@PathVariable Long id, @RequestParam boolean active) {
        return adminService.setUserStatus(id, active);
    }

    @PatchMapping("/users/{id}/password")
    @Operation(summary = "Reset password pengguna",
               description = "Admin mengganti password dokter/pasien. Password lama tidak perlu diketahui. "
                       + "Password baru minimal 6 karakter.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password berhasil diubah"),
        @ApiResponse(responseCode = "400", description = "Password baru tidak valid (min 6 karakter)"),
        @ApiResponse(responseCode = "404", description = "User tidak ditemukan")
    })
    public UserAdminResponse resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        String password = body.get("password");
        return adminService.resetPassword(id, password);
    }
}
