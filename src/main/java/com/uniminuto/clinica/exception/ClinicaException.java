package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción base de la aplicación. Cada subclase define el código HTTP
 * con el que se informa el error al frontend.
 */
public class ClinicaException extends RuntimeException {

    private final HttpStatus status;

    public ClinicaException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
