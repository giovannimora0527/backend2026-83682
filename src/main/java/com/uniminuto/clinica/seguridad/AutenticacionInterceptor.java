package com.uniminuto.clinica.seguridad;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.ForbiddenException;
import com.uniminuto.clinica.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Map;

/**
 * Valida el token JWT de cada solicitud y aplica los permisos declarados con {@link Permitido}.
 *
 * <ol>
 *   <li>Exige el encabezado {@code Authorization: Bearer <token>} con firma válida.</li>
 *   <li>Busca la sesión activa y vigente en la tabla {@code session} (el token se guarda como SHA-256).</li>
 *   <li>Verifica que el usuario y su perfil sigan activos.</li>
 *   <li>Comprueba que el perfil esté autorizado para el endpoint.</li>
 * </ol>
 */
@Component
public class AutenticacionInterceptor implements HandlerInterceptor {

    private final JwtServicio jwt;
    private final Db db;

    public AutenticacionInterceptor(JwtServicio jwt, Db db) {
        this.jwt = jwt;
        this.db = db;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || !(handler instanceof HandlerMethod metodo)) {
            return true;
        }
        SesionUsuario sesion = autenticar(request.getHeader(HttpHeaders.AUTHORIZATION));
        request.setAttribute(SesionActual.ATRIBUTO, sesion);

        Permitido permitido = AnnotatedElementUtils.findMergedAnnotation(metodo.getMethod(), Permitido.class);
        if (permitido == null) {
            permitido = AnnotatedElementUtils.findMergedAnnotation(metodo.getBeanType(), Permitido.class);
        }
        if (permitido != null && Arrays.stream(permitido.value()).noneMatch(sesion.perfil()::equals)) {
            throw new ForbiddenException("Tu perfil no tiene permiso para esta operación.");
        }
        return true;
    }

    SesionUsuario autenticar(String encabezado) {
        if (encabezado == null || !encabezado.startsWith("Bearer ")) {
            throw new UnauthorizedException("Debes iniciar sesión.");
        }
        String token = encabezado.substring(7).trim();
        if (!jwt.firmaValida(token)) {
            throw new UnauthorizedException("La sesión no es válida. Inicia sesión nuevamente.");
        }
        Map<String, Object> fila = db.uno("""
                SELECT s.session_id, u.id AS usuario_id, u.username, p.perfil_id, p.nombre AS perfil,
                       m.id AS medico_id
                FROM session s
                JOIN usuario u ON u.id = s.user_id
                JOIN perfil p ON p.perfil_id = u.rol
                LEFT JOIN medico m ON m.usuario_id = u.id
                WHERE s.token = :token AND s.activo = 1 AND s.fecha_expiracion > :ahora
                  AND u.activo = 1 AND p.activo = 1
                """, Db.p("token", ClaveUtil.sha256(token), "ahora", Tiempo.ahora()));
        if (fila == null) {
            throw new UnauthorizedException("La sesión expiró o fue cerrada. Inicia sesión nuevamente.");
        }
        return new SesionUsuario(Validar.largo(fila, "session_id"), Validar.largo(fila, "usuario_id"),
                Validar.cadena(fila, "username"), Validar.largo(fila, "perfil_id"),
                Perfiles.codigo(Validar.cadena(fila, "perfil")), Validar.largoNulo(fila, "medico_id"));
    }
}
