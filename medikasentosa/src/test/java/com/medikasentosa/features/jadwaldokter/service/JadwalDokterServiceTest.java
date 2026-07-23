package com.medikasentosa.features.jadwaldokter.service;

import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.dokter.repository.DokterRepository;
import com.medikasentosa.features.jadwaldokter.dto.JadwalDokterRequest;
import com.medikasentosa.features.jadwaldokter.dto.JadwalDokterResponse;
import com.medikasentosa.features.jadwaldokter.entity.Hari;
import com.medikasentosa.features.jadwaldokter.entity.JadwalDokter;
import com.medikasentosa.features.jadwaldokter.repository.JadwalDokterRepository;
import com.medikasentosa.features.poli.entity.Poli;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JadwalDokterServiceTest {

    @Mock
    private JadwalDokterRepository jadwalDokterRepository;

    @Mock
    private DokterRepository dokterRepository;

    @InjectMocks
    private JadwalDokterService jadwalDokterService;

    private Dokter dokter;
    private JadwalDokter jadwal;
    private JadwalDokterRequest request;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(3L)
                .nama("dr. Siti Rahmawati")
                .email("siti.dokter@medika.com")
                .password("hashed")
                .role(Role.DOKTER)
                .build();

        Poli poli = Poli.builder()
                .id(5L)
                .nama("Poli Anak")
                .deskripsi("Pelayanan spesialis anak")
                .isActive(true)
                .build();

        dokter = Dokter.builder()
                .id(10L)
                .user(user)
                .poli(poli)
                .noStr("STR-987654321")
                .spesialisasi("Spesialis Anak")
                .noHp("082112345678")
                .isActive(true)
                .build();

        jadwal = JadwalDokter.builder()
                .id(1L)
                .dokter(dokter)
                .hari(Hari.SENIN)
                .jamMulai(LocalTime.of(8, 0))
                .jamSelesai(LocalTime.of(12, 0))
                .kuota(20)
                .isActive(true)
                .build();

        request = new JadwalDokterRequest(10L, Hari.SELASA, LocalTime.of(9, 0), LocalTime.of(14, 0), 15);
    }

    @Test
    void create_success_returnsJadwalResponse() {
        when(dokterRepository.findById(10L)).thenReturn(Optional.of(dokter));
        when(jadwalDokterRepository.save(any(JadwalDokter.class))).thenAnswer(inv -> {
            JadwalDokter j = inv.getArgument(0);
            j.setId(2L);
            return j;
        });

        JadwalDokterResponse response = jadwalDokterService.create(request);

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.dokterId()).isEqualTo(10L);
        assertThat(response.hari()).isEqualTo(Hari.SELASA);
        assertThat(response.kuota()).isEqualTo(15);
        verify(jadwalDokterRepository).save(any(JadwalDokter.class));
    }

    @Test
    void create_fails_dokterNotFound() {
        when(dokterRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jadwalDokterService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Dokter tidak ditemukan");

        verify(jadwalDokterRepository, never()).save(any());
    }

    @Test
    void create_fails_invalidTime() {
        when(dokterRepository.findById(10L)).thenReturn(Optional.of(dokter));
        JadwalDokterRequest invalid = new JadwalDokterRequest(10L, Hari.RABU, LocalTime.of(15, 0), LocalTime.of(10, 0), 10);

        assertThatThrownBy(() -> jadwalDokterService.create(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Jam mulai harus sebelum jam selesai");

        verify(jadwalDokterRepository, never()).save(any());
    }

    @Test
    void getByDokter_returnsJadwalList() {
        when(dokterRepository.findById(10L)).thenReturn(Optional.of(dokter));
        when(jadwalDokterRepository.findByDokterAndIsActiveTrue(dokter)).thenReturn(List.of(jadwal));

        List<JadwalDokterResponse> result = jadwalDokterService.getByDokter(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).dokterId()).isEqualTo(10L);
        assertThat(result.get(0).hari()).isEqualTo(Hari.SENIN);
    }

    @Test
    void update_success_updatesJadwal() {
        JadwalDokterRequest updateReq = new JadwalDokterRequest(10L, Hari.KAMIS, LocalTime.of(8, 0), LocalTime.of(11, 0), 25);
        when(jadwalDokterRepository.findById(1L)).thenReturn(Optional.of(jadwal));

        JadwalDokterResponse response = jadwalDokterService.update(1L, updateReq);

        assertThat(response.hari()).isEqualTo(Hari.KAMIS);
        assertThat(response.kuota()).isEqualTo(25);
        verify(jadwalDokterRepository).save(jadwal);
    }

    @Test
    void deactivate_setsIsActiveFalse() {
        when(jadwalDokterRepository.findById(1L)).thenReturn(Optional.of(jadwal));

        jadwalDokterService.deactivate(1L);

        assertThat(jadwal.getIsActive()).isFalse();
        verify(jadwalDokterRepository).save(jadwal);
        verify(jadwalDokterRepository, never()).delete(any());
    }
}
