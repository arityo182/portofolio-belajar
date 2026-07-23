package com.medikasentosa.features.laboratorium.service;

import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.laboratorium.dto.LabOrderRequest;
import com.medikasentosa.features.laboratorium.dto.LabOrderResponse;
import com.medikasentosa.features.laboratorium.entity.LabOrder;
import com.medikasentosa.features.laboratorium.entity.StatusLab;
import com.medikasentosa.features.laboratorium.repository.LabOrderRepository;
import com.medikasentosa.features.pasien.entity.JenisKelamin;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.poli.entity.Poli;
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
class LabOrderServiceTest {

    @Mock
    private LabOrderRepository labOrderRepository;
    @Mock
    private RekamMedisRepository rekamMedisRepository;

    @InjectMocks
    private LabOrderService labOrderService;

    private RekamMedis rekamMedis;
    private LabOrder labOrder;
    private LabOrderRequest request;

    @BeforeEach
    void setUp() {
        User userPasien = User.builder().id(11L).nama("Tono Hartono").email("tono@email.com")
                .password("hashed").role(Role.PASIEN).isActive(true).build();
        User userDokter = User.builder().id(9L).nama("dr. Wati Kusuma").email("wati.dokter@medika.com")
                .password("hashed").role(Role.DOKTER).isActive(true).build();
        Poli poli = Poli.builder().id(10L).nama("Poli Dalam").deskripsi("Penyakit Dalam").isActive(true).build();
        Dokter dokter = Dokter.builder().id(17L).user(userDokter).poli(poli)
                .noStr("STR-667788990").spesialisasi("Spesialis Penyakit Dalam").noHp("082200112233").isActive(true).build();
        Pasien pasien = Pasien.builder().id(9L).user(userPasien).noRekamMedis("RM-2024-000009")
                .nik("3275012312000009").tanggalLahir(LocalDate.of(1978, 12, 3))
                .jenisKelamin(JenisKelamin.L).alamat("Jl. Sakura No. 6, Jakarta")
                .noHp("081322334455").golDarah("O").build();

        rekamMedis = RekamMedis.builder().id(3L).pasien(pasien).dokter(dokter)
                .diagnosa("Diabetes Melitus Tipe 2").tindakan("Kontrol gula darah").catatan("Puasa 10 jam sebelum tes").build();

        labOrder = LabOrder.builder().id(1L).rekamMedis(rekamMedis)
                .jenisPemeriksaan("Gula Darah Puasa").catatan("Ambil sampel darah pagi hari")
                .status(StatusLab.MENUNGGU).build();

        request = new LabOrderRequest(3L, "Gula Darah Puasa", "Ambil sampel darah pagi hari");
    }

    @Test
    void create_success_returnsLabOrderResponse() {
        when(rekamMedisRepository.findById(3L)).thenReturn(Optional.of(rekamMedis));
        when(labOrderRepository.save(any(LabOrder.class))).thenAnswer(inv -> {
            LabOrder o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        LabOrderResponse response = labOrderService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.jenisPemeriksaan()).isEqualTo("Gula Darah Puasa");
        assertThat(response.status()).isEqualTo(StatusLab.MENUNGGU);
        verify(labOrderRepository).save(any(LabOrder.class));
    }

    @Test
    void create_fails_rekamMedisNotFound() {
        when(rekamMedisRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> labOrderService.create(new LabOrderRequest(999L, "Darah Lengkap", null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Rekam medis tidak ditemukan");

        verify(labOrderRepository, never()).save(any());
    }

    @Test
    void getAll_returnsAllOrders() {
        when(labOrderRepository.findAll()).thenReturn(List.of(labOrder));

        List<LabOrderResponse> result = labOrderService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).namaPasien()).isEqualTo("Tono Hartono");
    }

    @Test
    void getByRekamMedis_returnsOrders() {
        when(labOrderRepository.findByRekamMedisId(3L)).thenReturn(List.of(labOrder));

        List<LabOrderResponse> result = labOrderService.getByRekamMedis(3L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).noRekamMedis()).isEqualTo("RM-2024-000009");
    }

    @Test
    void updateStatus_updatesStatus() {
        when(labOrderRepository.findById(1L)).thenReturn(Optional.of(labOrder));

        LabOrderResponse response = labOrderService.updateStatus(1L, StatusLab.PROSES);

        assertThat(response.status()).isEqualTo(StatusLab.PROSES);
        verify(labOrderRepository).save(labOrder);
    }
}
