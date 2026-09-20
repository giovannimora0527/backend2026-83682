package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/** Se lanza cuando los datos de entrada son invalidos (responde HTTP 400). */
public class BadRequestException extends RuntimeException {

    /** Codigo HTTP asociado a esta excepcion. */
    private final HttpStatus status;

    /** Crea la excepcion con el mensaje a devolver al cliente. */
    public BadRequestException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    /** Codigo HTTP con el que debe responder el controlador. */
    public HttpStatus getStatus() {
        return status;
    }
}
