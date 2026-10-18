package com.uniminuto.clinica.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * CORS global para el frontend Angular. Los orígenes permitidos se configuran con
 * {@code app.cors.allowed-origins} (lista separada por comas) o la variable de entorno
 * {@code CORS_ALLOWED_ORIGINS}; por defecto, {@code http://localhost:4200} y {@code http://127.0.0.1:4200}.
 */
@Configuration
public class ApiCorsConfig {
    @Bean
    WebMvcConfigurer corsConfigurer(
            @Value("${app.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}") String origins) {
        String[] allowed = Arrays.stream(origins.split(","))
                .map(String::trim).filter(o -> !o.isEmpty()).toArray(String[]::new);
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                if (allowed.length == 0) return;
                registry.addMapping("/**")
                        .allowedOrigins(allowed)
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("Content-Type", "Authorization", "Accept")
                        .maxAge(3600);
            }
        };
    }
}
