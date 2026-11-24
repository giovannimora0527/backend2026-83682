package com.uniminuto.clinica.seguridad;

import com.uniminuto.clinica.exception.ForbiddenException;
import com.uniminuto.clinica.exception.UnauthorizedException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Acceso a la sesión autenticada de la solicitud HTTP en curso.
 */
public final class SesionActual {

    static final String ATRIBUTO = "clinica.sesion";

    private SesionActual() {
    }

    public static SesionUsuario obtener() {
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes();
        Object sesion = atributos == null ? null : atributos.getAttribute(ATRIBUTO, RequestAttributes.SCOPE_REQUEST);
        if (sesion == null) {
            throw new UnauthorizedException("Debes iniciar sesión.");
        }
        return (SesionUsuario) sesion;
    }

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
