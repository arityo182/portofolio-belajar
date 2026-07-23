package com.medikasentosa.features.admin.service;

import com.medikasentosa.features.admin.dto.DokterCreateRequest;
import com.medikasentosa.features.admin.dto.UserAdminResponse;
import com.medikasentosa.features.appointment.repository.AppointmentRepository;
import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.auth.repository.UserRepository;
import com.medikasentosa.features.dokter.dto.DokterResponse;
import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.dokter.repository.DokterRepository;
import com.medikasentosa.features.dokter.service.DokterService;
import com.medikasentosa.features.pasien.service.PasienService;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DokterRepository dokterRepository;
    @Mock
    private PoliRepository poliRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DokterService dokterService;
    @Mock
    private PasienService pasienService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private User user;
    private Dokter dokter;
    private Poli poli;
    private DokterCreateRequest dokterCreateRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(13L)
                .nama("dr. Karim Benzema")
                .email("karim.dokter@medika.com")
                .password("hashed_pass")
                .role(Role.DOKTER)
                .isActive(true)
                .build();

        poli = Poli.builder()
                .id(12L)
                .nama("Poli Ortopedi")
                .deskripsi("Spesialis Ortopedi")
                .isActive(true)
                .build();

        dokter = Dokter.builder()
                .id(20L)
                .user(user)
                .poli(poli)
                .noStr("STR-998877665")
                .spesialisasi("Spesialis Ortopedi")
                .noHp("082233778899")
                .isActive(true)
                .build();

        dokterCreateRequest = new DokterCreateRequest(
                "dr. Karim Benzema", "karim.dokter@medika.com", "password123",
                12L, "STR-998877665", "Spesialis Ortopedi", "082233778899");
    }

    @Test
    void createDokterWithUser_success_createsUserAndDokter() {
        when(userRepository.existsByEmail("karim.dokter@medika.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_pass");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(dokterService.create(any())).thenReturn(new DokterResponse(
                20L, 13L, "dr. Karim Benzema", "karim.dokter@medika.com",
                12L, "Poli Ortopedi", "STR-998877665", "Spesialis Ortopedi", "082233778899"));

        DokterResponse response = adminService.createDokterWithUser(dokterCreateRequest);

        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.nama()).isEqualTo("dr. Karim Benzema");
        verify(userRepository).save(any(User.class));
        verify(dokterService).create(any());
    }

    @Test
    void createDokterWithUser_fails_duplicateEmail() {
        when(userRepository.existsByEmail("karim.dokter@medika.com")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createDokterWithUser(dokterCreateRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email sudah terdaftar");

        verify(userRepository, never()).save(any());
    }

    @Test
    void getAllDokterForAdmin_returnsAllDokter() {
        when(dokterRepository.findAll()).thenReturn(List.of(dokter));

        List<DokterResponse> result = adminService.getAllDokterForAdmin();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).namaPoli()).isEqualTo("Poli Ortopedi");
    }

    @Test
    void getDashboard_returnsStats() {
        when(userRepository.countByRole(Role.PASIEN)).thenReturn(150L);
        when(dokterRepository.count()).thenReturn(25L);
        when(poliRepository.count()).thenReturn(12L);
        when(appointmentRepository.count()).thenReturn(500L);

        var dashboard = adminService.getDashboard();

        assertThat(dashboard.totalPasien()).isEqualTo(150L);
        assertThat(dashboard.totalDokter()).isEqualTo(25L);
        assertThat(dashboard.totalPoli()).isEqualTo(12L);
        assertThat(dashboard.totalAppointment()).isEqualTo(500L);
    }

    @Test
    void getAllUsers_returnsFilteredUsers() {
        when(userRepository.findByRole(Role.DOKTER)).thenReturn(List.of(user));

        List<UserAdminResponse> result = adminService.getAllUsers(Role.DOKTER);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("karim.dokter@medika.com");
    }

    @Test
    void setUserStatus_deactivatesUser() {
        when(userRepository.findById(13L)).thenReturn(Optional.of(user));

        UserAdminResponse response = adminService.setUserStatus(13L, false);

        assertThat(response.isActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_updatesPassword() {
        when(userRepository.findById(13L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass123")).thenReturn("hashed_new");

        UserAdminResponse response = adminService.resetPassword(13L, "newpass123");

        assertThat(response.nama()).isEqualTo("dr. Karim Benzema");
        verify(passwordEncoder).encode("newpass123");
        verify(userRepository).save(user);
    }
}
