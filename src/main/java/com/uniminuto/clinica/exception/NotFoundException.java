package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/** El recurso solicitado no existe (HTTP 404). */
public class NotFoundException extends ClinicaException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
