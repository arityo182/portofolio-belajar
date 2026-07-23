package com.medikasentosa.shared.notification;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

/**
 * Service untuk broadcast notifikasi real-time via WebSocket.
 *
 * <p>Ini adalah lapisan tambahan (cross-cutting concern) — tidak mengubah
 * business logic endpoint REST yang sudah ada. Service ini dipanggil
 * sebagai efek samping setelah transaksi bisnis (mis. status appointment
 * berubah) untuk memberi tahu client yang subscribe.</p>
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class NotificationService {

    private final SimpMessagingTemplate messaging;

    public NotificationService(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    /**
     * Broadcast ke seluruh subscriber bahwa status appointment berubah.
     * Client frontend subscribe ke {@code /topic/appointment/{id}} untuk
     * menerima update real-time.
     *
     * @param appointmentId ID appointment yang berubah statusnya
     * @param status        status baru
     * @param pasienId      ID pasien pemilik appointment
     */
    public void broadcastStatusAppointment(Long appointmentId, String status, Long pasienId) {
        var payload = Map.of(
                "type", "APPOINTMENT_STATUS",
                "appointmentId", appointmentId,
                "status", status,
                "message", "Status janji temu Anda berubah menjadi: " + status
        );
        // Broadcast ke topic spesifik appointment
        messaging.convertAndSend("/topic/appointment/" + appointmentId, payload);
        // Broadcast juga ke channel pasien
        messaging.convertAndSend("/topic/pasien/" + pasienId, payload);
    }

    /**
     * Broadcast status antrian terbaru (nomor yang sedang dipanggil).
     *
     * @param jadwalId       ID jadwal praktik
     * @param tanggal        tanggal praktik
     * @param nomorDipanggil nomor antrian yang sedang dipanggil
     */
    public void broadcastStatusAntrian(Long jadwalId, LocalDate tanggal, Integer nomorDipanggil) {
        var payload = Map.of(
                "type", "ANTRIAN_UPDATE",
                "jadwalId", jadwalId,
                "tanggal", tanggal.toString(),
                "nomorDipanggil", nomorDipanggil,
                "message", "Nomor antrian " + nomorDipanggil + " sedang dipanggil"
        );
        messaging.convertAndSend("/topic/antrian/" + jadwalId + "/" + tanggal, payload);
    }
}