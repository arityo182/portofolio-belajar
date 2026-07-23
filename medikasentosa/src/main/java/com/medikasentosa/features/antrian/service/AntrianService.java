package com.medikasentosa.features.antrian.service;

import com.medikasentosa.features.antrian.dto.AntrianResponse;
import com.medikasentosa.features.antrian.entity.Antrian;
import com.medikasentosa.features.antrian.repository.AntrianRepository;
import com.medikasentosa.features.appointment.entity.Appointment;
import com.medikasentosa.features.appointment.repository.AppointmentRepository;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service untuk logika bisnis manajemen antrian.
 * Menangani generasi nomor antrian untuk sebuah appointment serta pembacaan antrian.
 * Dipisahkan dari pembuatan appointment demi menjaga single-responsibility.
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class AntrianService {

    private final AntrianRepository antrianRepository;
    private final AppointmentRepository appointmentRepository;

    public AntrianService(AntrianRepository antrianRepository,
                          AppointmentRepository appointmentRepository) {
        this.antrianRepository = antrianRepository;
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Menggenerate antrian untuk sebuah appointment. Nomor urut dihitung dari
     * jumlah antrian yang sudah ada pada jadwal dan tanggal appointment tersebut,
     * ditambah satu.
     *
     * @param appointmentId ID appointment yang akan diberi antrian
     * @return data antrian yang berhasil dibuat
     * @throws ResourceNotFoundException  bila appointment tidak ditemukan
     * @throws DuplicateResourceException bila appointment sudah memiliki antrian
     */
    public AntrianResponse generateAntrian(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment tidak ditemukan"));

        if (antrianRepository.existsByAppointmentId(appointmentId)) {
            throw new DuplicateResourceException("Antrian untuk appointment ini sudah ada");
        }

        long sudahAda = antrianRepository.countByAppointmentJadwalAndAppointmentTanggal(
                appointment.getJadwal(), appointment.getTanggal());
        int nomorUrut = (int) sudahAda + 1;

        Antrian antrian = Antrian.builder()
                .appointment(appointment)
                .nomorUrut(nomorUrut)
                .build();

        antrianRepository.save(antrian);
        return toResponse(antrian);
    }

    /**
     * Mengambil antrian milik sebuah appointment.
     *
     * @param appointmentId ID appointment yang dicari
     * @return data antrian appointment tersebut
     * @throws ResourceNotFoundException bila antrian belum digenerate
     */
    public AntrianResponse getByAppointment(Long appointmentId) {
        Antrian antrian = antrianRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Antrian tidak ditemukan"));
        return toResponse(antrian);
    }

    /**
     * Mengambil seluruh daftar antrian.
     *
     * @return daftar semua antrian
     */
    public List<AntrianResponse> getAll() {
        return antrianRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    private AntrianResponse toResponse(Antrian antrian) {
        Appointment appointment = antrian.getAppointment();
        return new AntrianResponse(
                antrian.getId(), appointment.getId(), antrian.getNomorUrut(),
                appointment.getPasien().getUser().getNama(),
                appointment.getDokter().getUser().getNama(),
                appointment.getDokter().getPoli().getNama(),
                appointment.getTanggal(),
                appointment.getStatus());
    }
}
