package com.medikasentosa.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

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

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}