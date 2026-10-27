package com.medikasentosa.features.appointment.service;

import com.medikasentosa.features.antrian.repository.AntrianRepository;
import com.medikasentosa.features.antrian.service.AntrianService;
import com.medikasentosa.features.appointment.dto.AppointmentRequest;
import com.medikasentosa.features.appointment.dto.AppointmentResponse;
import com.medikasentosa.features.appointment.entity.Appointment;
import com.medikasentosa.features.appointment.entity.StatusAppointment;
import com.medikasentosa.features.appointment.repository.AppointmentRepository;
import com.medikasentosa.features.jadwaldokter.entity.Hari;
import com.medikasentosa.features.jadwaldokter.entity.JadwalDokter;
import com.medikasentosa.features.jadwaldokter.repository.JadwalDokterRepository;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.pasien.repository.PasienRepository;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import com.medikasentosa.shared.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service untuk logika bisnis manajemen janji temu (appointment).
 * Menangani pembuatan appointment dengan validasi berurutan (pasien, jadwal,
 * keaktifan jadwal, kecocokan hari, duplikasi booking, dan kuota), pembacaan,
 * serta pembaruan status.
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final PasienRepository pasienRepository;
    private final JadwalDokterRepository jadwalDokterRepository;
    private final AntrianRepository antrianRepository;
    private final AntrianService antrianService;
    private final NotificationService notificationService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PasienRepository pasienRepository,
                              JadwalDokterRepository jadwalDokterRepository,
                              AntrianRepository antrianRepository,
                              AntrianService antrianService,
                              NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.pasienRepository = pasienRepository;
        this.jadwalDokterRepository = jadwalDokterRepository;
        this.antrianRepository = antrianRepository;
        this.antrianService = antrianService;
        this.notificationService = notificationService;
    }

    /**
     * Membuat janji temu baru dengan serangkaian validasi berurutan:
     * <ol>
     *     <li>Pasien harus ada (404).</li>
     *     <li>Tanggal tidak boleh di masa lalu (400).</li>
     *     <li>Jadwal harus ada (404) dan dikunci selama transaksi agar kuota aman
     *         dari booking bersamaan.</li>
     *     <li>Jadwal harus aktif (400).</li>
     *     <li>Hari dari tanggal harus cocok dengan hari jadwal (400).</li>
     *     <li>Pasien belum memesan jadwal dan tanggal yang sama (409).</li>
     *     <li>Kuota jadwal belum penuh (400).</li>
     * </ol>
     * Dokter diturunkan dari {@code jadwal.getDokter()} dan status diset {@code MENUNGGU}.
     * <p>
     * Setelah appointment tersimpan, nomor antrian otomatis digenerate melalui
     * {@link AntrianService#generateAntrian(Long)} dalam transaksi yang sama, sehingga
     * response yang dikembalikan sudah langsung memuat {@code nomorAntrian}. Bila salah
     * satu langkah gagal, seluruh transaksi di-rollback (appointment dan antrian batal).
     *
     * @param request data appointment baru (pasienId, jadwalId, tanggal, keluhan)
     * @return data appointment yang berhasil dibuat, sudah memuat nomor antrian
     * @throws ResourceNotFoundException  bila pasien atau jadwal tidak ditemukan
     * @throws IllegalArgumentException   bila jadwal tidak aktif, hari tidak cocok, atau kuota penuh
     * @throws DuplicateResourceException bila pasien sudah memesan jadwal dan tanggal yang sama
     */
    @Transactional
    public AppointmentResponse create(AppointmentRequest request) {
        log.info("Creating appointment for pasienId={}, jadwalId={}", request.pasienId(), request.jadwalId());
        // 1. Pasien ada
        Pasien pasien = pasienRepository.findById(request.pasienId())
                .orElseThrow(() -> new ResourceNotFoundException("Pasien tidak ditemukan"));

        // 2. Tanggal tidak boleh di masa lalu
        if (request.tanggal() == null || request.tanggal().isBefore(LocalDate.now())) {
            log.warn("Appointment rejected: tanggal {} is in the past", request.tanggal());
            throw new IllegalArgumentException("Tanggal janji temu tidak boleh di masa lalu");
        }

        // 3. Jadwal ada dan dikunci (PESSIMISTIC_WRITE) agar pengecekan kuota +
        //    pembuatan antrian tidak balapan dengan request booking lain.
        JadwalDokter jadwal = jadwalDokterRepository.findByIdForUpdate(request.jadwalId())
                .orElseThrow(() -> new ResourceNotFoundException("Jadwal tidak ditemukan"));

        // 4. Jadwal aktif
        if (!Boolean.TRUE.equals(jadwal.getIsActive())) {
            log.warn("Appointment rejected: jadwal {} is not active", request.jadwalId());
            throw new IllegalArgumentException("Jadwal tidak aktif");
        }

        // 5. Hari dari tanggal cocok dengan hari jadwal
        Hari hariTanggal = toHari(request.tanggal());
        if (hariTanggal != jadwal.getHari()) {
            log.warn("Appointment rejected: date {} does not match jadwal day {}", request.tanggal(), jadwal.getHari());
            throw new IllegalArgumentException(
                    "Tanggal tidak sesuai hari jadwal (jadwal hari " + jadwal.getHari() + ")");
        }

        // 6. Pasien belum booking jadwal + tanggal yang sama (BATAL tidak dihitung)
        if (appointmentRepository.existsActiveByPasienIdAndJadwalAndTanggal(
                pasien.getId(), jadwal, request.tanggal())) {
            log.warn("Appointment rejected: duplicate booking for pasienId={} jadwalId={} tanggal={}",
                    pasien.getId(), request.jadwalId(), request.tanggal());
            throw new DuplicateResourceException(
                    "Pasien sudah memiliki janji temu pada jadwal dan tanggal ini");
        }

        // 7. Kuota belum penuh (BATAL tidak dihitung)
        long terisi = appointmentRepository.countActiveByJadwalAndTanggal(jadwal, request.tanggal());
        if (terisi >= jadwal.getKuota()) {
            log.warn("Appointment rejected: kuota penuh for jadwalId={}", request.jadwalId());
            throw new IllegalArgumentException("Kuota jadwal pada tanggal ini sudah penuh");
        }

        // 8. Simpan appointment (dokter dari jadwal, status MENUNGGU)
        Appointment appointment = Appointment.builder()
                .pasien(pasien)
                .dokter(jadwal.getDokter())
                .jadwal(jadwal)
                .tanggal(request.tanggal())
                .status(StatusAppointment.MENUNGGU)
                .keluhan(request.keluhan())
                .build();

        appointmentRepository.save(appointment);

        // 9. Auto-generate antrian dalam transaksi yang sama agar response
        //    langsung memuat nomorAntrian tanpa perlu panggilan endpoint terpisah.
        antrianService.generateAntrian(appointment.getId());

        // 10. Beri tahu pasien + panel dokter secara real-time.
        notificationService.broadcastNewAppointment(
                appointment.getId(),
                pasien.getId(),
                appointment.getDokter().getId(),
                pasien.getUser().getNama(),
                appointment.getTanggal(),
                appointment.getDokter().getPoli().getNama());

        return toResponse(appointment);
    }

    /**
     * Mengambil seluruh daftar appointment.
     *
     * @return daftar semua appointment
     */
    public List<AppointmentResponse> getAll() {
        return appointmentRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    public Page<AppointmentResponse> getAll(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<AppointmentResponse> getByPasien(Long pasienId, Pageable pageable) {
        return appointmentRepository.findByPasienId(pasienId, pageable).map(this::toResponse);
    }

    /**
     * Mengambil satu appointment berdasarkan ID.
     *
     * @param id ID appointment yang dicari
     * @return data appointment yang ditemukan
     * @throws ResourceNotFoundException bila appointment tidak ditemukan
     */
    public AppointmentResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Mengambil seluruh appointment milik seorang pasien.
     *
     * @param pasienId ID pasien yang dicari
     * @return daftar appointment pasien tersebut
     */
    public List<AppointmentResponse> getByPasien(Long pasienId) {
        return appointmentRepository.findByPasienId(pasienId)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Mengambil seluruh appointment milik seorang dokter (untuk panel dokter).
     *
     * @param dokterId ID dokter
     * @return daftar appointment dokter tersebut
     */
    public List<AppointmentResponse> getByDokter(Long dokterId) {
        return appointmentRepository.findByDokterId(dokterId)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Memperbarui status sebuah appointment.
     *
     * @param id     ID appointment yang akan diperbarui
     * @param status status baru appointment
     * @return data appointment setelah diperbarui
     * @throws ResourceNotFoundException bila appointment tidak ditemukan
     */
    public AppointmentResponse updateStatus(Long id, StatusAppointment status) {
        log.info("Updating appointment id={} status to {}", id, status);
        Appointment appointment = findOrThrow(id);
        appointment.setStatus(status);
        appointmentRepository.save(appointment);
        
        // Broadcast notifikasi real-time via WebSocket (efek samping, tidak mengubah REST)
        notificationService.broadcastStatusAppointment(
                id,
                appointment.getPasien().getId(),
                appointment.getDokter().getId(),
                status.name());

        return toResponse(appointment);
    }

    /**
     * Mengonversi tanggal menjadi enum {@link Hari} berdasarkan hari dalam seminggu
     * (MONDAY → SENIN, ... SUNDAY → MINGGU).
     *
     * @param tanggal tanggal yang dikonversi
     * @return enum {@link Hari} yang bersesuaian
     */
    private Hari toHari(LocalDate tanggal) {
        // DayOfWeek.getValue(): 1 = Senin ... 7 = Minggu, selaras dengan urutan enum Hari.
        return Hari.values()[tanggal.getDayOfWeek().getValue() - 1];
    }

    private Appointment findOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment tidak ditemukan"));
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        var antrian = antrianRepository.findByAppointmentId(appointment.getId()).orElse(null);
        Integer nomorAntrian = antrian != null ? antrian.getNomorUrut() : null;
        Long antrianId = antrian != null ? antrian.getId() : null;

        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPasien().getId(),
                appointment.getPasien().getUser().getNama(),
                appointment.getPasien().getNoRekamMedis(),
                appointment.getDokter().getId(),
                appointment.getDokter().getUser().getNama(),
                appointment.getDokter().getPoli().getNama(),
                appointment.getJadwal().getId(),
                appointment.getJadwal().getHari(),
                appointment.getJadwal().getJamMulai(),
                appointment.getJadwal().getJamSelesai(),
                appointment.getTanggal(),
                appointment.getStatus(),
                appointment.getKeluhan(),
                nomorAntrian,
                antrianId);
    }
}
