package com.medikasentosa.features.poli.service;

import com.medikasentosa.features.poli.dto.PoliRequest;
import com.medikasentosa.features.poli.dto.PoliResponse;
import com.medikasentosa.features.poli.entity.Poli;
import com.medikasentosa.features.poli.repository.PoliRepository;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PoliServiceTest {

    @Mock PoliRepository poliRepository;
    @InjectMocks PoliService poliService;

    private PoliRequest request;
    private Poli poli;

    @BeforeEach
    void setUp() {
        request = new PoliRequest("Poli Jantung", "Spesialis jantung");
        poli = Poli.builder().id(1L).nama("Poli Umum").deskripsi("Umum").isActive(true).build();
    }

    @Test
    void create_shouldReturnResponse_whenNameUnique() {
        when(poliRepository.existsByNamaIgnoreCase("Poli Jantung")).thenReturn(false);
        when(poliRepository.save(any())).thenAnswer(inv -> { Poli p = inv.getArgument(0); p.setId(1L); return p; });

        PoliResponse r = poliService.create(request);
        assertThat(r.nama()).isEqualTo("Poli Jantung");
        assertThat(r.isActive()).isTrue();
    }

    @Test
    void create_shouldThrow_whenNameExists() {
        when(poliRepository.existsByNamaIgnoreCase("Poli Jantung")).thenReturn(true);
        assertThatThrownBy(() -> poliService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("sudah ada");
    }

    @Test
    void getAll_shouldReturnAllPoli() {
        when(poliRepository.findAll()).thenReturn(List.of(poli));
        List<PoliResponse> list = poliService.getAll();
        assertThat(list).hasSize(1);
        assertThat(list.get(0).nama()).isEqualTo("Poli Umum");
    }

    @Test
    void getById_shouldReturn_whenFound() {
        when(poliRepository.findById(1L)).thenReturn(Optional.of(poli));
        assertThat(poliService.getById(1L).nama()).isEqualTo("Poli Umum");
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(poliRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> poliService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldUpdateFields() {
        when(poliRepository.findById(1L)).thenReturn(Optional.of(poli));
        PoliResponse r = poliService.update(1L, new PoliRequest("Updated", "Desc baru"));
        assertThat(r.nama()).isEqualTo("Updated");
        assertThat(r.deskripsi()).isEqualTo("Desc baru");
        verify(poliRepository).save(poli);
    }

    @Test
    void deactivate_shouldSetInactive_andSave() {
        when(poliRepository.findById(1L)).thenReturn(Optional.of(poli));
        poliService.deactivate(1L);
        assertThat(poli.getIsActive()).isFalse();
        verify(poliRepository).save(poli);
    }
}
