package com.medikasentosa.features.screening.service;

import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.auth.repository.UserRepository;
import com.medikasentosa.features.screening.entity.Screening;
import com.medikasentosa.features.screening.repository.ScreeningRepository;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScreeningServiceTest {

    @Mock ScreeningRepository screeningRepository;
    @Mock UserRepository userRepository;
    MeterRegistry meterRegistry = new SimpleMeterRegistry();
    ScreeningService screeningService;

    @BeforeEach
    void setUp() {
        screeningService = new ScreeningService(null, screeningRepository, userRepository, meterRegistry);
    }

    @Test
    void getHistory_shouldReturnList() {
        User user = User.builder().id(1L).email("test@test.com").build();
        Screening s = Screening.builder().id(1L).user(user).label("Normal").confidence(95.0).build();
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(screeningRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(s));

        var history = screeningService.getHistory("test@test.com");
        assertThat(history).hasSize(1);
        assertThat(history.get(0).label()).isEqualTo("Normal");
    }

    @Test
    void getHistory_shouldThrow_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> screeningService.getHistory("unknown@test.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
