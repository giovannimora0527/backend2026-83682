package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/**
 * Error de validación de datos enviados por el cliente (HTTP 400).
 */
public class BadRequestException extends ClinicaException {

    /**
     * @param message mensaje para el usuario.
     */
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
