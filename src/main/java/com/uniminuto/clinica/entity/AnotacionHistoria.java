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
 * Entidad que representa una anotación
 * realizada dentro de una historia médica.
 */
@Entity
@Table(name = "anotacion_historia")
@Data
public class AnotacionHistoria {

    /**
     * Identificador único de la anotación.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador de la historia médica
     * a la cual pertenece la anotación.
     */
    @Column(name = "historia_id", nullable = false)
    private Integer historiaId;

    /**
     * Identificador del médico que realiza la anotación.
     */
    @Column(name = "medico_id", nullable = false)
    private Integer medicoId;

    /**
     * Fecha y hora de la anotación.
     */
    @Column(name = "fecha")
    private LocalDateTime fecha;

    /**
     * Descripción de la anotación realizada.
     */
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;
}