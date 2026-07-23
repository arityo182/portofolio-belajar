package com.medikasentosa.features.radiologi.service;

import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.pasien.entity.JenisKelamin;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.poli.entity.Poli;
import com.medikasentosa.features.radiologi.dto.RadiologiRequest;
import com.medikasentosa.features.radiologi.dto.RadiologiResponse;
import com.medikasentosa.features.radiologi.entity.Radiologi;
import com.medikasentosa.features.radiologi.entity.StatusRadiologi;
import com.medikasentosa.features.radiologi.repository.RadiologiRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RadiologiServiceTest {

    @Mock
    private RadiologiRepository radiologiRepository;
    @Mock
    private RekamMedisRepository rekamMedisRepository;

    @InjectMocks
    private RadiologiService radiologiService;

    private RekamMedis rekamMedis;
    private Radiologi radiologi;
    private RadiologiRequest request;

    @BeforeEach
    void setUp() {
        User userPasien = User.builder().id(12L).nama("Joko Widodo").email("joko@email.com")
                .password("hashed").role(Role.PASIEN).isActive(true).build();
        User userDokter = User.builder().id(10L).nama("dr. Agus Riyanto").email("agus.dokter@medika.com")
                .password("hashed").role(Role.DOKTER).isActive(true).build();
        Poli poli = Poli.builder().id(11L).nama("Poli Paru").deskripsi("Spesialis Paru").isActive(true).build();
        Dokter dokter = Dokter.builder().id(18L).user(userDokter).poli(poli)
                .noStr("STR-554433221").spesialisasi("Spesialis Paru").noHp("082211009988").isActive(true).build();
        Pasien pasien = Pasien.builder().id(10L).user(userPasien).noRekamMedis("RM-2024-000010")
                .nik("3275012312010010").tanggalLahir(LocalDate.of(1965, 7, 21))
                .jenisKelamin(JenisKelamin.L).alamat("Jl. Diponegoro No. 1, Jakarta")
                .noHp("081377889900").golDarah("A").build();

        rekamMedis = RekamMedis.builder().id(4L).pasien(pasien).dokter(dokter)
                .diagnosa("Pneumonia").tindakan("Antibiotik dan foto thorax")
                .catatan("Rontgen dada PA").build();

        radiologi = Radiologi.builder().id(1L).rekamMedis(rekamMedis).jenisRadiologi("X-Ray Dada")
                .catatanDokter("Curiga pneumonia lobaris").status(StatusRadiologi.MENUNGGU).build();

        request = new RadiologiRequest(4L, "X-Ray Dada", "Curiga pneumonia lobaris");
    }

    @Test
    void create_success_returnsRadiologiResponse() {
        when(rekamMedisRepository.findById(4L)).thenReturn(Optional.of(rekamMedis));
        when(radiologiRepository.save(any(Radiologi.class))).thenAnswer(inv -> {
            Radiologi r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        RadiologiResponse response = radiologiService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.jenisRadiologi()).isEqualTo("X-Ray Dada");
        assertThat(response.status()).isEqualTo(StatusRadiologi.MENUNGGU);
        verify(radiologiRepository).save(any(Radiologi.class));
    }

    @Test
    void create_fails_rekamMedisNotFound() {
        when(rekamMedisRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> radiologiService.create(new RadiologiRequest(999L, "CT-Scan", null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Rekam medis tidak ditemukan");

        verify(radiologiRepository, never()).save(any());
    }

    @Test
    void getAll_returnsAllRadiologi() {
        when(radiologiRepository.findAll()).thenReturn(List.of(radiologi));

        List<RadiologiResponse> result = radiologiService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).namaPasien()).isEqualTo("Joko Widodo");
    }

    @Test
    void getByRekamMedis_returnsRadiologiList() {
        when(radiologiRepository.findByRekamMedisId(4L)).thenReturn(List.of(radiologi));

        List<RadiologiResponse> result = radiologiService.getByRekamMedis(4L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).noRekamMedis()).isEqualTo("RM-2024-000010");
    }

    @Test
    void updateHasil_fillsHasilAndSetsSelesai() {
        when(radiologiRepository.findById(1L)).thenReturn(Optional.of(radiologi));

        RadiologiResponse response = radiologiService.updateHasil(1L,
                "Tampak infiltrat di lobus kanan bawah", "/images/thorax-001.jpg");

        assertThat(response.hasilDeskripsi()).isEqualTo("Tampak infiltrat di lobus kanan bawah");
        assertThat(response.gambarUrl()).isEqualTo("/images/thorax-001.jpg");
        assertThat(response.status()).isEqualTo(StatusRadiologi.SELESAI);
        verify(radiologiRepository).save(radiologi);
    }
}
