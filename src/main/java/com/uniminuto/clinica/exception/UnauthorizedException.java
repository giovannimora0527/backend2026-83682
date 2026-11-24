package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/** No hay una sesión válida (HTTP 401). */
public class UnauthorizedException extends ClinicaException {

    /**
     * @param message mensaje para el usuario.
     */
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
