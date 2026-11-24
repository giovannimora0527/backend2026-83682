package com.uniminuto.clinica.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.repository.SessionRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Autentica la peticion si el token es valido y su sesion sigue activa. Si no lo es, la peticion
 * continua sin autenticar y las rutas protegidas responden 401.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtValidator jwtValidator;
    private final SessionRepository sessionRepository;
    private final UsuarioRepository usuarioRepository;
    private final MedicoRepository medicoRepository;

    public JwtAuthenticationFilter(JwtValidator jwtValidator, SessionRepository sessionRepository,
                                   UsuarioRepository usuarioRepository, MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
        this.jwtValidator = jwtValidator;
        this.sessionRepository = sessionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            autenticar(cabecera.substring(PREFIJO.length())).ifPresent(usuario -> {
                SecurityContext contexto = SecurityContextHolder.createEmptyContext();
                contexto.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                        usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol()))));
                SecurityContextHolder.setContext(contexto);
            });
        }
        chain.doFilter(request, response);
    }

    private Optional<UsuarioAutenticado> autenticar(String token) {
        String jti;
        Long usuarioId;
        try {
            Claims claims = jwtValidator.validar(token);
            jti = claims.getId();
            usuarioId = Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
        if (jti == null) {
            return Optional.empty();
        }
        return sessionRepository.findByJti(jti)
                .filter(sesion -> Boolean.TRUE.equals(sesion.getActiva()))
                .filter(sesion -> usuarioId.equals(sesion.getUserId()))
                .filter(sesion -> sesion.getFechaExpiracion().isAfter(LocalDateTime.now()))
                .flatMap(sesion -> usuarioRepository.findById(usuarioId))
                .filter(usuario -> Boolean.TRUE.equals(usuario.getActivo()))
                .filter(usuario -> usuario.getPerfil().getCodigo() != null
                        && Boolean.TRUE.equals(usuario.getPerfil().getActivo()))
                .map(usuario -> new UsuarioAutenticado(usuario.getId(), usuario.getUsername(),
                        usuario.getPerfil().getCodigo(), medicoId(usuario), jti));
    }

    private Long medicoId(Usuario usuario) {
        if (!Rol.MEDICO.equals(usuario.getPerfil().getCodigo())) {
            return null;
        }
        return medicoRepository.findByUsuarioId(usuario.getId()).map(Medico::getId).orElse(null);
    }
}
