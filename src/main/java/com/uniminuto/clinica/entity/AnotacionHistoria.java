package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad que representa una anotación en la historia médica.
 * Cada anotación registra observaciones médicas realizadas por un médico
 * sobre un paciente específico en una historia médica determinada.
 *
 * @author TuNombre
 * @version 1.0
 */
@Entity
@Table(name = "anotacion_historia")
@Data
@NoArgsConstructor
public class AnotacionHistoria {

    /**
     * Identificador único de la anotación.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    /**
     * Identificador de la historia médica asociada.
     */
    @Column(name = "historia_id", nullable = false)
    private Integer historiaId;

    /**
     * Identificador del médico que realiza la anotación.
     */
    @Column(name = "medico_id", nullable = false)
    private Integer medicoId;

    /**
     * Fecha de la anotación.
     */
    @Column(name = "fecha")
    private LocalDateTime fecha;

    /**
     * Descripción de la anotación médica.
     */
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;
}