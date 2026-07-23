package com.medikasentosa.features.rekammedis.service;

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
import com.medikasentosa.features.rekammedis.dto.RekamMedisRequest;
import com.medikasentosa.features.rekammedis.dto.RekamMedisResponse;
import com.medikasentosa.features.rekammedis.entity.RekamMedis;
import com.medikasentosa.features.rekammedis.repository.RekamMedisRepository;
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
class RekamMedisServiceTest {

    @Mock
    private RekamMedisRepository rekamMedisRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private RekamMedisService rekamMedisService;

    private Appointment appointment;
    private RekamMedis rekamMedis;
    private RekamMedisRequest request;

    @BeforeEach
    void setUp() {
        User userPasien = User.builder().id(5L).nama("Bambang Sumantri").email("bambang@email.com")
                .password("hashed").role(Role.PASIEN).isActive(true).build();
        User userDokter = User.builder().id(4L).nama("dr. Dian Permata").email("dian.dokter@medika.com")
                .password("hashed").role(Role.DOKTER).isActive(true).build();
        Poli poli = Poli.builder().id(4L).nama("Poli Dalam").deskripsi("Penyakit Dalam").isActive(true).build();
        Dokter dokter = Dokter.builder().id(12L).user(userDokter).poli(poli)
                .noStr("STR-556677889").spesialisasi("Spesialis Penyakit Dalam")
                .noHp("082233445566").isActive(true).build();
        Pasien pasien = Pasien.builder().id(3L).user(userPasien).noRekamMedis("RM-2024-000003")
                .nik("3275012311950003").tanggalLahir(LocalDate.of(1975, 11, 20))
                .jenisKelamin(JenisKelamin.L).alamat("Jl. Dahlia No. 9, Surabaya")
                .noHp("081133445566").golDarah("A").build();
        JadwalDokter jadwal = JadwalDokter.builder().id(6L).dokter(dokter).hari(Hari.KAMIS)
                .jamMulai(LocalTime.of(10, 0)).jamSelesai(LocalTime.of(16, 0)).kuota(12).isActive(true).build();

        appointment = Appointment.builder().id(300L).pasien(pasien).dokter(dokter).jadwal(jadwal)
                .tanggal(LocalDate.of(2024, 7, 25)).status(StatusAppointment.MENUNGGU)
                .keluhan("Demam tinggi 3 hari").build();

        rekamMedis = RekamMedis.builder().id(1L).pasien(pasien).dokter(dokter).appointment(appointment)
                .diagnosa("Demam Tifoid").tindakan("Pemberian antibiotik IV").catatan("Pasien perlu rawat inap").build();

        request = new RekamMedisRequest(300L, "Demam Tifoid", "Pemberian antibiotik IV", "Pasien perlu rawat inap");
    }

    @Test
    void create_success_setsAppointmentSelesai() {
        when(appointmentRepository.findById(300L)).thenReturn(Optional.of(appointment));
        when(rekamMedisRepository.existsByAppointmentId(300L)).thenReturn(false);
        when(rekamMedisRepository.save(any(RekamMedis.class))).thenAnswer(inv -> {
            RekamMedis rm = inv.getArgument(0);
            rm.setId(1L);
            return rm;
        });

        RekamMedisResponse response = rekamMedisService.create(request);

        assertThat(response.diagnosa()).isEqualTo("Demam Tifoid");
        assertThat(response.tindakan()).isEqualTo("Pemberian antibiotik IV");
        assertThat(appointment.getStatus()).isEqualTo(StatusAppointment.SELESAI);
        verify(appointmentRepository).save(appointment);
        verify(rekamMedisRepository).save(any(RekamMedis.class));
    }

    @Test
    void create_fails_appointmentNotFound() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rekamMedisService.create(new RekamMedisRequest(999L, "Flu", "Istirahat", null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Appointment tidak ditemukan");

        verify(rekamMedisRepository, never()).save(any());
    }

    @Test
    void getById_returnsRekamMedis() {
        when(rekamMedisRepository.findById(1L)).thenReturn(Optional.of(rekamMedis));

        RekamMedisResponse response = rekamMedisService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.namaPasien()).isEqualTo("Bambang Sumantri");
        assertThat(response.namaDokter()).isEqualTo("dr. Dian Permata");
    }

    @Test
    void getByPasien_returnsRiwayat() {
        when(rekamMedisRepository.findByPasienId(3L)).thenReturn(List.of(rekamMedis));

        List<RekamMedisResponse> result = rekamMedisService.getByPasien(3L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).noRekamMedis()).isEqualTo("RM-2024-000003");
    }

    @Test
    void getByAppointment_returnsRekamMedis() {
        when(rekamMedisRepository.findByAppointmentId(300L)).thenReturn(Optional.of(rekamMedis));

        RekamMedisResponse response = rekamMedisService.getByAppointment(300L);

        assertThat(response.appointmentId()).isEqualTo(300L);
        assertThat(response.catatan()).isEqualTo("Pasien perlu rawat inap");
    }
}
