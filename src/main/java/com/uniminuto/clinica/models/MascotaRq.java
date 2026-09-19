package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Clase que representa la solicitud de una mascota.
 */
@Data
public class MascotaRq {

    /**
     * Identificador de la mascota.
     */
    private Integer mascotaId;

    /**
     * Nombre de la mascota.
     */
    private String nombreMascota;

    /**
     * Edad de la mascota.
     */
    private Integer edad;

    /**
     * Identificador de la raza de la mascota.
     */
    private Integer razaId;

    /**
     * Identificador del cliente propietario de la mascota.
     */
    private Long clienteId;
}
