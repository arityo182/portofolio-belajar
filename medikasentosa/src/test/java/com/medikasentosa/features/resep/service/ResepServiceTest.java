package com.medikasentosa.features.resep.service;

import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.obat.entity.Obat;
import com.medikasentosa.features.obat.repository.ObatRepository;
import com.medikasentosa.features.pasien.entity.JenisKelamin;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.poli.entity.Poli;
import com.medikasentosa.features.rekammedis.entity.RekamMedis;
import com.medikasentosa.features.rekammedis.repository.RekamMedisRepository;
import com.medikasentosa.features.resep.dto.ResepRequest;
import com.medikasentosa.features.resep.dto.ResepResponse;
import com.medikasentosa.features.resep.entity.Resep;
import com.medikasentosa.features.resep.repository.ResepRepository;
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
class ResepServiceTest {

    @Mock
    private ResepRepository resepRepository;
    @Mock
    private RekamMedisRepository rekamMedisRepository;
    @Mock
    private ObatRepository obatRepository;

    @InjectMocks
    private ResepService resepService;

    private RekamMedis rekamMedis;
    private Obat obat;
    private Resep resep;

    @BeforeEach
    void setUp() {
        User userPasien = User.builder().id(6L).nama("Siti Nurhaliza").email("sitin@email.com")
                .password("hashed").role(Role.PASIEN).isActive(true).build();
        User userDokter = User.builder().id(5L).nama("dr. Andi Pratama").email("andi.dokter@medika.com")
                .password("hashed").role(Role.DOKTER).isActive(true).build();
        Poli poli = Poli.builder().id(6L).nama("Poli Kulit").deskripsi("Spesialis Kulit").isActive(true).build();
        Pasien pasien = Pasien.builder().id(4L).user(userPasien).noRekamMedis("RM-2024-000004")
                .nik("3201015006900004").tanggalLahir(LocalDate.of(1994, 3, 12))
                .jenisKelamin(JenisKelamin.P).alamat("Jl. Cempaka No. 4, Medan")
                .noHp("081255667788").golDarah("B").build();
        Dokter dokter = Dokter.builder().id(13L).user(userDokter).poli(poli)
                .noStr("STR-990011223").spesialisasi("Spesialis Kulit").noHp("082288776655").isActive(true).build();

        rekamMedis = RekamMedis.builder().id(2L).pasien(pasien).dokter(dokter)
                .diagnosa("Dermatitis Atopik").tindakan("Terapi topikal").catatan("Pakai krim 2x sehari").build();

        obat = Obat.builder().id(1L).nama("Hydrocortisone Cream 1%").kategori("Kortikosteroid Topikal")
                .satuan("tube").stok(50).harga(BigDecimal.valueOf(45000))
                .deskripsi("Krim anti inflamasi").isActive(true).build();

        resep = Resep.builder().id(1L).rekamMedis(rekamMedis).obat(obat).dosis("Oles tipis")
                .jumlah(2).aturanPakai("2x sehari pagi dan malam").build();
    }

    @Test
    void create_withObatId_reducesStock() {
        ResepRequest req = new ResepRequest(2L, 1L, null, "Oles tipis", 2, "2x sehari");
        when(rekamMedisRepository.findById(2L)).thenReturn(Optional.of(rekamMedis));
        when(obatRepository.findById(1L)).thenReturn(Optional.of(obat));
        when(resepRepository.save(any(Resep.class))).thenAnswer(inv -> {
            Resep r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        ResepResponse response = resepService.create(req);

        assertThat(response.obatId()).isEqualTo(1L);
        assertThat(obat.getStok()).isEqualTo(48);
        verify(obatRepository).save(obat);
        verify(resepRepository).save(any(Resep.class));
    }

    @Test
    void create_withFreeText_usesNamaObat() {
        ResepRequest req = new ResepRequest(2L, null, "Paracetamol 500mg", "1 tablet", 10, "3x sehari");
        when(rekamMedisRepository.findById(2L)).thenReturn(Optional.of(rekamMedis));
        when(resepRepository.save(any(Resep.class))).thenAnswer(inv -> {
            Resep r = inv.getArgument(0);
            r.setId(2L);
            return r;
        });

        ResepResponse response = resepService.create(req);

        assertThat(response.namaObat()).isEqualTo("Paracetamol 500mg");
        assertThat(response.jumlah()).isEqualTo(10);
        verify(obatRepository, never()).save(any());
    }

    @Test
    void create_fails_rekamMedisNotFound() {
        ResepRequest req = new ResepRequest(999L, 1L, null, "2x1", 5, "setelah makan");
        when(rekamMedisRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resepService.create(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Rekam medis tidak ditemukan");

        verify(resepRepository, never()).save(any());
    }

    @Test
    void getByRekamMedis_returnsResepList() {
        when(resepRepository.findByRekamMedisId(2L)).thenReturn(List.of(resep));

        List<ResepResponse> result = resepService.getByRekamMedis(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).namaObat()).isEqualTo("Hydrocortisone Cream 1%");
    }

    @Test
    void delete_removesResep() {
        when(resepRepository.findById(1L)).thenReturn(Optional.of(resep));

        resepService.delete(1L);

        verify(resepRepository).delete(resep);
    }
}
