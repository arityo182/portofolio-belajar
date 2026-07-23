package com.medikasentosa.features.rekammedis.service;

import com.medikasentosa.features.appointment.entity.Appointment;
import com.medikasentosa.features.appointment.entity.StatusAppointment;
import com.medikasentosa.features.appointment.repository.AppointmentRepository;
import com.medikasentosa.features.rekammedis.dto.RekamMedisRequest;
import com.medikasentosa.features.rekammedis.dto.RekamMedisResponse;
import com.medikasentosa.features.rekammedis.entity.RekamMedis;
import com.medikasentosa.features.rekammedis.repository.RekamMedisRepository;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service untuk logika bisnis manajemen rekam medis.
 * Menangani pembuatan rekam medis dari sebuah appointment (sekaligus menandai
 * appointment tersebut SELESAI) serta pembacaan rekam medis.
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class RekamMedisService {

    private final RekamMedisRepository rekamMedisRepository;
    private final AppointmentRepository appointmentRepository;

    public RekamMedisService(RekamMedisRepository rekamMedisRepository,
                             AppointmentRepository appointmentRepository) {
        this.rekamMedisRepository = rekamMedisRepository;
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Membuat rekam medis baru dari sebuah appointment dengan validasi berurutan:
     * <ol>
     *     <li>Appointment harus ada (404).</li>
     *     <li>Appointment belum memiliki rekam medis (409).</li>
     * </ol>
     * Pasien dan dokter diturunkan dari appointment. Setelah rekam medis tersimpan,
     * status appointment diperbarui menjadi {@code SELESAI}. Seluruh operasi dijalankan
     * dalam satu transaksi.
     *
     * @param request data rekam medis baru (appointmentId, diagnosa, tindakan, catatan)
     * @return data rekam medis yang berhasil dibuat
     * @throws ResourceNotFoundException  bila appointment tidak ditemukan
     * @throws DuplicateResourceException bila appointment sudah memiliki rekam medis
     */
    @Transactional
    public RekamMedisResponse create(RekamMedisRequest request) {
        // 1. Appointment ada
        Appointment appointment = appointmentRepository.findById(request.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment tidak ditemukan"));

        // 2. Appointment belum punya rekam medis
        if (rekamMedisRepository.existsByAppointmentId(appointment.getId())) {
            throw new DuplicateResourceException("Appointment ini sudah memiliki rekam medis");
        }

        // 3. Pasien & dokter diambil dari appointment
        RekamMedis rekamMedis = RekamMedis.builder()
                .pasien(appointment.getPasien())
                .dokter(appointment.getDokter())
                .appointment(appointment)
                .diagnosa(request.diagnosa())
                .tindakan(request.tindakan())
                .catatan(request.catatan())
                .build();

        // 4. Simpan rekam medis
        rekamMedisRepository.save(rekamMedis);

        // 5. Tandai appointment SELESAI
        appointment.setStatus(StatusAppointment.SELESAI);
        appointmentRepository.save(appointment);

        return toResponse(rekamMedis);
    }

    /**
     * Mengambil satu rekam medis berdasarkan ID.
     *
     * @param id ID rekam medis yang dicari
     * @return data rekam medis yang ditemukan
     * @throws ResourceNotFoundException bila rekam medis tidak ditemukan
     */
    public RekamMedisResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Mengambil seluruh rekam medis milik seorang pasien (riwayat rekam medis).
     *
     * @param pasienId ID pasien yang dicari
     * @return daftar rekam medis pasien tersebut
     */
    public List<RekamMedisResponse> getByPasien(Long pasienId) {
        return rekamMedisRepository.findByPasienId(pasienId)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Mengambil seluruh rekam medis yang dibuat oleh seorang dokter (terurut terbaru).
     *
     * @param dokterId ID dokter
     * @return daftar rekam medis yang dibuat dokter tersebut
     */
    public List<RekamMedisResponse> getByDokter(Long dokterId) {
        return rekamMedisRepository.findByDokterIdOrderByCreatedAtDesc(dokterId)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Mengambil rekam medis milik sebuah appointment.
     *
     * @param appointmentId ID appointment yang dicari
     * @return data rekam medis appointment tersebut
     * @throws ResourceNotFoundException bila rekam medis belum dibuat
     */
    public RekamMedisResponse getByAppointment(Long appointmentId) {
        RekamMedis rekamMedis = rekamMedisRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Rekam medis tidak ditemukan"));
        return toResponse(rekamMedis);
    }

    private RekamMedis findOrThrow(Long id) {
        return rekamMedisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rekam medis tidak ditemukan"));
    }

    private RekamMedisResponse toResponse(RekamMedis rekamMedis) {
        Appointment appointment = rekamMedis.getAppointment();
        return new RekamMedisResponse(
                rekamMedis.getId(),
                appointment.getId(),
                rekamMedis.getPasien().getId(),
                rekamMedis.getPasien().getUser().getNama(),
                rekamMedis.getPasien().getNoRekamMedis(),
                rekamMedis.getDokter().getId(),
                rekamMedis.getDokter().getUser().getNama(),
                rekamMedis.getDiagnosa(),
                rekamMedis.getTindakan(),
                rekamMedis.getCatatan(),
                appointment.getTanggal(),
                rekamMedis.getCreatedAt());
    }
}
