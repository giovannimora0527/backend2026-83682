package com.uniminuto.clinica.exception;

import org.springframework.http.HttpStatus;

/**
 * La operación entra en conflicto con el estado actual de los datos (HTTP 409):
 * reglas de negocio, concurrencia o duplicidad.
 */
public class ConflictException extends ClinicaException {

    /**
     * @param message mensaje para el usuario.
     */
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
