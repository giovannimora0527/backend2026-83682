package com.uniminuto.clinica.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** CORS de desarrollo limitado al origen del frontend local. */
@Configuration
@Profile({"dev", "prod"})
public class ApiCorsConfig {
    @Bean
    WebMvcConfigurer developmentCors(@Value("${app.cors.allowed-origin:http://localhost:4200}") String origin) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                if (origin == null || origin.isBlank()) return;
                registry.addMapping("/**").allowedOrigins(origin).allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("Content-Type", "Authorization");
            }
        };
    }
}
