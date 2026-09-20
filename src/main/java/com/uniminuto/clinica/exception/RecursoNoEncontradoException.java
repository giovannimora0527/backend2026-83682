package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/** Se lanza cuando el recurso solicitado no existe (responde HTTP 404). */
public class RecursoNoEncontradoException extends RuntimeException {

    /** Codigo HTTP asociado a esta excepcion. */
    private final HttpStatus status;

    /** Crea la excepcion con el mensaje a devolver al cliente. */
    public RecursoNoEncontradoException(String message) {
        super(message);
        this.status = HttpStatus.NOT_FOUND;
    }

    /** Codigo HTTP con el que debe responder el controlador. */
    public HttpStatus getStatus() {
        return status;
    }
}
