package com.medikasentosa.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfigurasi global dokumentasi OpenAPI (Swagger) untuk backend RS Medika Sentosa.
 * Mendefinisikan informasi umum API yang tampil di halaman Swagger UI,
 * seperti judul, versi, dan deskripsi layanan.
 *
 * @author Ari
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Membuat metadata OpenAPI global (title, version, description)
     * yang akan digunakan oleh springdoc untuk menghasilkan dokumentasi API.
     *
     * @return objek {@link OpenAPI} berisi informasi global API
     */
    @Bean
    public OpenAPI medikaSentosaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RS Medika Sentosa API")
                        .version("1.0")
                        .description("REST API backend RS Medika Sentosa: "
                                + "menangani autentikasi pengguna, manajemen poli, "
                                + "dan screening osteoporosis berbasis machine learning."));
    }
}
