package com.medikasentosa.features.laboratorium.service;

import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.laboratorium.dto.LabHasilRequest;
import com.medikasentosa.features.laboratorium.dto.LabHasilResponse;
import com.medikasentosa.features.laboratorium.entity.LabHasil;
import com.medikasentosa.features.laboratorium.entity.LabOrder;
import com.medikasentosa.features.laboratorium.entity.StatusLab;
import com.medikasentosa.features.laboratorium.repository.LabHasilRepository;
import com.medikasentosa.features.laboratorium.repository.LabOrderRepository;
import com.medikasentosa.features.pasien.entity.JenisKelamin;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.rekammedis.entity.RekamMedis;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabHasilServiceTest {

    @Mock
    private LabHasilRepository labHasilRepository;
    @Mock
    private LabOrderRepository labOrderRepository;

    @InjectMocks
    private LabHasilService labHasilService;

    private LabOrder labOrder;
    private LabHasil labHasil;
    private LabHasilRequest request;

    @BeforeEach
    void setUp() {
        User userPasien = User.builder().id(15L).nama("Lina Marlina").email("lina@email.com")
                .password("hashed").role(Role.PASIEN).isActive(true).build();
        Pasien pasien = Pasien.builder().id(12L).user(userPasien).noRekamMedis("RM-2024-000012")
                .nik("3275012312020012").tanggalLahir(LocalDate.of(1989, 2, 14))
                .jenisKelamin(JenisKelamin.P).alamat("Jl. Kamboja No. 3, Jakarta")
                .noHp("081345567788").golDarah("B").build();
        RekamMedis rekamMedis = RekamMedis.builder().id(5L).pasien(pasien)
                .diagnosa("Anemia").tindakan("Cek darah lengkap").build();

        labOrder = LabOrder.builder().id(1L).rekamMedis(rekamMedis).jenisPemeriksaan("Gula Darah Puasa")
                .catatan("Ambil sampel pagi").status(StatusLab.MENUNGGU).build();

        labHasil = LabHasil.builder().id(1L).labOrder(labOrder)
                .hasilText("Gula darah: 95 mg/dL").nilaiNormal("70-100 mg/dL")
                .interpretasi("Hasil dalam batas normal").build();

        request = new LabHasilRequest(1L, "Gula darah: 95 mg/dL", "70-100 mg/dL", "Hasil dalam batas normal");
    }

    @Test
    void create_success_autoClosesOrder() {
        when(labOrderRepository.findById(1L)).thenReturn(Optional.of(labOrder));
        when(labHasilRepository.existsByLabOrderId(1L)).thenReturn(false);
        when(labHasilRepository.save(any(LabHasil.class))).thenAnswer(inv -> {
            LabHasil h = inv.getArgument(0);
            h.setId(1L);
            return h;
        });

        LabHasilResponse response = labHasilService.create(request);

        assertThat(response.hasilText()).isEqualTo("Gula darah: 95 mg/dL");
        assertThat(labOrder.getStatus()).isEqualTo(StatusLab.SELESAI);
        verify(labOrderRepository).save(labOrder);
        verify(labHasilRepository).save(any(LabHasil.class));
    }

    @Test
    void create_fails_orderAlreadySelesai() {
        labOrder.setStatus(StatusLab.SELESAI);
        when(labOrderRepository.findById(1L)).thenReturn(Optional.of(labOrder));
        when(labHasilRepository.existsByLabOrderId(1L)).thenReturn(true);

        assertThatThrownBy(() -> labHasilService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Order laboratorium ini sudah memiliki hasil");

        verify(labHasilRepository, never()).save(any());
    }

    @Test
    void create_fails_orderNotFound() {
        when(labOrderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> labHasilService.create(new LabHasilRequest(999L, "Hasil", "Normal", null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order laboratorium tidak ditemukan");

        verify(labHasilRepository, never()).save(any());
    }

    @Test
    void getByLabOrder_returnsHasil() {
        when(labHasilRepository.findByLabOrderId(1L)).thenReturn(Optional.of(labHasil));

        LabHasilResponse response = labHasilService.getByLabOrder(1L);

        assertThat(response.nilaiNormal()).isEqualTo("70-100 mg/dL");
        assertThat(response.interpretasi()).isEqualTo("Hasil dalam batas normal");
    }
}
