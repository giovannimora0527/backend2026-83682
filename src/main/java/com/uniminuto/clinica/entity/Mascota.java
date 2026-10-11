
package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * ENTIDAD MASCOTA
 *
 * Representa la tabla mascota de MySQL.
 *
 * Cada atributo corresponde a una columna.
 */
@Entity
@Table(name = "mascota")
@Data
public class Mascota {

    /**
     * Identificador único.
     * MySQL genera su valor automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mascota_id", nullable = false)
    private Integer mascotaId;

    /**
     * Nombre de la mascota.
     */
    @Column(name = "nombre_mascota", nullable = false)
    private String nombreMascota;

    /**
     * Edad de la mascota.
     */
    @Column(name = "edad", nullable = false)
    private Integer edad;

    /**
     * Identificador de la raza.
     *
     * Corresponde a raza_id en MySQL.
     */
    @Column(name = "raza_id", nullable = false)
    private Integer razaId;

    /**
     * Identificador del propietario.
     *
     * Corresponde a cliente_id en MySQL.
     */
    @Column(name = "cliente_id", nullable = false)
    private Integer clienteId;

    /**
     * Fecha en la que se registra la mascota.
     */
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    /**
     * Fecha de la última modificación.
     */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
}
