package com.medikasentosa.features.screening.service;

import com.medikasentosa.features.screening.dto.FastApiResponse;
import com.medikasentosa.features.screening.dto.ScreeningResponse;
import com.medikasentosa.features.screening.entity.Screening;
import com.medikasentosa.features.auth.entity.User;
import com.medikasentosa.shared.exception.ResourceNotFoundException;
import com.medikasentosa.features.screening.repository.ScreeningRepository;
import com.medikasentosa.features.auth.repository.UserRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service untuk logika bisnis screening osteoporosis.
 * Menyiapkan citra X-ray, memanggil model machine learning (FastAPI),
 * menyimpan hasil prediksi, dan menyediakan riwayat screening pengguna.
 *
 * @author Ari
 * @since 1.0.0
 */
@Service
public class ScreeningService {

    private final RestTemplate restTemplate;
    private final ScreeningRepository screeningRepository;
    private final UserRepository userRepository;
    private final Counter screeningCounter;
    private final Timer screeningLatencyTimer;

    @Value("${app.fastapi.url}")
    private String fastApiUrl;

    public ScreeningService(RestTemplate restTemplate,
            ScreeningRepository screeningRepository,
            UserRepository userRepository,
            MeterRegistry meterRegistry) {
        this.restTemplate = restTemplate;
        this.screeningRepository = screeningRepository;
        this.userRepository = userRepository;
        this.screeningCounter = Counter.builder("screening.requests.total")
                .description("Total screening requests")
                .register(meterRegistry);
        this.screeningLatencyTimer = Timer.builder("screening.latency")
                .description("Latency screening ke FastAPI")
                .register(meterRegistry);
    }

    /**
     * Melakukan screening: mengirim citra X-ray ke model ML (FastAPI),
     * lalu menyimpan hasil prediksi ke database dan mengaitkannya dengan pengguna.
     *
     * @param file  berkas citra X-ray (multipart) yang akan diprediksi
     * @param email email pengguna (diambil dari token JWT) sebagai pemilik hasil screening
     * @return hasil screening (label, confidence, risiko, Grad-CAM, latensi)
     * @throws IOException               bila berkas gagal dibaca
     * @throws ResourceNotFoundException bila user dengan email tersebut tidak ditemukan
     */
    public ScreeningResponse screen(MultipartFile file, String email) throws IOException {

        // 1. cari user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        // 2. siapkan file sebagai multipart
        String filename = file.getOriginalFilename() != null
                ? file.getOriginalFilename()
                : "xray.jpg";

        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        // header untuk part file (content-type gambar)
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(
                file.getContentType() != null ? file.getContentType() : "image/jpeg"));
        HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(resource, fileHeaders);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", filePart);

        // header request (multipart)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        // 3. panggil FastAPI (dengan metrik latency)
        long start = System.nanoTime();
        FastApiResponse prediction = restTemplate.postForObject(
                fastApiUrl + "/predict/osteoporosis",
                request,
                FastApiResponse.class);
        screeningLatencyTimer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);

        // 4. simpan ke DB
        Screening screening = Screening.builder()
                .user(user)
                .label(prediction.label())
                .confidence(prediction.confidence())
                .risk(prediction.risk())
                .gradcamImage(prediction.gradcamImage())
                .latencyMs(prediction.latencyMs() != null
                        ? prediction.latencyMs().longValue()
                        : null)
                .build();

        screeningRepository.save(screening);
        screeningCounter.increment();

        return toResponse(screening);
    }

    /**
     * Mengambil riwayat screening milik pengguna, diurutkan dari yang terbaru.
     *
     * @param email email pengguna (diambil dari token JWT)
     * @return daftar riwayat screening pengguna
     * @throws ResourceNotFoundException bila user dengan email tersebut tidak ditemukan
     */
    public List<ScreeningResponse> getHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        return screeningRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ScreeningResponse toResponse(Screening s) {
        return new ScreeningResponse(
                s.getId(),
                s.getLabel(),
                s.getConfidence(),
                s.getRisk(),
                s.getGradcamImage(),
                s.getLatencyMs(),
                s.getCreatedAt());
    }
}