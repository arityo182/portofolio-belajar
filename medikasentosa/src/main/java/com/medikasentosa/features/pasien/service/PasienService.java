package com.medikasentosa.features.pasien.service;

import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.auth.repository.UserRepository;
import com.medikasentosa.features.pasien.dto.PasienRequest;
import com.medikasentosa.features.pasien.dto.PasienResponse;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.pasien.repository.PasienRepository;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service untuk logika bisnis manajemen data pasien.
 * Menangani pembuatan (termasuk generasi nomor rekam medis unik), pembacaan,
 * dan pembaruan profil pasien, serta memutakhirkan peran user menjadi PASIEN.
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class PasienService {

    private final PasienRepository pasienRepository;
    private final UserRepository userRepository;

    public PasienService(PasienRepository pasienRepository,
                         UserRepository userRepository) {
        this.pasienRepository = pasienRepository;
        this.userRepository = userRepository;
    }

    /**
     * Membuat profil pasien baru beserta nomor rekam medis unik yang digenerate otomatis.
     *
     * @param request data pasien baru (userId, nik, tanggalLahir, jenisKelamin, alamat, noHp, golDarah)
     * @return data pasien yang berhasil dibuat
     * @throws ResourceNotFoundException  bila user tidak ditemukan
     * @throws DuplicateResourceException bila user sudah terdaftar sebagai pasien
     */
    public PasienResponse create(PasienRequest request) {
        User user = findUserOrThrow(request.userId());

        if (pasienRepository.existsByUserId(request.userId())) {
            throw new DuplicateResourceException("User sudah terdaftar sebagai pasien");
        }

        if (user.getRole() != Role.PASIEN) {
            user.setRole(Role.PASIEN);
            userRepository.save(user);
        }

        Pasien pasien = Pasien.builder()
                .user(user)
                .noRekamMedis(generateNoRekamMedis())
                .nik(request.nik())
                .tanggalLahir(request.tanggalLahir())
                .jenisKelamin(request.jenisKelamin())
                .alamat(request.alamat())
                .noHp(request.noHp())
                .golDarah(request.golDarah())
                .build();

        pasienRepository.save(pasien);
        return toResponse(pasien);
    }

    /**
     * Mengambil seluruh daftar pasien.
     *
     * @return daftar semua pasien
     */
    public List<PasienResponse> getAll() {
        return pasienRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    /**
     * Mengambil satu pasien berdasarkan ID.
     *
     * @param id ID pasien yang dicari
     * @return data pasien yang ditemukan
     * @throws ResourceNotFoundException bila pasien tidak ditemukan
     */
    public PasienResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Mengambil entity Pasien (bukan DTO) berdasarkan ID — dipakai internal
     * oleh admin service untuk operasi yang butuh entity langsung.
     *
     * @param id ID pasien
     * @return entity Pasien
     * @throws ResourceNotFoundException bila pasien tidak ditemukan
     */
    public Pasien getEntityById(Long id) {
        return findOrThrow(id);
    }

    /**
     * Mengambil profil pasien berdasarkan ID user terkait.
     *
     * @param userId ID user yang dicari
     * @return data pasien milik user tersebut
     * @throws ResourceNotFoundException bila user tidak memiliki profil pasien
     */
    public PasienResponse getByUserId(Long userId) {
        Pasien pasien = pasienRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Pasien tidak ditemukan"));
        return toResponse(pasien);
    }

    /**
     * Memperbarui data profil pasien yang sudah ada.
     * Nomor rekam medis dan user terkait tidak dapat diubah.
     *
     * @param id      ID pasien yang akan diperbarui
     * @param request data pasien baru (nik, tanggalLahir, jenisKelamin, alamat, noHp, golDarah)
     * @return data pasien setelah diperbarui
     * @throws ResourceNotFoundException bila pasien tidak ditemukan
     */
    public PasienResponse update(Long id, PasienRequest request) {
        Pasien pasien = findOrThrow(id);
        pasien.setNik(request.nik());
        pasien.setTanggalLahir(request.tanggalLahir());
        pasien.setJenisKelamin(request.jenisKelamin());
        pasien.setAlamat(request.alamat());
        pasien.setNoHp(request.noHp());
        pasien.setGolDarah(request.golDarah());
        pasienRepository.save(pasien);
        return toResponse(pasien);
    }

    /**
     * Menghasilkan nomor rekam medis unik dengan format {@code RM-<tahun>-<6 digit urut>}.
     * Nomor urut dimulai dari jumlah pasien + 1 dan terus dinaikkan selama nomor
     * kandidat sudah dipakai, sehingga dijamin unik walau ada gap akibat penghapusan.
     *
     * @return nomor rekam medis unik
     */
    private String generateNoRekamMedis() {
        int year = LocalDate.now().getYear();
        long seq = pasienRepository.count() + 1;
        String candidate = String.format("RM-%d-%06d", year, seq);
        while (pasienRepository.existsByNoRekamMedis(candidate)) {
            seq++;
            candidate = String.format("RM-%d-%06d", year, seq);
        }
        return candidate;
    }

    private Pasien findOrThrow(Long id) {
        return pasienRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pasien tidak ditemukan"));
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
    }

    private PasienResponse toResponse(Pasien pasien) {
        return new PasienResponse(
                pasien.getId(),
                pasien.getUser().getId(),
                pasien.getUser().getNama(),
                pasien.getUser().getEmail(),
                pasien.getNoRekamMedis(),
                pasien.getNik(),
                pasien.getTanggalLahir(),
                pasien.getJenisKelamin(),
                pasien.getAlamat(),
                pasien.getNoHp(),
                pasien.getGolDarah());
    }
}
