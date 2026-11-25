package com.uniminuto.clinica.seguridad;

import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.ClinicaException;
import com.uniminuto.clinica.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Filtro de Spring Security que autentica cada solicitud con el token JWT.
 *
 * <ol>
 *   <li>Verifica la firma del token (HS256).</li>
 *   <li>Busca la sesión activa y vigente en la tabla {@code session} (el token se guarda como SHA-256).</li>
 *   <li>Verifica que el usuario y su rol sigan activos.</li>
 *   <li>Carga los permisos del rol ({@code perfil_permiso}) como <i>authorities</i>: cada endpoint exige uno con
 *       {@code @PreAuthorize}, de modo que el backend es la autoridad de autorización.</li>
 * </ol>
 * Sin encabezado {@code Authorization} la solicitud continúa como anónima y Spring Security responde 401 si el
 * recurso exige autenticación.
 */
@Component
public class FiltroAutenticacion extends OncePerRequestFilter {

    /** Verificación de la firma de los tokens. */
    private final JwtServicio jwt;
    /** Acceso a la sesión activa y a los permisos del rol. */
    private final SesionRepository repositorio;

    /**
     * @param jwt servicio de tokens.
     * @param repositorio acceso a sesiones y permisos.
     */
    public FiltroAutenticacion(JwtServicio jwt, SesionRepository repositorio) {
        this.jwt = jwt;
        this.repositorio = repositorio;
    }

    /**
     * Autentica la solicitud si trae token y publica la sesión en el contexto de seguridad.
     *
     * @param request solicitud HTTP.
     * @param response respuesta HTTP.
     * @param cadena cadena de filtros.
     * @throws ServletException error del contenedor.
     * @throws IOException error de escritura.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain cadena)
            throws ServletException, IOException {
        String encabezado = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (encabezado == null || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            cadena.doFilter(request, response);
            return;
        }
        try {
            SesionUsuario sesion = autenticar(encabezado);
            request.setAttribute(SesionActual.ATRIBUTO, sesion);
            List<GrantedAuthority> autoridades = new ArrayList<>();
            autoridades.add(new SimpleGrantedAuthority("ROLE_" + sesion.perfil()));
            sesion.permisos().forEach(p -> autoridades.add(new SimpleGrantedAuthority(p)));
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(sesion, null, autoridades));
        } catch (ClinicaException e) {
            escribirError(response, e.getStatus().value(), e.getMessage());
            return;
        }
        cadena.doFilter(request, response);
    }

    /**
     * Valida el encabezado Authorization y obtiene la sesión activa con sus permisos.
     *
     * @param encabezado valor del encabezado Authorization (Bearer token).
     * @return sesión del usuario autenticado.
     */
    SesionUsuario autenticar(String encabezado) {
        if (!encabezado.startsWith("Bearer ")) {
            throw new UnauthorizedException("Debes iniciar sesión.");
        }
        String token = encabezado.substring(7).trim();
        if (!jwt.firmaValida(token)) {
            throw new UnauthorizedException("La sesión no es válida. Inicia sesión nuevamente.");
        }
        Map<String, Object> fila = repositorio.sesionActiva(ClaveUtil.sha256(token), Tiempo.ahora());
        if (fila == null) {
            throw new UnauthorizedException("La sesión expiró o fue cerrada. Inicia sesión nuevamente.");
        }
        long perfilId = Validar.largo(fila, "perfil_id");
        return new SesionUsuario(Validar.largo(fila, "session_id"), Validar.largo(fila, "usuario_id"),
                Validar.cadena(fila, "username"), perfilId, Perfiles.codigo(Validar.cadena(fila, "perfil")),
                Validar.largoNulo(fila, "medico_id"), repositorio.permisos(perfilId));
    }

    /**
     * Escribe una respuesta de error JSON con el formato común {status, error, mensaje}.
     *
     * @param response respuesta HTTP.
     * @param status código HTTP.
     * @param mensaje mensaje para el usuario.
     * @throws IOException error de escritura.
     */
    static void escribirError(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"error\":\"" + (status == 401 ? "UNAUTHORIZED" : "FORBIDDEN")
                + "\",\"mensaje\":\"" + mensaje.replace("\"", "\\\"") + "\"}");
    }
}
