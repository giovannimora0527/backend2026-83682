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
 * Entidad que representa la historia médica
 * de un paciente de la clínica veterinaria.
 */
@Entity
@Table(name = "historia_medica")
@Data
public class HistoriaMedica {

    /**
     * Identificador único de la historia médica.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador del paciente asociado
     * a la historia médica.
     */
    @Column(name = "paciente_id", nullable = false)
    private Integer pacienteId;

    /**
     * Fecha y hora de creación de la historia médica.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}