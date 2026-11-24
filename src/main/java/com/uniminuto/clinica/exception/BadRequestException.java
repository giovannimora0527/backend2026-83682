package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/**
 * Error de validación de datos enviados por el cliente (HTTP 400).
 */
public class BadRequestException extends ClinicaException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
