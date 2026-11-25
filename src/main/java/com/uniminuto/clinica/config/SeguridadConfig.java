package com.uniminuto.clinica.config;

import com.uniminuto.clinica.seguridad.FiltroAutenticacion;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración de Spring Security (RBAC).
 *
 * <ul>
 *   <li>API sin estado: autenticación por JWT + sesión en base de datos ({@link FiltroAutenticacion}).</li>
 *   <li>Rutas públicas: inicio de sesión en dos pasos, recuperación de contraseña, Swagger y salud.</li>
 *   <li>Todo lo demás exige autenticación, y cada endpoint exige un permiso con {@code @PreAuthorize}
 *       ({@link EnableMethodSecurity}). Los permisos de cada rol están en la tabla {@code perfil_permiso}.</li>
 *   <li>401/403 se responden en JSON con el mismo formato que el resto de errores.</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity
public class SeguridadConfig {

    /** Rutas que no requieren sesión. */
    private static final String[] PUBLICAS = {"/auth/login", "/auth/verificar", "/auth/reenviar", "/auth/recuperar",
        "/auth/restablecer", "/error", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/health"};

    /**
     * Cadena de filtros de seguridad.
     *
     * @param http configurador de Spring Security.
     * @param filtro filtro de autenticación JWT.
     * @return cadena configurada.
     * @throws Exception error de configuración.
     */
    @Bean
    public SecurityFilterChain cadenaSeguridad(HttpSecurity http, FiltroAutenticacion filtro) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(cors()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLICAS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> RespuestaSeguridad.escribir(res, 401, "Debes iniciar sesión."))
                        .accessDeniedHandler((req, res, ex) ->
                                RespuestaSeguridad.escribir(res, 403, "Tu perfil no tiene permiso para esta operación.")))
                .addFilterBefore(filtro, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Evita que el filtro se registre dos veces (como filtro del servlet y dentro de Spring Security).
     *
     * @param filtro filtro de autenticación.
     * @return registro deshabilitado fuera de la cadena de seguridad.
     */
    @Bean
    public FilterRegistrationBean<FiltroAutenticacion> registroFiltro(FiltroAutenticacion filtro) {
        FilterRegistrationBean<FiltroAutenticacion> r = new FilterRegistrationBean<>(filtro);
        r.setEnabled(false);
        return r;
    }

    /**
     * Sin usuarios en memoria: la autenticación es solo por JWT (evita el usuario por defecto de Spring Boot).
     *
     * @return gestor vacío.
     */
    @Bean
    public UserDetailsService sinUsuariosEnMemoria() {
        return new InMemoryUserDetailsManager();
    }

    /**
     * CORS para el frontend Angular (desarrollo en localhost; en Docker nginx sirve el mismo origen).
     *
     * @return configuración CORS.
     */
    @Bean
    public CorsConfigurationSource cors() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setExposedHeaders(List.of("Content-Disposition"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", c);
        return fuente;
    }
}
