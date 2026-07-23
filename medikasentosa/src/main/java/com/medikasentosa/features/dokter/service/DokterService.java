package com.medikasentosa.features.dokter.service;

import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.auth.repository.UserRepository;
import com.medikasentosa.features.dokter.dto.DokterRequest;
import com.medikasentosa.features.dokter.dto.DokterResponse;
import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.dokter.repository.DokterRepository;
import com.medikasentosa.features.poli.entity.Poli;
import com.medikasentosa.features.poli.repository.PoliRepository;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service untuk logika bisnis manajemen data dokter.
 * Menangani pembuatan, pembacaan, pembaruan, dan penghapusan profil dokter,
 * serta memutakhirkan peran (role) user terkait menjadi DOKTER saat pembuatan.
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class DokterService {

    private final DokterRepository dokterRepository;
    private final UserRepository userRepository;
    private final PoliRepository poliRepository;

    public DokterService(DokterRepository dokterRepository,
                         UserRepository userRepository,
                         PoliRepository poliRepository) {
        this.dokterRepository = dokterRepository;
        this.userRepository = userRepository;
        this.poliRepository = poliRepository;
    }

    /**
     * Membuat profil dokter baru dan menyetel peran user terkait menjadi DOKTER.
     *
     * @param request data dokter baru (userId, poliId, noStr, spesialisasi, noHp)
     * @return data dokter yang berhasil dibuat
     * @throws ResourceNotFoundException  bila user atau poli tidak ditemukan
     * @throws DuplicateResourceException bila user sudah terdaftar sebagai dokter
     */
    public DokterResponse create(DokterRequest request) {
        User user = findUserOrThrow(request.userId());

        if (dokterRepository.existsByUserId(request.userId())) {
            throw new DuplicateResourceException("User ini sudah terdaftar sebagai dokter");
        }

        Poli poli = findPoliOrThrow(request.poliId());

        user.setRole(Role.DOKTER);
        userRepository.save(user);

        Dokter dokter = Dokter.builder()
                .user(user)
                .poli(poli)
                .noStr(request.noStr())
                .spesialisasi(request.spesialisasi())
                .noHp(request.noHp())
                .isActive(true)
                .build();

        dokterRepository.save(dokter);
        return toResponse(dokter);
    }

    /**
     * Mengambil seluruh daftar dokter.
     *
     * @return daftar semua dokter
     */
    public List<DokterResponse> getAll() {
        return dokterRepository.findByIsActiveTrue()
                .stream().map(this::toResponse).toList();
    }

    /**
     * Mengambil satu dokter berdasarkan ID.
     *
     * @param id ID dokter yang dicari
     * @return data dokter yang ditemukan
     * @throws ResourceNotFoundException bila dokter tidak ditemukan
     */
    public DokterResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Mengambil profil dokter berdasarkan ID user terkait.
     *
     * @param userId ID user yang dicari
     * @return data dokter milik user tersebut
     * @throws ResourceNotFoundException bila user tidak memiliki profil dokter
     */
    public DokterResponse getByUserId(Long userId) {
        Dokter dokter = dokterRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Dokter tidak ditemukan"));
        return toResponse(dokter);
    }

    /**
     * Mengambil seluruh dokter yang bertugas pada sebuah poli.
     *
     * @param poliId ID poli yang dicari
     * @return daftar dokter pada poli tersebut
     * @throws ResourceNotFoundException bila poli tidak ditemukan
     */
    public List<DokterResponse> getByPoli(Long poliId) {
        Poli poli = findPoliOrThrow(poliId);
        return dokterRepository.findByPoliAndIsActiveTrue(poli)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Memperbarui data profil dokter yang sudah ada.
     *
     * @param id      ID dokter yang akan diperbarui
     * @param request data dokter baru (userId, poliId, noStr, spesialisasi, noHp)
     * @return data dokter setelah diperbarui
     * @throws ResourceNotFoundException bila dokter atau poli tidak ditemukan
     */
    public DokterResponse update(Long id, DokterRequest request) {
        Dokter dokter = findOrThrow(id);
        Poli poli = findPoliOrThrow(request.poliId());

        dokter.setPoli(poli);
        dokter.setNoStr(request.noStr());
        dokter.setSpesialisasi(request.spesialisasi());
        dokter.setNoHp(request.noHp());
        dokterRepository.save(dokter);
        return toResponse(dokter);
    }

    /**
     * Menonaktifkan sebuah dokter (soft delete) dengan menyetel {@code isActive} menjadi false.
     *
     * @param id ID dokter yang akan dinonaktifkan
     * @throws ResourceNotFoundException bila dokter tidak ditemukan
     */
    public void delete(Long id) {
        Dokter dokter = findOrThrow(id);
        dokter.setIsActive(false);
        dokterRepository.save(dokter);
    }

    private Dokter findOrThrow(Long id) {
        return dokterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dokter tidak ditemukan"));
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
    }

    private Poli findPoliOrThrow(Long id) {
        return poliRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Poli tidak ditemukan"));
    }

    private DokterResponse toResponse(Dokter dokter) {
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
