package com.medikasentosa.features.appointment.service;

import com.medikasentosa.features.antrian.repository.AntrianRepository;
import com.medikasentosa.features.antrian.service.AntrianService;
import com.medikasentosa.features.appointment.dto.AppointmentRequest;
import com.medikasentosa.features.appointment.dto.AppointmentResponse;
import com.medikasentosa.features.appointment.entity.Appointment;
import com.medikasentosa.features.appointment.entity.StatusAppointment;
import com.medikasentosa.features.appointment.repository.AppointmentRepository;
import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.jadwaldokter.entity.Hari;
import com.medikasentosa.features.jadwaldokter.entity.JadwalDokter;
import com.medikasentosa.features.jadwaldokter.repository.JadwalDokterRepository;
import com.medikasentosa.features.pasien.entity.JenisKelamin;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.pasien.repository.PasienRepository;
import com.medikasentosa.features.poli.entity.Poli;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import com.medikasentosa.shared.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    /** Tanggal Senin di masa depan; dipakai agar tidak tertolak validasi tanggal lampau. */
    private static final LocalDate FUTURE_SENIN =
            LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PasienRepository pasienRepository;
    @Mock
    private JadwalDokterRepository jadwalDokterRepository;
    @Mock
    private AntrianRepository antrianRepository;
    @Mock
    private AntrianService antrianService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Pasien pasien;
    private Dokter dokter;
    private JadwalDokter jadwal;
    private Appointment appointment;
    private AppointmentRequest request;

    @BeforeEach
    void setUp() {
        User userPasien = User.builder().id(5L).nama("Rina Wijaya").email("rina@email.com")
                .password("hashed").role(Role.PASIEN).isActive(true).build();
        User userDokter = User.builder().id(3L).nama("dr. Budi Santoso").email("budi.dokter@medika.com")
                .password("hashed").role(Role.DOKTER).isActive(true).build();
        Poli poli = Poli.builder().id(2L).nama("Poli Jantung").deskripsi("Spesialis Jantung").isActive(true).build();

        pasien = Pasien.builder().id(1L).user(userPasien).noRekamMedis("RM-2024-000001")
                .nik("3275012304900001").tanggalLahir(LocalDate.of(1988, 5, 15))
                .jenisKelamin(JenisKelamin.P).alamat("Jl. Kenanga No. 3, Jakarta Timur")
                .noHp("085678901234").golDarah("B").build();

        dokter = Dokter.builder().id(10L).user(userDokter).poli(poli)
                .noStr("STR-123456789").spesialisasi("Spesialis Jantung")
                .noHp("081234567890").isActive(true).build();

        jadwal = JadwalDokter.builder().id(3L).dokter(dokter).hari(Hari.SENIN)
                .jamMulai(LocalTime.of(8, 0)).jamSelesai(LocalTime.of(14, 0)).kuota(10).isActive(true).build();

        appointment = Appointment.builder().id(100L).pasien(pasien).dokter(dokter).jadwal(jadwal)
                .tanggal(FUTURE_SENIN).status(StatusAppointment.MENUNGGU)
                .keluhan("Nyeri dada").build();

        request = new AppointmentRequest(1L, 3L, FUTURE_SENIN, "Nyeri dada");
    }

    @Test
    void create_success_createsAppointmentAndAntrian() {
        when(pasienRepository.findById(1L)).thenReturn(Optional.of(pasien));
        when(jadwalDokterRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(jadwal));
        when(appointmentRepository.existsActiveByPasienIdAndJadwalAndTanggal(1L, jadwal, FUTURE_SENIN))
                .thenReturn(false);
        when(appointmentRepository.countActiveByJadwalAndTanggal(jadwal, FUTURE_SENIN)).thenReturn(3L);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(100L);
            return a;
        });
        when(antrianRepository.findByAppointmentId(100L)).thenReturn(Optional.empty());

        AppointmentResponse response = appointmentService.create(request);

        assertThat(response.pasienId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(StatusAppointment.MENUNGGU);
        verify(antrianService).generateAntrian(100L);
        verify(notificationService).broadcastNewAppointment(
                eq(100L), eq(1L), eq(10L), eq("Rina Wijaya"), eq(FUTURE_SENIN), eq("Poli Jantung"));
    }

    @Test
    void create_fails_pasienNotFound() {
        when(pasienRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pasien tidak ditemukan");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void create_fails_tanggalLampau() {
        AppointmentRequest req = new AppointmentRequest(
                1L, 3L, LocalDate.now().minusDays(1), "Nyeri dada");
        when(pasienRepository.findById(1L)).thenReturn(Optional.of(pasien));

        assertThatThrownBy(() -> appointmentService.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tanggal janji temu tidak boleh di masa lalu");

        verify(jadwalDokterRepository, never()).findByIdForUpdate(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void create_fails_quotaFull() {
        when(pasienRepository.findById(1L)).thenReturn(Optional.of(pasien));
        when(jadwalDokterRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(jadwal));
        when(appointmentRepository.existsActiveByPasienIdAndJadwalAndTanggal(1L, jadwal, FUTURE_SENIN))
                .thenReturn(false);
        when(appointmentRepository.countActiveByJadwalAndTanggal(jadwal, FUTURE_SENIN)).thenReturn(10L);

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Kuota jadwal pada tanggal ini sudah penuh");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void getByPasien_returnsAppointments() {
        when(appointmentRepository.findByPasienId(1L)).thenReturn(List.of(appointment));
        when(antrianRepository.findByAppointmentId(100L)).thenReturn(Optional.empty());

        List<AppointmentResponse> result = appointmentService.getByPasien(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).namaPasien()).isEqualTo("Rina Wijaya");
    }

    @Test
    void getByDokter_returnsAppointments() {
        when(appointmentRepository.findByDokterId(10L)).thenReturn(List.of(appointment));
        when(antrianRepository.findByAppointmentId(100L)).thenReturn(Optional.empty());

        List<AppointmentResponse> result = appointmentService.getByDokter(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).namaDokter()).isEqualTo("dr. Budi Santoso");
    }

    @Test
    void updateStatus_updatesStatusAndBroadcasts() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));

        AppointmentResponse response = appointmentService.updateStatus(100L, StatusAppointment.DIPERIKSA);

        assertThat(response.status()).isEqualTo(StatusAppointment.DIPERIKSA);
        verify(notificationService).broadcastStatusAppointment(100L, 1L, 10L, "DIPERIKSA");
    }

    @Test
    void create_fails_jadwalNotFound() {
        when(pasienRepository.findById(1L)).thenReturn(Optional.of(pasien));
        when(jadwalDokterRepository.findByIdForUpdate(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Jadwal tidak ditemukan");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void create_fails_jadwalTidakAktif() {
        jadwal.setIsActive(false);
        when(pasienRepository.findById(1L)).thenReturn(Optional.of(pasien));
        when(jadwalDokterRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(jadwal));

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Jadwal tidak aktif");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void create_fails_hariTidakCocok() {
        // Jadwal SENIN, tapi tanggal adalah Rabu (dua hari setelah Senin).
        AppointmentRequest req = new AppointmentRequest(
                1L, 3L, FUTURE_SENIN.plusDays(2), "Sakit kepala");
        when(pasienRepository.findById(1L)).thenReturn(Optional.of(pasien));
        when(jadwalDokterRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(jadwal));

        assertThatThrownBy(() -> appointmentService.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hari");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void create_fails_doubleBooking() {
        when(pasienRepository.findById(1L)).thenReturn(Optional.of(pasien));
        when(jadwalDokterRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(jadwal));
        when(appointmentRepository.existsActiveByPasienIdAndJadwalAndTanggal(1L, jadwal, FUTURE_SENIN))
                .thenReturn(true);

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("sudah");

        verify(appointmentRepository, never()).save(any());
    }
}
