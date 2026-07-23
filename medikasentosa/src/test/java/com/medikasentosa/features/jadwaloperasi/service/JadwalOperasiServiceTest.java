package com.medikasentosa.features.jadwaloperasi.service;

import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.dokter.repository.DokterRepository;
import com.medikasentosa.features.jadwaloperasi.dto.JadwalOperasiRequest;
import com.medikasentosa.features.jadwaloperasi.dto.JadwalOperasiResponse;
import com.medikasentosa.features.jadwaloperasi.entity.JadwalOperasi;
import com.medikasentosa.features.jadwaloperasi.entity.StatusOperasi;
import com.medikasentosa.features.jadwaloperasi.repository.JadwalOperasiRepository;
import com.medikasentosa.features.pasien.entity.JenisKelamin;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.pasien.repository.PasienRepository;
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
class JadwalOperasiServiceTest {

    @Mock
    private JadwalOperasiRepository repo;
    @Mock
    private PasienRepository pasienRepo;
    @Mock
    private DokterRepository dokterRepo;

    @InjectMocks
    private JadwalOperasiService jadwalOperasiService;

    private Pasien pasien;
    private Dokter dokter;
    private JadwalOperasi jadwalOperasi;
    private JadwalOperasiRequest request;

    @BeforeEach
    void setUp() {
        User userPasien = User.builder().id(8L).nama("Ratna Dewi").email("ratna@email.com")
                .password("hashed").role(Role.PASIEN).isActive(true).build();
        User userDokter = User.builder().id(7L).nama("dr. Sutrisno").email("sutrisno.dokter@medika.com")
                .password("hashed").role(Role.DOKTER).isActive(true).build();
        Poli poli = Poli.builder().id(8L).nama("Poli Bedah").deskripsi("Spesialis Bedah").isActive(true).build();

        pasien = Pasien.builder().id(6L).user(userPasien).noRekamMedis("RM-2024-000006")
                .nik("3275012311980006").tanggalLahir(LocalDate.of(1985, 9, 17))
                .jenisKelamin(JenisKelamin.P).alamat("Jl. Cendana No. 8, Semarang")
                .noHp("081356677889").golDarah("A").build();

        dokter = Dokter.builder().id(15L).user(userDokter).poli(poli)
                .noStr("STR-445566778").spesialisasi("Spesialis Bedah Umum").noHp("082277889900").isActive(true).build();

        jadwalOperasi = JadwalOperasi.builder().id(1L).pasien(pasien).dokter(dokter)
                .tanggalOperasi(LocalDate.now().plusDays(7)).jamMulai(LocalTime.of(8, 0))
                .jenisOperasi("Appendektomi").ruangOperasi("OK-1")
                .catatan("Pasien puasa 8 jam").status(StatusOperasi.TERJADWAL).build();

        request = new JadwalOperasiRequest(6L, 15L, LocalDate.now().plusDays(7),
                LocalTime.of(8, 0), "Appendektomi", "OK-1", "Pasien puasa 8 jam");
    }

    @Test
    void create_success_returnsJadwalOperasiResponse() {
        when(pasienRepo.findById(6L)).thenReturn(Optional.of(pasien));
        when(dokterRepo.findById(15L)).thenReturn(Optional.of(dokter));
        when(repo.save(any(JadwalOperasi.class))).thenAnswer(inv -> {
            JadwalOperasi j = inv.getArgument(0);
            j.setId(1L);
            return j;
        });

        JadwalOperasiResponse response = jadwalOperasiService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.jenisOperasi()).isEqualTo("Appendektomi");
        assertThat(response.status()).isEqualTo(StatusOperasi.TERJADWAL);
        verify(repo).save(any(JadwalOperasi.class));
    }

    @Test
    void create_fails_pasienNotFound() {
        when(pasienRepo.findById(6L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jadwalOperasiService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pasien tidak ditemukan");

        verify(repo, never()).save(any());
    }

    @Test
    void getAll_returnsAllJadwalOperasi() {
        when(repo.findAll()).thenReturn(List.of(jadwalOperasi));

        List<JadwalOperasiResponse> result = jadwalOperasiService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).ruangOperasi()).isEqualTo("OK-1");
    }

    @Test
    void getByPasien_returnsJadwalList() {
        when(repo.findByPasienId(6L)).thenReturn(List.of(jadwalOperasi));

        List<JadwalOperasiResponse> result = jadwalOperasiService.getByPasien(6L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).namaPasien()).isEqualTo("Ratna Dewi");
    }

    @Test
    void updateStatus_updatesStatus() {
        when(repo.findById(1L)).thenReturn(Optional.of(jadwalOperasi));

        JadwalOperasiResponse response = jadwalOperasiService.updateStatus(1L, StatusOperasi.BERLANGSUNG);

        assertThat(response.status()).isEqualTo(StatusOperasi.BERLANGSUNG);
        verify(repo).save(jadwalOperasi);
    }
}
