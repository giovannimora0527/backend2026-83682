package com.uniminuto.clinica.seguridad;

import com.uniminuto.clinica.exception.ForbiddenException;
import com.uniminuto.clinica.exception.UnauthorizedException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Acceso a la sesión autenticada de la solicitud HTTP en curso.
 */
public final class SesionActual {

    /** Nombre del atributo de la solicitud donde el interceptor guarda la sesión. */
    static final String ATRIBUTO = "clinica.sesion";

    /** Clase de utilidades: no se instancia. */
    private SesionActual() {
    }

    /**
     * Obtiene la sesión autenticada de la solicitud en curso.
     *
     * @return sesión del usuario.
     * @throws com.uniminuto.clinica.exception.UnauthorizedException si no hay sesión.
     */
    public static SesionUsuario obtener() {
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes();
        Object sesion = atributos == null ? null : atributos.getAttribute(ATRIBUTO, RequestAttributes.SCOPE_REQUEST);
        if (sesion == null) {
            throw new UnauthorizedException("Debes iniciar sesión.");
        }
        return (SesionUsuario) sesion;
    }

    /**
     * @return identificador del usuario autenticado.
     */
    public static long usuarioId() {
        return obtener().usuarioId();
    }

    /** Devuelve el médico asociado al usuario actual o lanza 403 si no tiene uno. */
    public static long medicoId() {
        Long medico = obtener().medicoId();
        if (medico == null) {
            throw new ForbiddenException("Tu usuario no está asociado a un médico veterinario.");
        }
        return medico;
    }
}
