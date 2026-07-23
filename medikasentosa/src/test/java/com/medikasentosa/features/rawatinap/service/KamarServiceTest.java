package com.medikasentosa.features.rawatinap.service;

import com.medikasentosa.features.rawatinap.dto.KamarRequest;
import com.medikasentosa.features.rawatinap.dto.KamarResponse;
import com.medikasentosa.features.rawatinap.entity.Kamar;
import com.medikasentosa.features.rawatinap.entity.TipeKamar;
import com.medikasentosa.features.rawatinap.repository.KamarRepository;
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
class KamarServiceTest {

    @Mock
    private KamarRepository kamarRepository;

    @InjectMocks
    private KamarService kamarService;

    private Kamar kamar;
    private KamarRequest request;

    @BeforeEach
    void setUp() {
        kamar = Kamar.builder()
                .id(1L)
                .nomorKamar("VIP-101")
                .tipeKamar(TipeKamar.VIP)
                .kapasitas(2)
                .terisi(0)
                .hargaPerMalam(BigDecimal.valueOf(750000))
                .isActive(true)
                .build();

        request = new KamarRequest("VIP-101", TipeKamar.VIP, 2, BigDecimal.valueOf(750000));
    }

    @Test
    void create_success_returnsKamarResponse() {
        when(kamarRepository.existsByNomorKamarIgnoreCase("VIP-101")).thenReturn(false);
        when(kamarRepository.save(any(Kamar.class))).thenAnswer(inv -> {
            Kamar k = inv.getArgument(0);
            k.setId(1L);
            return k;
        });

        KamarResponse response = kamarService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nomorKamar()).isEqualTo("VIP-101");
        assertThat(response.kapasitas()).isEqualTo(2);
        assertThat(response.terisi()).isEqualTo(0);
        verify(kamarRepository).save(any(Kamar.class));
    }

    @Test
    void create_fails_duplicateNomorKamar() {
        when(kamarRepository.existsByNomorKamarIgnoreCase("VIP-101")).thenReturn(true);

        assertThatThrownBy(() -> kamarService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Nomor kamar sudah ada");

        verify(kamarRepository, never()).save(any());
    }

    @Test
    void getAll_returnsAllKamar() {
        when(kamarRepository.findAll()).thenReturn(List.of(kamar));

        List<KamarResponse> result = kamarService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).tipeKamar()).isEqualTo(TipeKamar.VIP);
    }

    @Test
    void getAvailable_returnsAvailableKamar() {
        when(kamarRepository.findAvailableKamar()).thenReturn(List.of(kamar));

        List<KamarResponse> result = kamarService.getAvailable();

        assertThat(result).hasSize(1);
    }

    @Test
    void update_success_updatesKamar() {
        KamarRequest updateReq = new KamarRequest("VIP-102", TipeKamar.VVIP, 3, BigDecimal.valueOf(1200000));
        when(kamarRepository.findById(1L)).thenReturn(Optional.of(kamar));
        when(kamarRepository.existsByNomorKamarIgnoreCase("VIP-102")).thenReturn(false);

        KamarResponse response = kamarService.update(1L, updateReq);

        assertThat(response.nomorKamar()).isEqualTo("VIP-102");
        assertThat(response.tipeKamar()).isEqualTo(TipeKamar.VVIP);
        verify(kamarRepository).save(kamar);
    }

    @Test
    void deactivate_setsIsActiveFalse() {
        when(kamarRepository.findById(1L)).thenReturn(Optional.of(kamar));

        kamarService.deactivate(1L);

        assertThat(kamar.getIsActive()).isFalse();
        verify(kamarRepository).save(kamar);
    }

    @Test
    void deactivate_fails_kamarNotFound() {
        when(kamarRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kamarService.deactivate(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Kamar tidak ditemukan");
    }
}
