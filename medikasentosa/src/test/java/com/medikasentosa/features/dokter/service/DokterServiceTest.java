package com.medikasentosa.features.dokter.service;

import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.auth.repository.UserRepository;
import com.medikasentosa.features.dokter.dto.DokterRequest;
import com.medikasentosa.features.dokter.dto.DokterResponse;
import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.dokter.repository.DokterRepository;
import com.medikasentosa.features.poli.entity.Poli;
import com.medikasentosa.features.poli.repository.PoliRepository;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test untuk {@link DokterService} menggunakan JUnit 5 + Mockito.
 *
 * @author Ari
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DokterServiceTest {

    @Mock
    private DokterRepository dokterRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PoliRepository poliRepository;

    @InjectMocks
    private DokterService dokterService;

    private User user;
    private Poli poli;
    private DokterRequest request;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .nama("dr. Budi Santoso")
                .email("budi.dokter@medika.com")
                .password("hashed")
                .role(Role.PASIEN)
                .build();

        poli = Poli.builder()
                .id(2L)
                .nama("Poli Jantung")
                .deskripsi("Pelayanan spesialis jantung")
                .isActive(true)
                .build();

        request = new DokterRequest(1L, 2L, "STR-123456789", "Spesialis Jantung", "081234567890");
    }

    @Test
    void create_success_setsUserRoleToDokterAndReturnsFlatResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(dokterRepository.existsByUserId(1L)).thenReturn(false);
        when(poliRepository.findById(2L)).thenReturn(Optional.of(poli));
        when(dokterRepository.save(any(Dokter.class))).thenAnswer(inv -> {
            Dokter d = inv.getArgument(0);
            d.setId(10L);
            return d;
        });

        DokterResponse response = dokterService.create(request);

        // role user berubah menjadi DOKTER lalu disimpan
        assertThat(user.getRole()).isEqualTo(Role.DOKTER);
        verify(userRepository).save(user);

        // dokter yang disimpan aktif
        ArgumentCaptor<Dokter> captor = ArgumentCaptor.forClass(Dokter.class);
        verify(dokterRepository).save(captor.capture());
        assertThat(captor.getValue().getIsActive()).isTrue();

        // response flat
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.nama()).isEqualTo("dr. Budi Santoso");
        assertThat(response.email()).isEqualTo("budi.dokter@medika.com");
        assertThat(response.poliId()).isEqualTo(2L);
        assertThat(response.namaPoli()).isEqualTo("Poli Jantung");
        assertThat(response.noStr()).isEqualTo("STR-123456789");
        assertThat(response.spesialisasi()).isEqualTo("Spesialis Jantung");
        assertThat(response.noHp()).isEqualTo("081234567890");
    }

    @Test
    void create_fails_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dokterService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User tidak ditemukan");

        verify(dokterRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_fails_whenUserAlreadyDokter() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(dokterRepository.existsByUserId(1L)).thenReturn(true);

        assertThatThrownBy(() -> dokterService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("User ini sudah terdaftar sebagai dokter");

        verify(dokterRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_fails_whenPoliNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(dokterRepository.existsByUserId(1L)).thenReturn(false);
        when(poliRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dokterService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Poli tidak ditemukan");

        verify(dokterRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_fails_whenDokterNotFound() {
        when(dokterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dokterService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Dokter tidak ditemukan");
    }

    @Test
    void delete_softDeletes_setsIsActiveFalseAndSaves() {
        Dokter dokter = Dokter.builder()
                .id(10L)
                .user(user)
                .poli(poli)
                .noStr("STR-123456789")
                .spesialisasi("Spesialis Jantung")
                .noHp("081234567890")
                .isActive(true)
                .build();
        when(dokterRepository.findById(10L)).thenReturn(Optional.of(dokter));

        dokterService.delete(10L);

        assertThat(dokter.getIsActive()).isFalse();
        verify(dokterRepository).save(dokter);
        verify(dokterRepository, never()).delete(any());
    }
}
