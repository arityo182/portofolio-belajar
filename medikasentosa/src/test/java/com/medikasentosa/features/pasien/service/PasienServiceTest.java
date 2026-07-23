package com.medikasentosa.features.pasien.service;

import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.auth.repository.UserRepository;
import com.medikasentosa.features.pasien.dto.PasienRequest;
import com.medikasentosa.features.pasien.dto.PasienResponse;
import com.medikasentosa.features.pasien.entity.JenisKelamin;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.pasien.repository.PasienRepository;
import com.medikasentosa.shared.exception.DuplicateResourceException;
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
class PasienServiceTest {

    @Mock
    private PasienRepository pasienRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PasienService pasienService;

    private User user;
    private Pasien pasien;
    private PasienRequest request;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(5L)
                .nama("Ahmad Fauzi")
                .email("ahmad.fauzi@email.com")
                .password("hashed")
                .role(Role.PASIEN)
                .isActive(true)
                .build();

        pasien = Pasien.builder()
                .id(1L)
                .user(user)
                .noRekamMedis("RM-2024-000001")
                .nik("3275012304900001")
                .tanggalLahir(LocalDate.of(1990, 4, 23))
                .jenisKelamin(JenisKelamin.L)
                .alamat("Jl. Melati No. 12, Jakarta Selatan")
                .noHp("081298765432")
                .golDarah("O")
                .build();

        request = new PasienRequest(5L, "3275012304900001", LocalDate.of(1990, 4, 23),
                JenisKelamin.L, "Jl. Melati No. 12, Jakarta Selatan", "081298765432", "O");
    }

    @Test
    void create_success_generatesRMAndSetsRole() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(pasienRepository.existsByUserId(5L)).thenReturn(false);
        when(pasienRepository.count()).thenReturn(5L);
        when(pasienRepository.existsByNoRekamMedis(anyString())).thenReturn(false);
        when(pasienRepository.save(any(Pasien.class))).thenAnswer(inv -> {
            Pasien p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        PasienResponse response = pasienService.create(request);

        assertThat(response.noRekamMedis()).contains("RM-");
        assertThat(response.nama()).isEqualTo("Ahmad Fauzi");
        assertThat(response.nik()).isEqualTo("3275012304900001");
        verify(pasienRepository).save(any(Pasien.class));
    }

    @Test
    void create_fails_userNotFound() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pasienService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User tidak ditemukan");

        verify(pasienRepository, never()).save(any());
    }

    @Test
    void create_fails_userAlreadyPasien() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(pasienRepository.existsByUserId(5L)).thenReturn(true);

        assertThatThrownBy(() -> pasienService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("User sudah terdaftar sebagai pasien");

        verify(pasienRepository, never()).save(any());
    }

    @Test
    void getAll_returnsAllPasien() {
        when(pasienRepository.findAll()).thenReturn(List.of(pasien));

        List<PasienResponse> result = pasienService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nama()).isEqualTo("Ahmad Fauzi");
    }

    @Test
    void getById_returnsPasien() {
        when(pasienRepository.findById(1L)).thenReturn(Optional.of(pasien));

        PasienResponse response = pasienService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("ahmad.fauzi@email.com");
    }

    @Test
    void getByUserId_returnsPasien() {
        when(pasienRepository.findByUserId(5L)).thenReturn(Optional.of(pasien));

        PasienResponse response = pasienService.getByUserId(5L);

        assertThat(response.userId()).isEqualTo(5L);
        assertThat(response.golDarah()).isEqualTo("O");
    }

    @Test
    void update_success_updatesProfil() {
        PasienRequest updateReq = new PasienRequest(5L, "3275012304900002", LocalDate.of(1990, 4, 23),
                JenisKelamin.L, "Jl. Anggrek No. 5, Jakarta Pusat", "081212345678", "A");
        when(pasienRepository.findById(1L)).thenReturn(Optional.of(pasien));

        PasienResponse response = pasienService.update(1L, updateReq);

        assertThat(response.alamat()).isEqualTo("Jl. Anggrek No. 5, Jakarta Pusat");
        assertThat(response.noHp()).isEqualTo("081212345678");
        verify(pasienRepository).save(pasien);
    }
}
