package com.uniminuto.clinica.exception;

import com.uniminuto.clinica.exception.dto.ErrorResponse;
import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * Clase de excepción personalizada para manejar errores de solicitud incorrecta (Bad Request).
 */
public class BadRequestException extends RuntimeException {

    private final HttpStatus status;
    private final List<ErrorResponse.Detalle> detalles;

    public BadRequestException(String message) {
        this(message, null);
    }

    public BadRequestException(String message, List<ErrorResponse.Detalle> detalles) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.detalles = detalles;
    }

    public List<ErrorResponse.Detalle> getDetalles() {
        return detalles;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
