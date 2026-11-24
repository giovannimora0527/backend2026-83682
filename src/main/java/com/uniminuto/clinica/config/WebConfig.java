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

    /** Interceptor que valida el token y los permisos. */
    private final AutenticacionInterceptor autenticacion;

    /**
     * @param autenticacion interceptor de seguridad.
     */
    public WebConfig(AutenticacionInterceptor autenticacion) {
        this.autenticacion = autenticacion;
    }

    /**
     * Permite solicitudes del frontend Angular en localhost.
     *
     * @param registry registro de CORS.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition");
    }

    /**
     * Registra el interceptor de seguridad en todas las rutas excepto las públicas (autenticación, Swagger y errores).
     *
     * @param registry registro de interceptores.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autenticacion)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/login", "/auth/verificar", "/auth/reenviar", "/auth/recuperar",
                        "/auth/restablecer", "/error", "/v3/api-docs/**", "/swagger-ui/**",
                        "/swagger-ui.html", "/actuator/health");
    }
}
