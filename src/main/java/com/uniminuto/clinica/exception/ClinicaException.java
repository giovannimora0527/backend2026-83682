package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción base de la aplicación. Cada subclase define el código HTTP
 * con el que se informa el error al frontend.
 */
public class ClinicaException extends RuntimeException {

    /** Código HTTP con el que se responde. */
    private final HttpStatus status;

    /**
     * @param status código HTTP.
     * @param message mensaje para el usuario.
     */
    public ClinicaException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * @return código HTTP de la excepción.
     */
    public HttpStatus getStatus() {
        return status;
    }
}
