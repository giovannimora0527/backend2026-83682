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
 * Entidad que representa una fórmula médica
 * almacenada en la base de datos de la clínica.
 */
@Entity
@Table(name = "formula_medica")
@Data
public class FormulaMedica {

    /**
     * Identificador único de la fórmula médica.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador de la cita asociada a la fórmula médica.
     */
    @Column(name = "cita_id", nullable = false)
    private Integer citaId;

    /**
     * Identificador del medicamento asociado a la fórmula.
     */
    @Column(name = "medicamento_id", nullable = false)
    private Integer medicamentoId;

    /**
     * Dosis indicada para el medicamento.
     */
    @Column(name = "dosis", nullable = false, columnDefinition = "TEXT")
    private String dosis;

    /**
     * Indicaciones adicionales de la fórmula médica.
     */
    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;

    /**
     * Fecha y hora en la que se creó el registro.
     */
    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    /**
     * Fecha y hora de la última actualización del registro.
     */
    @Column(name = "fecha_actualizacion_registro")
    private LocalDateTime fechaActualizacionRegistro;
}