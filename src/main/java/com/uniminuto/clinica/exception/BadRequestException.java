package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/**
 * Clase de excepción personalizada para manejar errores de solicitud incorrecta (Bad Request).
 */
public class BadRequestException extends RuntimeException {

    private final HttpStatus status;

    public BadRequestException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public BadRequestException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
