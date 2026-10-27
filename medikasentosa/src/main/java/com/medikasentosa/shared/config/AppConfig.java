package com.medikasentosa.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AppConfig {

    @Value("${app.fastapi.url}")
    private String fastApiUrl;

    @Bean
    public RestClient fastApiClient() {
        return RestClient.builder()
                .baseUrl(fastApiUrl)
                .build();
    }

    /**
     * RestTemplate dengan timeout eksplisit agar kegagalan layanan internal
     * tidak menggantung request pengguna tanpa batas.
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(60));
        return new RestTemplate(factory);
    }
}
