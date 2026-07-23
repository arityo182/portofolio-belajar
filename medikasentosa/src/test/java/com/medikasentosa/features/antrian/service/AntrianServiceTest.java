package com.medikasentosa.features.antrian.service;

import com.medikasentosa.features.antrian.dto.AntrianResponse;
import com.medikasentosa.features.antrian.entity.Antrian;
import com.medikasentosa.features.antrian.repository.AntrianRepository;
import com.medikasentosa.features.appointment.entity.Appointment;
import com.medikasentosa.features.appointment.entity.StatusAppointment;
import com.medikasentosa.features.appointment.repository.AppointmentRepository;
import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.jadwaldokter.entity.Hari;
import com.medikasentosa.features.jadwaldokter.entity.JadwalDokter;
import com.medikasentosa.features.pasien.entity.JenisKelamin;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.poli.entity.Poli;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AntrianServiceTest {

    @Mock
    private AntrianRepository antrianRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AntrianService antrianService;

    private Appointment appointment;
    private Antrian antrian;

    @BeforeEach
    void setUp() {
        User userPasien = User.builder().id(5L).nama("Dewi Sartika").email("dewi@email.com")
                .password("hashed").role(Role.PASIEN).isActive(true).build();
        User userDokter = User.builder().id(3L).nama("dr. Hendra Gunawan").email("hendra.dokter@medika.com")
                .password("hashed").role(Role.DOKTER).isActive(true).build();
        Poli poli = Poli.builder().id(3L).nama("Poli THT").deskripsi("Spesialis THT").isActive(true).build();
        Dokter dokter = Dokter.builder().id(11L).user(userDokter).poli(poli)
                .noStr("STR-112233445").spesialisasi("Spesialis THT").noHp("082211223344").isActive(true).build();
        Pasien pasien = Pasien.builder().id(2L).user(userPasien).noRekamMedis("RM-2024-000002")
                .nik("3275012311900002").tanggalLahir(LocalDate.of(1992, 8, 10))
                .jenisKelamin(JenisKelamin.P).alamat("Jl. Mawar No. 7, Bandung")
                .noHp("081122334455").golDarah("AB").build();
        JadwalDokter jadwal = JadwalDokter.builder().id(5L).dokter(dokter).hari(Hari.RABU)
                .jamMulai(LocalTime.of(9, 0)).jamSelesai(LocalTime.of(15, 0)).kuota(15).isActive(true).build();

        appointment = Appointment.builder().id(200L).pasien(pasien).dokter(dokter).jadwal(jadwal)
                .tanggal(LocalDate.of(2024, 7, 24)).status(StatusAppointment.MENUNGGU).keluhan("Telinga sakit").build();

        antrian = Antrian.builder().id(1L).appointment(appointment).nomorUrut(1).build();
    }

    @Test
    void generateAntrian_success_createsNomorUrut() {
        when(appointmentRepository.findById(200L)).thenReturn(Optional.of(appointment));
        when(antrianRepository.existsByAppointmentId(200L)).thenReturn(false);
        when(antrianRepository.countByAppointmentJadwalAndAppointmentTanggal(appointment.getJadwal(), appointment.getTanggal())).thenReturn(0L);
        when(antrianRepository.save(any(Antrian.class))).thenAnswer(inv -> {
            Antrian a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        AntrianResponse response = antrianService.generateAntrian(200L);

        assertThat(response.nomorUrut()).isEqualTo(1);
        assertThat(response.namaPasien()).isEqualTo("Dewi Sartika");
        verify(antrianRepository).save(any(Antrian.class));
    }

    @Test
    void generateAntrian_fails_appointmentNotFound() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> antrianService.generateAntrian(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Appointment tidak ditemukan");

        verify(antrianRepository, never()).save(any());
    }

    @Test
    void getByAppointment_returnsAntrian() {
        when(antrianRepository.findByAppointmentId(200L)).thenReturn(Optional.of(antrian));

        AntrianResponse response = antrianService.getByAppointment(200L);

        assertThat(response.nomorUrut()).isEqualTo(1);
        assertThat(response.namaDokter()).isEqualTo("dr. Hendra Gunawan");
    }

    @Test
    void getAll_returnsAllAntrian() {
        when(antrianRepository.findAll()).thenReturn(List.of(antrian));

        List<AntrianResponse> result = antrianService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).namaPoli()).isEqualTo("Poli THT");
    }
}
