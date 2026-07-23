package com.medikasentosa.features.admin.service;

import com.medikasentosa.features.admin.dto.DashboardResponse;
import com.medikasentosa.features.admin.dto.DokterCreateRequest;
import com.medikasentosa.features.admin.dto.PasienCreateRequest;
import com.medikasentosa.features.admin.dto.UserAdminResponse;
import com.medikasentosa.features.appointment.repository.AppointmentRepository;
import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.auth.repository.UserRepository;
import com.medikasentosa.features.dokter.dto.DokterRequest;
import com.medikasentosa.features.dokter.dto.DokterResponse;
import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.dokter.repository.DokterRepository;
import com.medikasentosa.features.dokter.service.DokterService;
import com.medikasentosa.features.pasien.dto.PasienRequest;
import com.medikasentosa.features.pasien.dto.PasienResponse;
import com.medikasentosa.features.pasien.service.PasienService;
import com.medikasentosa.features.poli.repository.PoliRepository;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service untuk operasi administratif (FR-35, FR-36, FR-04).
 *
 * Mengelola: pembuatan user dokter + profil dokter dalam satu transaksi,
 * daftar semua pengguna, aktivasi/nonaktifkan akun, dan statistik dashboard.
 * Akses dibatasi role ADMIN lewat {@code @PreAuthorize} pada controller.
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final DokterRepository dokterRepository;
    private final PoliRepository poliRepository;
    private final AppointmentRepository appointmentRepository;
    private final DokterService dokterService;
    private final PasienService pasienService;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository,
                        DokterRepository dokterRepository,
                        PoliRepository poliRepository,
                        AppointmentRepository appointmentRepository,
                        DokterService dokterService,
                        PasienService pasienService,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.dokterRepository = dokterRepository;
        this.poliRepository = poliRepository;
        this.appointmentRepository = appointmentRepository;
        this.dokterService = dokterService;
        this.pasienService = pasienService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Membuat user dokter baru beserta profil dokternya dalam satu transaksi (FR-36).
     *
     * Menggabungkan pembuatan akun (User role=DOKTER) dan profil dokter sehingga
     * admin tidak perlu mendaftar user terpisah. Bila email sudah dipakai, lempar
     * 409. Bila user sudah terdaftar sebagai dokter, lempar 409.
     *
     * @param request data user dokter baru (nama, email, password, poliId, noStr, dll)
     * @return data dokter yang berhasil dibuat
     * @throws DuplicateResourceException bila email sudah terdaftar
     * @throws ResourceNotFoundException  bila poli tidak ditemukan
     */
    @Transactional
    public DokterResponse createDokterWithUser(DokterCreateRequest request) {
        // Validasi email unik
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email sudah terdaftar");
        }

        // Buat user baru dengan role DOKTER
        User user = User.builder()
                .nama(request.nama())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.DOKTER)
                .isActive(true)
                .build();
        user = userRepository.save(user);

        // Buat profil dokter via DokterService (sudah handle validasi poli & duplikat)
        DokterRequest dokterReq = new DokterRequest(
                user.getId(), request.poliId(), request.noStr(),
                request.spesialisasi(), request.noHp());
        return dokterService.create(dokterReq);
    }

    /**
     * Mengambil daftar dokter termasuk yang nonaktif (untuk admin).
     *
     * Berbeda dengan {@code DokterService.getAll()} yang hanya mengembalikan
     * dokter aktif, method ini menampilkan semua dokter agar admin bisa
     * melihat yang sudah dinonaktifkan.
     *
     * @return daftar semua dokter (aktif & nonaktif)
     */
    public List<DokterResponse> getAllDokterForAdmin() {
        return dokterRepository.findAll().stream()
                .map(this::toDokterResponse)
                .toList();
    }

    /**
     * Membuat user pasien baru beserta profil pasiennya dalam satu transaksi.
     *
     * Menggabungkan pembuatan akun (User role=PASIEN) dan profil pasien (NIK,
     * alamat, dll) sehingga admin bisa menambah pasien manual tanpa minta pasien
     * daftar sendiri via /register.
     *
     * @param request data user pasien baru (nama, email, password, nik, dll)
     * @return data pasien yang berhasil dibuat
     * @throws DuplicateResourceException bila email sudah terdaftar
     */
    @Transactional
    public PasienResponse createPasienWithUser(PasienCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email sudah terdaftar");
        }

        User user = User.builder()
                .nama(request.nama())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.PASIEN)
                .isActive(true)
                .build();
        user = userRepository.save(user);

        PasienRequest pasienReq = new PasienRequest(
                user.getId(), request.nik(), request.tanggalLahir(),
                request.jenisKelamin(), request.alamat(), request.noHp(),
                request.golDarah());
        return pasienService.create(pasienReq);
    }

    /**
     * Memperbarui profil pasien (dipakai admin untuk edit data pasien).
     *
     * @param id      ID pasien
     * @param request data profil pasien baru
     * @return data pasien setelah diperbarui
     */
    public PasienResponse updatePasien(Long id, PasienRequest request) {
        return pasienService.update(id, request);
    }

    /**
     * Mengambil daftar semua pengguna, opsional difilter per role (FR-04 implisit).
     *
     * @param role role untuk filter (boleh null = semua role)
     * @return daftar pengguna dengan status aktif
     */
    public List<UserAdminResponse> getAllUsers(Role role) {
        List<User> users = (role == null) ? userRepository.findAll() : userRepository.findByRole(role);
        return users.stream().map(this::toUserAdminResponse).toList();
    }

    /**
     * Mengaktifkan atau menonaktifkan akun pengguna (FR-04 implisit).
     *
     * Akun nonaktif tidak dapat login (ditolak di AuthService.login).
     *
     * @param id      ID user yang akan diubah statusnya
     * @param active  status aktif baru (true = aktifkan, false = nonaktifkan)
     * @throws ResourceNotFoundException bila user tidak ditemukan
     */
    @Transactional
    public UserAdminResponse setUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
        user.setIsActive(active);
        userRepository.save(user);
        return toUserAdminResponse(user);
    }

    /**
     * Mengganti password pengguna (admin reset password dokter/pasien).
     *
     * Password baru di-hash dengan BCrypt sebelum disimpan. Admin tidak
     * perlu tahu password lama — ini fitur reset, bukan change password mandiri.
     *
     * @param id           ID user yang akan diganti passwordnya
     * @param newPassword  password baru (plain text, akan di-hash)
     * @throws ResourceNotFoundException bila user tidak ditemukan
     * @throws IllegalArgumentException  bila password baru kosong/terlalu pendek
     */
    @Transactional
    public UserAdminResponse resetPassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password baru minimal 6 karakter");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return toUserAdminResponse(user);
    }

    /**
     * Mengambil statistik ringkas untuk dashboard admin.
     *
     * @return jumlah pasien, dokter, poli, dan appointment
     */
    public DashboardResponse getDashboard() {
        long totalPasien = userRepository.countByRole(Role.PASIEN);
        long totalDokter = dokterRepository.count();
        long totalPoli = poliRepository.count();
        long totalAppointment = appointmentRepository.count();
        return new DashboardResponse(totalPasien, totalDokter, totalPoli, totalAppointment);
    }

    private UserAdminResponse toUserAdminResponse(User user) {
        return new UserAdminResponse(
                user.getId(), user.getNama(), user.getEmail(),
                user.getRole(), user.getIsActive());
    }

    private DokterResponse toDokterResponse(Dokter dokter) {
        return new DokterResponse(
                dokter.getId(),
                dokter.getUser().getId(),
                dokter.getUser().getNama(),
                dokter.getUser().getEmail(),
                dokter.getPoli().getId(),
                dokter.getPoli().getNama(),
                dokter.getNoStr(),
                dokter.getSpesialisasi(),
                dokter.getNoHp());
    }
}
