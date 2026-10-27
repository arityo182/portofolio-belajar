package com.medikasentosa.shared.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service untuk broadcast notifikasi real-time via WebSocket.
 *
 * <p>Ini adalah lapisan tambahan (cross-cutting concern) — tidak mengubah
 * business logic endpoint REST yang sudah ada. Service ini dipanggil
 * sebagai efek samping setelah transaksi bisnis (mis. status appointment
 * berubah) untuk memberi tahu client yang subscribe.</p>
 *
 * <p>Setiap event dikirim ke topic pasien, topic dokter penanggung jawab, dan
 * topic appointment sehingga daftar appointment pasien maupun panel dokter
 * dapat diperbarui tanpa polling.</p>
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final SimpMessagingTemplate messaging;

    public NotificationService(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    /**
     * Broadcast bahwa appointment baru dibuat. Panel dokter memakai event ini
     * untuk memuat ulang daftar antrian pasien secara real-time.
     *
     * @param appointmentId ID appointment
     * @param pasienId      ID pasien
     * @param dokterId      ID dokter penanggung jawab
     * @param namaPasien    nama pasien untuk ditampilkan pada notifikasi dokter
     * @param tanggal       tanggal janji temu
     * @param namaPoli      nama poli
     */
    public void broadcastNewAppointment(Long appointmentId, Long pasienId, Long dokterId,
                                        String namaPasien, LocalDate tanggal, String namaPoli) {
        log.info("Broadcasting new appointment: appointmentId={}, dokterId={}", appointmentId, dokterId);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "APPOINTMENT_CREATED");
        payload.put("appointmentId", appointmentId);
        payload.put("pasienId", pasienId);
        payload.put("dokterId", dokterId);
        payload.put("namaPasien", namaPasien);
        payload.put("namaPoli", namaPoli);
        payload.put("tanggal", tanggal != null ? tanggal.toString() : null);
        payload.put("message", "Janji temu baru dari " + namaPasien);
        deliver(payload, appointmentId, pasienId, dokterId);
    }

    /**
     * Broadcast ke seluruh subscriber bahwa status appointment berubah.
     * Client frontend subscribe ke {@code /topic/appointment/{id}},
     * {@code /topic/pasien/{id}}, dan {@code /topic/dokter/{id}}.
     *
     * @param appointmentId ID appointment yang berubah statusnya
     * @param pasienId      ID pasien pemilik appointment
     * @param dokterId      ID dokter penanggung jawab
     * @param status        status baru
     */
    public void broadcastStatusAppointment(Long appointmentId, Long pasienId,
                                           Long dokterId, String status) {
        log.info("Broadcasting appointment status change: appointmentId={}, status={}", appointmentId, status);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "APPOINTMENT_STATUS");
        payload.put("appointmentId", appointmentId);
        payload.put("pasienId", pasienId);
        payload.put("dokterId", dokterId);
        payload.put("status", status);
        payload.put("message", "Status janji temu Anda berubah menjadi: " + status);
        deliver(payload, appointmentId, pasienId, dokterId);
    }

    /**
     * Broadcast status antrian terbaru (nomor yang sedang dipanggil).
     *
     * @param jadwalId       ID jadwal praktik
     * @param tanggal        tanggal praktik
     * @param nomorDipanggil nomor antrian yang sedang dipanggil
     */
    public void broadcastStatusAntrian(Long jadwalId, LocalDate tanggal, Integer nomorDipanggil) {
        log.info("Broadcasting antrian update: jadwalId={}, nomor={}", jadwalId, nomorDipanggil);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "ANTRIAN_UPDATE");
        payload.put("jadwalId", jadwalId);
        payload.put("tanggal", tanggal != null ? tanggal.toString() : null);
        payload.put("nomorDipanggil", nomorDipanggil);
        payload.put("message", "Nomor antrian " + nomorDipanggil + " sedang dipanggil");
        messaging.convertAndSend("/topic/antrian/" + jadwalId + "/" + tanggal, (Object) payload);
    }

    /**
     * Broadcast aksi pemanggilan antrian oleh dokter/admin. Event ini dikirim
     * ke pasien yang dipanggil, dokter pemilik jadwal, dan topic appointment.
     *
     * @param antrianId    ID antrian
     * @param appointmentId ID appointment terkait
     * @param pasienId     ID pasien
     * @param dokterId     ID dokter
     * @param jadwalId     ID jadwal praktik
     * @param tanggal      tanggal praktik
     * @param nomorUrut    nomor antrian yang dipanggil
     * @param message      pesan untuk pasien
     */
    public void broadcastAntrianCalled(Long antrianId, Long appointmentId, Long pasienId,
                                       Long dokterId, Long jadwalId, LocalDate tanggal,
                                       Integer nomorUrut, String message) {
        log.info("Broadcasting antrian call: antrianId={}, nomor={}", antrianId, nomorUrut);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "ANTRIAN_CALLED");
        payload.put("antrianId", antrianId);
        payload.put("appointmentId", appointmentId);
        payload.put("pasienId", pasienId);
        payload.put("dokterId", dokterId);
        payload.put("jadwalId", jadwalId);
        payload.put("tanggal", tanggal != null ? tanggal.toString() : null);
        payload.put("nomorUrut", nomorUrut);
        payload.put("message", message != null ? message : "Nomor antrian " + nomorUrut + " dipanggil");
        deliver(payload, appointmentId, pasienId, dokterId);
        if (jadwalId != null) {
            messaging.convertAndSend("/topic/antrian/" + jadwalId + "/" + tanggal, (Object) payload);
        }
    }

    private void deliver(Map<String, Object> payload, Long appointmentId, Long pasienId, Long dokterId) {
        if (appointmentId != null) {
            messaging.convertAndSend("/topic/appointment/" + appointmentId, (Object) payload);
        }
        if (pasienId != null) {
            messaging.convertAndSend("/topic/pasien/" + pasienId, (Object) payload);
        }
        if (dokterId != null) {
            messaging.convertAndSend("/topic/dokter/" + dokterId, (Object) payload);
        }
    }
}
