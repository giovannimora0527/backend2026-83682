package com.uniminuto.clinica.config;

import com.uniminuto.clinica.service.IdentidadService;
import com.uniminuto.clinica.service.IdentidadService.Identidad;
import com.uniminuto.clinica.service.IdentidadService.Rol;
import com.uniminuto.clinica.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Set<String> DOCUMENTACION = Set.of(
            "/v3/api-docs", "/v3/api-docs.yaml", "/v3/api-docs/swagger-config", "/swagger-ui.html");
    private static final Set<String> CATALOGOS = Set.of(
            "/medico/listar", "/raza/listar", "/especializacion/listar");
    private static final Set<String> RECEPCION = Set.of(
            "/cliente/listar-todos", "/mascota/listar-mascotas",
            "/mascota/listar-mascotas-ordenado", "/mascota/listar-detalle");

    private final JwtService jwt;
    private final IdentidadService identidades;

    public JwtAuthenticationFilter(JwtService jwt, IdentidadService identidades) {
        this.jwt = jwt;
        this.identidades = identidades;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String ruta = request.getServletPath();
        String metodo = request.getMethod();
        boolean consultaDocumentacion = ("GET".equals(metodo) || "HEAD".equals(metodo))
                && (DOCUMENTACION.contains(ruta) || ruta.startsWith("/swagger-ui/"));
        return consultaDocumentacion || "OPTIONS".equals(metodo)
                || ("POST".equals(metodo) && "/auth/login".equals(ruta))
                || ("GET".equals(metodo) && "/clinica/test".equals(ruta));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Identidad identidad;
        try {
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
                throw new IllegalArgumentException();
            }
            var claims = jwt.leerToken(authorization.substring(7));
            String username = claims.getSubject();
            Long usuarioId = claims.get("usuarioId", Long.class);
            if (username == null || username.isBlank() || usuarioId == null) {
                throw new IllegalArgumentException();
            }
            var usuario = identidades.usuarioActivo(username, usuarioId).orElseThrow(IllegalArgumentException::new);
            identidad = identidades.perfilActivo(usuario);
        } catch (JwtException | IllegalArgumentException error) {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            ApiError.escribir(response, 401, "Se requiere una sesión válida.");
            return;
        } catch (ResponseStatusException error) {
            ApiError.escribir(response, error.getStatusCode().value(), error.getReason());
            return;
        }

        if (!permite(identidad.rol(), request.getMethod(), request.getServletPath())) {
            ApiError.escribir(response, 403, "No tienes permiso para esta operación.");
            return;
        }
        request.setAttribute(IdentidadService.ATRIBUTO, identidad);
        request.setAttribute("usuarioAutenticado", identidad.username());
        chain.doFilter(request, response);
    }

    static boolean permite(Rol rol, String metodo, String ruta) {
        boolean lectura = "GET".equals(metodo) || "HEAD".equals(metodo);
        if (ruta.equals("/reportes") || ruta.startsWith("/reportes/")) {
            return lectura && (rol == Rol.ADMIN || rol == Rol.RECEPCION
                    && ruta.matches("/reportes/CITAS_OPERATIVAS(/pdf|/excel)?"));
        }
        if (ruta.equals("/facturacion") || ruta.startsWith("/facturacion/")) {
            if (ruta.matches("/facturacion/facturas/[0-9]+/anular")) return rol == Rol.ADMIN;
            return rol == Rol.ADMIN || rol == Rol.RECEPCION;
        }
        if (ruta.equals("/despacho") || ruta.startsWith("/despacho/")) return rol == Rol.DESPACHO_MEDICO;
        if (ruta.startsWith("/tarifario/")) return rol == Rol.ADMIN;
        if (ruta.startsWith("/atencion/")) return rol == Rol.MEDICO;
        if (ruta.startsWith("/formula-medica/") && !lectura) return rol == Rol.MEDICO
                && (("POST".equals(metodo) && ruta.equals("/formula-medica/crear"))
                    || ("PUT".equals(metodo) && ruta.matches("/formula-medica/actualizar/[0-9]+")));
        if (rol == Rol.ADMIN || (lectura && (ruta.equals("/auth/validar")
                || ruta.equals("/menu/propio") || ruta.startsWith("/menu/usuario/")))) {
            return true;
        }
        if (ruta.equals("/inventario") || ruta.startsWith("/inventario/")) {
            return lectura && (rol == Rol.DESPACHO_MEDICO || rol == Rol.MEDICO)
                    && (ruta.equals("/inventario/productos") || ruta.matches("/inventario/productos/[0-9]+/lotes"));
        }
        if (rol == Rol.DESPACHO_MEDICO) {
            return lectura && ruta.equals("/medicamento/listar");
        }
        if (ruta.startsWith("/cita/agenda/")) {
            if (ruta.matches("/cita/agenda/[0-9]+/historial")) return lectura;
            if (ruta.matches("/cita/agenda/[0-9]+/cambio")) return "PUT".equals(metodo);
            if (ruta.matches("/cita/agenda/medicos/[0-9]+/disponibilidad")) return lectura;
            if (rol != Rol.MEDICO) return false;
            return (lectura && Set.of("/cita/agenda/mia", "/cita/agenda/mia/excel",
                    "/cita/agenda/mia/configuracion").contains(ruta))
                    || ("PUT".equals(metodo) && ruta.equals("/cita/agenda/mia/horarios"))
                    || ("POST".equals(metodo) && ruta.equals("/cita/agenda/mia/ausencias"));
        }
        boolean citas = ruta.startsWith("/cita/") && (lectura || Set.of("POST", "PUT").contains(metodo));
        if (rol == Rol.RECEPCION) {
            return (lectura && (CATALOGOS.contains(ruta) || RECEPCION.contains(ruta)))
                    || ("POST".equals(metodo) && ruta.equals("/mascota/crear")) || citas;
        }
        return (lectura && (CATALOGOS.contains(ruta) || Set.of("/medicamento/listar",
                "/cliente/listar-todos", "/mascota/listar-detalle").contains(ruta)))
                || (ruta.startsWith("/cita/") && (lectura || "PUT".equals(metodo)))
                || (ruta.startsWith("/historia-medica/") && lectura)
                || ((ruta.startsWith("/formula-medica/") || ruta.startsWith("/anotacion-historia/"))
                    && (lectura || Set.of("POST", "PUT").contains(metodo)));
    }
}
