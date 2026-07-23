package com.medikasentosa.features.rawatinap.service;

import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.dokter.repository.DokterRepository;
import com.medikasentosa.features.pasien.entity.JenisKelamin;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.pasien.repository.PasienRepository;
import com.medikasentosa.features.poli.entity.Poli;
import com.medikasentosa.features.rawatinap.dto.RawatInapRequest;
import com.medikasentosa.features.rawatinap.dto.RawatInapResponse;
import com.medikasentosa.features.rawatinap.entity.Kamar;
import com.medikasentosa.features.rawatinap.entity.RawatInap;
import com.medikasentosa.features.rawatinap.entity.StatusRawatInap;
import com.medikasentosa.features.rawatinap.entity.TipeKamar;
import com.medikasentosa.features.rawatinap.repository.RawatInapRepository;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RawatInapServiceTest {

    @Mock
    private RawatInapRepository rawatInapRepository;
    @Mock
    private KamarService kamarService;
    @Mock
    private PasienRepository pasienRepository;
    @Mock
    private DokterRepository dokterRepository;

    @InjectMocks
    private RawatInapService rawatInapService;

    private Pasien pasien;
    private Dokter dokter;
    private Kamar kamar;
    private RawatInap rawatInap;
    private RawatInapRequest request;

    @BeforeEach
    void setUp() {
        User userPasien = User.builder().id(7L).nama("Hendra Setiawan").email("hendra@email.com")
                .password("hashed").role(Role.PASIEN).isActive(true).build();
        User userDokter = User.builder().id(6L).nama("dr. Rina Maharani").email("rina.dokter@medika.com")
                .password("hashed").role(Role.DOKTER).isActive(true).build();
        Poli poli = Poli.builder().id(7L).nama("Poli Bedah").deskripsi("Spesialis Bedah").isActive(true).build();

        pasien = Pasien.builder().id(5L).user(userPasien).noRekamMedis("RM-2024-000005")
                .nik("3275012311970005").tanggalLahir(LocalDate.of(1982, 6, 8))
                .jenisKelamin(JenisKelamin.L).alamat("Jl. Bougenville No. 10, Yogyakarta")
                .noHp("081344556677").golDarah("O").build();

        dokter = Dokter.builder().id(14L).user(userDokter).poli(poli)
                .noStr("STR-778899001").spesialisasi("Spesialis Bedah").noHp("081299887766").isActive(true).build();

        kamar = Kamar.builder().id(2L).nomorKamar("ICU-01").tipeKamar(TipeKamar.ICU)
                .kapasitas(4).terisi(2).hargaPerMalam(BigDecimal.valueOf(2500000)).isActive(true).build();

        rawatInap = RawatInap.builder().id(1L).pasien(pasien).dokter(dokter).kamar(kamar)
                .tanggalMasuk(LocalDate.of(2024, 7, 20)).diagnosaAwal("Appendicitis Akut")
                .catatan("Perlu observasi ketat").status(StatusRawatInap.AKTIF).build();

        request = new RawatInapRequest(5L, 14L, 2L, LocalDate.of(2024, 7, 20),
                "Appendicitis Akut", "Perlu observasi ketat");
    }

    @Test
    void masuk_success_incrementsTerisi() {
        when(pasienRepository.findById(5L)).thenReturn(Optional.of(pasien));
        when(dokterRepository.findById(14L)).thenReturn(Optional.of(dokter));
        when(kamarService.findOrThrow(2L)).thenReturn(kamar);
        when(rawatInapRepository.save(any(RawatInap.class))).thenAnswer(inv -> {
            RawatInap r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        RawatInapResponse response = rawatInapService.masuk(request);

        assertThat(response.status()).isEqualTo(StatusRawatInap.AKTIF);
        assertThat(response.namaPasien()).isEqualTo("Hendra Setiawan");
        assertThat(kamar.getTerisi()).isEqualTo(3);
        verify(rawatInapRepository).save(any(RawatInap.class));
    }

    @Test
    void masuk_fails_kamarPenuh() {
        kamar.setTerisi(4);
        when(pasienRepository.findById(5L)).thenReturn(Optional.of(pasien));
        when(dokterRepository.findById(14L)).thenReturn(Optional.of(dokter));
        when(kamarService.findOrThrow(2L)).thenReturn(kamar);

        assertThatThrownBy(() -> rawatInapService.masuk(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Kamar ICU-01 sudah penuh (4/4)");

        verify(rawatInapRepository, never()).save(any());
    }

    @Test
    void keluar_success_setsSelesaiAndDecrementsTerisi() {
        when(rawatInapRepository.findById(1L)).thenReturn(Optional.of(rawatInap));

        RawatInapResponse response = rawatInapService.keluar(1L, LocalDate.of(2024, 7, 25));

        assertThat(response.status()).isEqualTo(StatusRawatInap.SELESAI);
        assertThat(kamar.getTerisi()).isEqualTo(1);
        verify(rawatInapRepository).save(rawatInap);
    }

    @Test
    void getAktif_returnsActiveRawatInap() {
        when(rawatInapRepository.findByStatus(StatusRawatInap.AKTIF)).thenReturn(List.of(rawatInap));

        List<RawatInapResponse> result = rawatInapService.getAktif();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).diagnosaAwal()).isEqualTo("Appendicitis Akut");
    }

    @Test
    void getByPasien_returnsRiwayat() {
        when(rawatInapRepository.findByPasienId(5L)).thenReturn(List.of(rawatInap));

        List<RawatInapResponse> result = rawatInapService.getByPasien(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nomorKamar()).isEqualTo("ICU-01");
    }
}
