package com.uniminuto.clinica.security;

import com.uniminuto.clinica.exception.dto.ErrorResponse;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.repository.SessionRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] RUTAS_PUBLICAS = {
            "/auth/login", "/auth/validate-token", "/v3/api-docs/**", "/swagger-ui/**",
            "/swagger-ui.html", "/clinica/test", "/actuator/health"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtValidator jwtValidator,
                                            SessionRepository sessionRepository,
                                            UsuarioRepository usuarioRepository,
                                            MedicoRepository medicoRepository,
                                            ObjectMapper objectMapper) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> { })
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(RUTAS_PUBLICAS).permitAll()
                        // Segunda barrera por modulo; el detalle por metodo esta en @PreAuthorize (C6).
                        .requestMatchers("/usuario/**", "/perfil/**", "/reporte/**", "/email/**",
                                "/inventario/carga/**").hasRole(Rol.ADMINISTRADOR)
                        .requestMatchers("/factura/*/anular").hasRole(Rol.ADMINISTRADOR)
                        .requestMatchers("/factura/**").hasAnyRole(Rol.ADMINISTRADOR, Rol.SECRETARIA)
                        .requestMatchers("/cita/**", "/medico/**", "/cliente/**", "/mascota/**", "/raza/**")
                        .hasAnyRole(Rol.ADMINISTRADOR, Rol.SECRETARIA, Rol.MEDICO)
                        .requestMatchers(HttpMethod.POST, "/atencion/**").hasRole(Rol.MEDICO)
                        .requestMatchers(HttpMethod.PUT, "/atencion/**").hasRole(Rol.MEDICO)
                        .requestMatchers("/atencion/**", "/historia-clinica/**", "/anotacion-historia/**")
                        .hasAnyRole(Rol.ADMINISTRADOR, Rol.MEDICO)
                        .requestMatchers(HttpMethod.POST, "/despacho/*/revertir").hasRole(Rol.ADMINISTRADOR)
                        .requestMatchers(HttpMethod.POST, "/despacho/*/confirmar", "/despacho/formula/*/preparar")
                        .hasRole(Rol.DESPACHO_MEDICO)
                        .requestMatchers("/despacho/**").hasAnyRole(Rol.DESPACHO_MEDICO, Rol.ADMINISTRADOR)
                        .requestMatchers(HttpMethod.GET, "/formula-medica/**")
                        .hasAnyRole(Rol.ADMINISTRADOR, Rol.MEDICO, Rol.DESPACHO_MEDICO)
                        .requestMatchers("/formula-medica/*/anular").hasAnyRole(Rol.ADMINISTRADOR, Rol.MEDICO)
                        .requestMatchers("/formula-medica/**").hasRole(Rol.MEDICO)
                        .requestMatchers(HttpMethod.GET, "/inventario/**")
                        .hasAnyRole(Rol.ADMINISTRADOR, Rol.DESPACHO_MEDICO, Rol.MEDICO)
                        .requestMatchers("/inventario/**").hasRole(Rol.ADMINISTRADOR)
                        .requestMatchers(HttpMethod.GET, "/servicio/**", "/impuesto/**", "/tarifa/**",
                                "/configuracion-clinica", "/especializacion/**").authenticated()
                        .requestMatchers("/servicio/**", "/impuesto/**", "/tarifa/**",
                                "/configuracion-clinica", "/especializacion/**").hasRole(Rol.ADMINISTRADOR)
                        .anyRequest().authenticated())
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint((request, response, ex) -> escribirError(response, objectMapper,
                                HttpStatus.UNAUTHORIZED,
                                "Debe iniciar sesión o su sesión ya no es válida."))
                        .accessDeniedHandler((request, response, ex) -> escribirError(response, objectMapper,
                                HttpStatus.FORBIDDEN,
                                "No tiene permisos para realizar esta operación.")))
                .addFilterBefore(new JwtAuthenticationFilter(jwtValidator, sessionRepository, usuarioRepository,
                                medicoRepository),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origins}") List<String> origenes) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(origenes.stream().map(String::trim).toList());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setExposedHeaders(List.of("Content-Disposition"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void escribirError(HttpServletResponse response, ObjectMapper objectMapper, HttpStatus status,
                                      String mensaje) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
                new ErrorResponse(status, mensaje)));
    }
}
