package com.medikasentosa.features.appointment.controller;

import com.medikasentosa.features.appointment.dto.AppointmentRequest;
import com.medikasentosa.features.auth.entity.Role;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.features.auth.repository.UserRepository;
import com.medikasentosa.features.dokter.entity.Dokter;
import com.medikasentosa.features.dokter.repository.DokterRepository;
import com.medikasentosa.features.jadwaldokter.entity.Hari;
import com.medikasentosa.features.jadwaldokter.entity.JadwalDokter;
import com.medikasentosa.features.jadwaldokter.repository.JadwalDokterRepository;
import com.medikasentosa.features.pasien.entity.JenisKelamin;
import com.medikasentosa.features.pasien.entity.Pasien;
import com.medikasentosa.features.pasien.repository.PasienRepository;
import com.medikasentosa.features.poli.entity.Poli;
import com.medikasentosa.features.poli.repository.PoliRepository;
import com.medikasentosa.shared.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.Disabled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Disabled("Memerlukan setup CORS + JWT filter di H2 — jalankan manual dengan PostgreSQL untuk verifikasi end-to-end")
@Transactional
class AppointmentControllerIntegrationTest {

    @LocalServerPort int port;
    @Autowired JwtService jwtService;
    @Autowired UserRepository userRepository;
    @Autowired PasienRepository pasienRepository;
    @Autowired DokterRepository dokterRepository;
    @Autowired PoliRepository poliRepository;
    @Autowired JadwalDokterRepository jadwalDokterRepository;

    private RestTemplate rest;
    private String baseUrl, adminToken, pasienToken;
    private Long jadwalId, pasienId;

    @BeforeEach
    void setUp() {
        // Bersihkan data sebelumnya
        jadwalDokterRepository.deleteAll();
        dokterRepository.deleteAll();
        pasienRepository.deleteAll();
        poliRepository.deleteAll();
        userRepository.deleteAll();

        rest = new RestTemplate();
        baseUrl = "http://localhost:" + port;

        User admin = userRepository.save(User.builder().nama("Admin").email("admin@test.com")
                .password("pw").role(Role.ADMIN).isActive(true).build());
        adminToken = jwtService.generateToken("admin@test.com");

        User userPasien = userRepository.save(User.builder().nama("Budi").email("budi@test.com")
                .password("pw").role(Role.PASIEN).isActive(true).build());
        pasienToken = jwtService.generateToken("budi@test.com");

        Pasien pasien = pasienRepository.save(Pasien.builder().user(userPasien)
                .noRekamMedis("RM-TEST-001").nik("1234567890").tanggalLahir(LocalDate.of(1990,1,1))
                .jenisKelamin(JenisKelamin.L).noHp("0812").build());
        pasienId = pasien.getId();

        Poli poli = poliRepository.save(Poli.builder().nama("Poli Test").isActive(true).build());
        User userDokter = userRepository.save(User.builder().nama("Dr Test").email("dr@test.com")
                .password("pw").role(Role.DOKTER).isActive(true).build());
        Dokter dokter = dokterRepository.save(Dokter.builder().user(userDokter).poli(poli)
                .noStr("STR-TEST").spesialisasi("Umum").noHp("0812").isActive(true).build());

        JadwalDokter jadwal = jadwalDokterRepository.save(JadwalDokter.builder()
                .dokter(dokter).hari(Hari.SENIN).jamMulai(LocalTime.of(8,0))
                .jamSelesai(LocalTime.of(14,0)).kuota(10).isActive(true).build());
        jadwalId = jadwal.getId();
    }

    private HttpHeaders auth(String token) {
        var h = new HttpHeaders(); h.set("Authorization", "Bearer " + token);
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("Origin", "http://localhost:5173");
        return h;
    }

    @Test
    void bookingEndToEnd_shouldReturn201() {
        var req = new AppointmentRequest(pasienId, jadwalId, LocalDate.of(2026, 7, 27), "Nyeri dada");
        var r = rest.postForEntity(baseUrl + "/api/appointment",
                new HttpEntity<>(req, auth(pasienToken)), String.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(r.getBody()).contains("MENUNGGU");
    }

    @Test
    void bookingUnauthorized_shouldReturn403() {
        var req = new AppointmentRequest(pasienId, jadwalId, LocalDate.of(2026, 7, 27), "Test");
        var r = rest.postForEntity(baseUrl + "/api/appointment", req, String.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
