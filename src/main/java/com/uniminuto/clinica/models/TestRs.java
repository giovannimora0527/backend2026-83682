package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Clase que modela la respuesta del servicio de test.
 * @author gmora
 */
@SuppressWarnings("unused")
@Data
public class TestRs {
    /**
     * Respuesta del servicio.
     */
    private String respuesta;
}
