package co.edu.uniminuto.veterinaria.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Permite que el frontend Angular (http://localhost:4200) consuma la API durante el desarrollo.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Registra la política CORS para todos los endpoints bajo /api.
     *
     * @param registry registro de mapeos CORS
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200", "http://127.0.0.1:4200")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
