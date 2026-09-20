package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * DTO de respuesta generica usado por los servicios que no devuelven un
 * recurso, sino solamente el resultado de la operacion.
 */
@Data
public class MiRespuestaRS {

    /**
     * Codigo de estado HTTP de la operacion.
     */
    private int status;

    /**
     * Mensaje descriptivo del resultado de la operacion.
     */
    private String message;
}
