package com.medikasentosa.features.antrian.service;

import com.medikasentosa.features.antrian.dto.AntrianResponse;
import com.medikasentosa.features.antrian.entity.Antrian;
import com.medikasentosa.features.antrian.repository.AntrianRepository;
import com.medikasentosa.features.appointment.entity.Appointment;
import com.medikasentosa.features.appointment.entity.StatusAppointment;
import com.medikasentosa.features.appointment.repository.AppointmentRepository;
import com.medikasentosa.features.jadwaldokter.repository.JadwalDokterRepository;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import com.medikasentosa.shared.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service untuk logika bisnis manajemen antrian.
 * Menangani generasi nomor antrian untuk sebuah appointment, pembacaan antrian,
 * serta aksi pemanggilan antrian oleh dokter/admin.
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class AntrianService {

    private static final Logger log = LoggerFactory.getLogger(AntrianService.class);

    private final AntrianRepository antrianRepository;
    private final AppointmentRepository appointmentRepository;
    private final JadwalDokterRepository jadwalDokterRepository;
    private final NotificationService notificationService;

    public AntrianService(AntrianRepository antrianRepository,
                          AppointmentRepository appointmentRepository,
                          JadwalDokterRepository jadwalDokterRepository,
                          NotificationService notificationService) {
        this.antrianRepository = antrianRepository;
        this.appointmentRepository = appointmentRepository;
        this.jadwalDokterRepository = jadwalDokterRepository;
        this.notificationService = notificationService;
    }

    /**
     * Menggenerate antrian untuk sebuah appointment. Nomor urut dihitung dari
     * jumlah antrian yang sudah ada pada jadwal dan tanggal appointment tersebut,
     * ditambah satu. Jadwal dikunci selama transaksi agar dua booking bersamaan
     * tidak memperoleh nomor urut yang sama.
     *
     * @param appointmentId ID appointment yang akan diberi antrian
     * @return data antrian yang berhasil dibuat
     * @throws ResourceNotFoundException  bila appointment tidak ditemukan
     * @throws DuplicateResourceException bila appointment sudah memiliki antrian
     */
    @Transactional
    public AntrianResponse generateAntrian(Long appointmentId) {
        log.info("Generating antrian for appointmentId={}", appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment tidak ditemukan"));

        // Kunci jadwal agar count + insert nomor urut bersifat serial.
        if (appointment.getJadwal() != null) {
            jadwalDokterRepository.findByIdForUpdate(appointment.getJadwal().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Jadwal tidak ditemukan"));
        }

        if (antrianRepository.existsByAppointmentId(appointmentId)) {
            log.warn("Antrian already exists for appointmentId={}", appointmentId);
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
     * Mengambil satu antrian berdasarkan ID.
     *
     * @param id ID antrian
     * @return data antrian
     * @throws ResourceNotFoundException bila antrian tidak ditemukan
     */
    public AntrianResponse getById(Long id) {
        return toResponse(findOrThrow(id));
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

    /**
     * Mengambil antrian pada tanggal tertentu. Bila {@code dokterId} diisi,
     * hasil dibatasi pada antrian dokter tersebut.
     *
     * @param tanggal  tanggal antrian
     * @param dokterId filter dokter (opsional, null = semua dokter)
     * @return daftar antrian terurut berdasarkan nomor urut
     */
    public List<AntrianResponse> getByTanggal(LocalDate tanggal, Long dokterId) {
        var antrian = dokterId != null
                ? antrianRepository.findByAppointmentTanggalAndAppointmentDokterIdOrderByNomorUrutAsc(
                        tanggal, dokterId)
                : antrianRepository.findByAppointmentTanggalOrderByNomorUrutAsc(tanggal);
        return antrian.stream().map(this::toResponse).toList();
    }

    /**
     * Memanggil antrian: memvalidasi bahwa antrian berada pada tanggal hari ini
     * dan appointment belum selesai/batal, lalu mem-broadcast notifikasi ke
     * pasien dan panel dokter.
     *
     * @param id ID antrian yang dipanggil
     * @return data antrian yang dipanggil
     * @throws ResourceNotFoundException bila antrian tidak ditemukan
     * @throws IllegalArgumentException  bila antrian bukan untuk hari ini atau sudah selesai/batal
     */
    @Transactional
    public AntrianResponse panggil(Long id) {
        Antrian antrian = findOrThrow(id);
        Appointment appointment = antrian.getAppointment();

        if (appointment.getTanggal() == null || !appointment.getTanggal().equals(LocalDate.now())) {
            throw new IllegalArgumentException("Hanya antrian untuk hari ini yang dapat dipanggil");
        }
        if (appointment.getStatus() == StatusAppointment.SELESAI
                || appointment.getStatus() == StatusAppointment.BATAL) {
            throw new IllegalArgumentException("Antrian dengan status " + appointment.getStatus()
                    + " tidak dapat dipanggil");
        }

        String message = "Nomor antrian " + antrian.getNomorUrut() + " ("
                + appointment.getPasien().getUser().getNama() + ") dipanggil ke "
                + appointment.getDokter().getPoli().getNama();

        notificationService.broadcastAntrianCalled(
                antrian.getId(),
                appointment.getId(),
                appointment.getPasien().getId(),
                appointment.getDokter().getId(),
                appointment.getJadwal() != null ? appointment.getJadwal().getId() : null,
                appointment.getTanggal(),
                antrian.getNomorUrut(),
                message);
        notificationService.broadcastStatusAntrian(
                appointment.getJadwal() != null ? appointment.getJadwal().getId() : null,
                appointment.getTanggal(),
                antrian.getNomorUrut());

        log.info("Antrian {} (nomor {}) called", id, antrian.getNomorUrut());
        return toResponse(antrian);
    }

    private Antrian findOrThrow(Long id) {
        return antrianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Antrian tidak ditemukan"));
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
