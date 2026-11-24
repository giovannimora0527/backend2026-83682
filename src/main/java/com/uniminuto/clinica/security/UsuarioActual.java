package com.uniminuto.clinica.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Entrega a los servicios el usuario autenticado; nunca se toma del cuerpo de la peticion. */
@Component
public class UsuarioActual {

    public UsuarioAutenticado obtener() {
        var autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null && autenticacion.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return usuario;
        }
        throw new AuthenticationCredentialsNotFoundException("No hay un usuario autenticado.");
    }

    /** Un MEDICO solo opera sobre lo suyo; los demas roles los limita @PreAuthorize. */
    public void verificarPropiedad(Long medicoId) {
        UsuarioAutenticado usuario = obtener();
        if (usuario.esMedico() && (usuario.medicoId() == null || !usuario.medicoId().equals(medicoId))) {
            throw new AccessDeniedException("El recurso pertenece a otro medico.");
        }
    }
}
