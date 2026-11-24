package com.uniminuto.clinica.config;

import com.uniminuto.clinica.seguridad.AutenticacionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración web: CORS para el frontend Angular y registro del interceptor de seguridad.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AutenticacionInterceptor autenticacion;

    public WebConfig(AutenticacionInterceptor autenticacion) {
        this.autenticacion = autenticacion;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autenticacion)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/login", "/error", "/v3/api-docs/**", "/swagger-ui/**",
                        "/swagger-ui.html", "/actuator/health");
    }
}
