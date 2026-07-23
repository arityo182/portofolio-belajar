package com.medikasentosa.features.obat.service;

import com.medikasentosa.features.obat.dto.ObatRequest;
import com.medikasentosa.features.obat.dto.ObatResponse;
import com.medikasentosa.features.obat.entity.Obat;
import com.medikasentosa.features.obat.repository.ObatRepository;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObatServiceTest {

    @Mock
    private ObatRepository obatRepository;

    @InjectMocks
    private ObatService obatService;

    private Obat obat;
    private ObatRequest request;

    @BeforeEach
    void setUp() {
        obat = Obat.builder()
                .id(1L)
                .nama("Amoxicillin 500mg")
                .kategori("Antibiotik")
                .satuan("kapsul")
                .stok(200)
                .harga(BigDecimal.valueOf(15000))
                .deskripsi("Antibiotik spektrum luas")
                .isActive(true)
                .build();

        request = new ObatRequest("Amoxicillin 500mg", "Antibiotik", "kapsul", 200,
                BigDecimal.valueOf(15000), "Antibiotik spektrum luas", null, null);
    }

    @Test
    void create_success_returnsObatResponse() {
        when(obatRepository.existsByNamaIgnoreCase("Amoxicillin 500mg")).thenReturn(false);
        when(obatRepository.save(any(Obat.class))).thenAnswer(inv -> {
            Obat o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        ObatResponse response = obatService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nama()).isEqualTo("Amoxicillin 500mg");
        assertThat(response.stok()).isEqualTo(200);
        verify(obatRepository).save(any(Obat.class));
    }

    @Test
    void create_fails_duplicateNama() {
        when(obatRepository.existsByNamaIgnoreCase("Amoxicillin 500mg")).thenReturn(true);

        assertThatThrownBy(() -> obatService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Obat dengan nama ini sudah ada");

        verify(obatRepository, never()).save(any());
    }

    @Test
    void getAll_returnsAllObat() {
        when(obatRepository.findAll()).thenReturn(List.of(obat));

        List<ObatResponse> result = obatService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).kategori()).isEqualTo("Antibiotik");
    }

    @Test
    void getById_returnsObat() {
        when(obatRepository.findById(1L)).thenReturn(Optional.of(obat));

        ObatResponse response = obatService.getById(1L);

        assertThat(response.harga()).isEqualTo(BigDecimal.valueOf(15000));
        assertThat(response.satuan()).isEqualTo("kapsul");
    }

    @Test
    void update_success_updatesObat() {
        ObatRequest updateReq = new ObatRequest("Amoxicillin 500mg", "Antibiotik", "tablet", 150,
                BigDecimal.valueOf(18000), "Updated desc", null, null);
        when(obatRepository.findById(1L)).thenReturn(Optional.of(obat));

        ObatResponse response = obatService.update(1L, updateReq);

        assertThat(response.satuan()).isEqualTo("tablet");
        assertThat(response.stok()).isEqualTo(150);
        verify(obatRepository).save(obat);
    }

    @Test
    void deactivate_setsIsActiveFalse() {
        when(obatRepository.findById(1L)).thenReturn(Optional.of(obat));

        obatService.deactivate(1L);

        assertThat(obat.getIsActive()).isFalse();
        verify(obatRepository).save(obat);
    }

    @Test
    void kurangiStok_success_reducesStok() {
        when(obatRepository.findById(1L)).thenReturn(Optional.of(obat));

        ObatResponse response = obatService.kurangiStok(1L, 30);

        assertThat(response.stok()).isEqualTo(170);
        verify(obatRepository).save(obat);
    }

    @Test
    void kurangiStok_fails_insufficient() {
        when(obatRepository.findById(1L)).thenReturn(Optional.of(obat));

        assertThatThrownBy(() -> obatService.kurangiStok(1L, 500))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Stok tidak mencukupi (200 tersedia)");

        verify(obatRepository, never()).save(any());
    }
}
