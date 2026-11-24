package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/** El usuario autenticado no tiene permiso para la operación (HTTP 403). */
public class ForbiddenException extends ClinicaException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
