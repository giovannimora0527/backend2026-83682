package com.uniminuto.clinica.security;

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
}
