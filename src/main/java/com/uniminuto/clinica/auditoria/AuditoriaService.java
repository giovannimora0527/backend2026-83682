package com.uniminuto.clinica.auditoria;

import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.seguridad.SesionActual;
import com.uniminuto.clinica.seguridad.SesionUsuario;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Log central de operaciones sensibles: inicio y cierre de sesión, usuarios, roles y permisos, anulaciones,
 * reversiones, ajustes de inventario, importaciones, tarifas y archivado de registros. Se escribe dentro de la
 * misma transacción de la operación, de modo que solo queda registrado lo que realmente se confirmó.
 */
@Service
public class AuditoriaService {

    /** Registros por página en la consulta. */
    static final int TAMANO = 50;

    /** Acceso a la tabla auditoria. */
    private final AuditoriaRepository repositorio;

    /**
     * @param repositorio repositorio de auditoría.
     */
    public AuditoriaService(AuditoriaRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Registra una operación del usuario autenticado.
     *
     * @param accion código de la acción.
     * @param entidad entidad afectada.
     * @param entidadId identificador de la entidad (puede ser null).
     * @param detalle descripción legible.
     */
    public void registrar(String accion, String entidad, Long entidadId, String detalle) {
        SesionUsuario s = sesionOpcional();
        repositorio.insertar(new RegistroAuditoria(s == null ? null : s.usuarioId(), s == null ? null : s.username(),
                s == null ? null : s.perfil(), accion, entidad, entidadId, recortar(detalle), ip(), Tiempo.ahora()));
    }

    /**
     * Registra una operación sin sesión autenticada (inicio de sesión, recuperación de contraseña). Usa una
     * transacción propia para que un intento fallido quede registrado aunque la operación termine en error.
     *
     * @param usuarioId usuario identificado (o null si no existe).
     * @param username nombre de usuario indicado.
     * @param perfil rol del usuario (o null).
     * @param accion código de la acción.
     * @param detalle descripción legible.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarSinSesion(Long usuarioId, String username, String perfil, String accion, String detalle) {
        repositorio.insertar(new RegistroAuditoria(usuarioId, recortarCampo(username), perfil, accion, "usuario",
                usuarioId, recortar(detalle), ip(), Tiempo.ahora()));
    }

    /**
     * Consulta el log con filtros.
     *
     * @param desde fecha inicial AAAA-MM-DD (opcional).
     * @param hasta fecha final AAAA-MM-DD (opcional).
     * @param accion acción (opcional).
     * @param usuario parte del nombre de usuario (opcional).
     * @param pagina página desde 0.
     * @return filas, página, indicador de más páginas y acciones disponibles.
     */
    public Map<String, Object> consultar(String desde, String hasta, String accion, String usuario, Integer pagina) {
        LocalDate d = Validar.fecha(desde, "desde", false);
        LocalDate h = Validar.fecha(hasta, "hasta", false);
        if (d != null && h != null && h.isBefore(d)) {
            throw new BadRequestException("La fecha final no puede ser anterior a la inicial.");
        }
        int p = Validar.pagina(pagina);
        List<Map<String, Object>> filas = repositorio.buscar(d == null ? null : d.atStartOfDay(),
                h == null ? null : h.plusDays(1).atStartOfDay(), vacio(accion), vacio(usuario), TAMANO + 1, p * TAMANO);
        boolean hayMas = filas.size() > TAMANO;
        return Map.of("filas", hayMas ? filas.subList(0, TAMANO) : filas, "pagina", p, "hayMas", hayMas,
                "acciones", repositorio.acciones().stream().map(f -> f.get("accion")).toList());
    }

    /** @return sesión de la solicitud actual o null si no hay. */
    private static SesionUsuario sesionOpcional() {
        try {
            return SesionActual.obtener();
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** @return IP de origen de la solicitud (considera el proxy nginx). */
    private static String ip() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a)) {
            return null;
        }
        HttpServletRequest r = a.getRequest();
        String reenviada = r.getHeader("X-Forwarded-For");
        String ip = reenviada != null && !reenviada.isBlank() ? reenviada.split(",")[0].trim() : r.getRemoteAddr();
        return recortarCampo(ip);
    }

    /**
     * @param texto detalle.
     * @return detalle de máximo 1000 caracteres.
     */
    private static String recortar(String texto) {
        return texto == null || texto.length() <= 1000 ? texto : texto.substring(0, 1000);
    }

    /**
     * @param texto valor corto.
     * @return valor de máximo 50 caracteres.
     */
    private static String recortarCampo(String texto) {
        return texto == null || texto.length() <= 50 ? texto : texto.substring(0, 50);
    }

    /**
     * @param texto texto recibido.
     * @return null si está vacío.
     */
    private static String vacio(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
