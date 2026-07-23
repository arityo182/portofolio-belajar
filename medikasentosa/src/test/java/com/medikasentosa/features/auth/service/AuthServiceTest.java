package com.medikasentosa.features.auth.service;

import com.medikasentosa.features.auth.dto.AuthResponse;
import com.medikasentosa.features.auth.dto.LoginRequest;
import com.medikasentosa.features.auth.dto.RegisterRequest;
import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.auth.repository.UserRepository;
import com.medikasentosa.shared.exception.DuplicateResourceException;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import com.medikasentosa.shared.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .nama("Ari Hidayat")
                .email("ari@email.com")
                .password("hashed_pass")
                .role(Role.PASIEN)
                .isActive(true)
                .build();

        registerRequest = new RegisterRequest("Ari Hidayat", "ari@email.com", "arie12345");
        loginRequest = new LoginRequest("ari@email.com", "arie12345");
    }

    @Test
    void register_success_hashesPasswordAndReturnsToken() {
        when(userRepository.existsByEmail("ari@email.com")).thenReturn(false);
        when(passwordEncoder.encode("arie12345")).thenReturn("hashed_pass");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(anyString())).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.token()).isEqualTo("mock-jwt-token");
        assertThat(response.user().email()).isEqualTo("ari@email.com");
        assertThat(response.user().role()).isEqualTo(Role.PASIEN);
        verify(passwordEncoder).encode("arie12345");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_fails_duplicateEmail() {
        when(userRepository.existsByEmail("ari@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email sudah terdaftar");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success_returnsToken() {
        when(userRepository.findByEmail("ari@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("arie12345", "hashed_pass")).thenReturn(true);
        when(jwtService.generateToken(anyString())).thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.token()).isEqualTo("mock-jwt-token");
        assertThat(response.user().nama()).isEqualTo("Ari Hidayat");
    }

    @Test
    void login_fails_wrongPassword() {
        when(userRepository.findByEmail("ari@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("arie12345", "hashed_pass")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Email atau password salah");
    }

    @Test
    void login_fails_inactiveAccount() {
        user.setIsActive(false);
        when(userRepository.findByEmail("ari@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("arie12345", "hashed_pass")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Akun Anda dinonaktifkan. Hubungi admin.");
    }
}
