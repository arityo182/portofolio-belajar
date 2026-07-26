package com.medikasentosa.shared.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Konfigurasi web: register RequestIdFilter + prefix API versioning.
 *
 * <p>Untuk production: ganti {@code server.servlet.context-path=/api/v1}
 * di application.properties.</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<RequestIdFilter> requestIdFilter() {
        FilterRegistrationBean<RequestIdFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new RequestIdFilter());
        bean.addUrlPatterns("/api/*");
        bean.setOrder(1);
        return bean;
    }
}
