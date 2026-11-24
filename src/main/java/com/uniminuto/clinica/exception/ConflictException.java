package com.uniminuto.clinica.exception;

import com.uniminuto.clinica.exception.dto.ErrorResponse;

import java.util.List;

/** Regla de negocio incumplida o dato duplicado (409). */
public class ConflictException extends RuntimeException {

    private final List<ErrorResponse.Detalle> detalles;

    public ConflictException(String message) {
        super(message);
        this.detalles = null;
    }

    /** Duplicado: indica el campo de la peticion que lo causa (C1). */
    public ConflictException(String campo, String message) {
        this(message, List.of(new ErrorResponse.Detalle(campo, message)));
    }

    public ConflictException(String message, List<ErrorResponse.Detalle> detalles) {
        super(message);
        this.detalles = detalles;
    }

    public List<ErrorResponse.Detalle> getDetalles() {
        return detalles;
    }
}
