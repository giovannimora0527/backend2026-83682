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
 * Entidad que representa una cita
 * registrada en la clínica veterinaria.
 */
@Entity
@Table(name = "cita")
@Data
public class Cita {

    /**
     * Identificador único de la cita.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador del cliente asociado a la cita.
     */
    @Column(name = "cliente_id", nullable = false)
    private Integer clienteId;

    /**
     * Identificador de la mascota asociada a la cita.
     */
    @Column(name = "mascota_id", nullable = false)
    private Integer mascotaId;

    /**
     * Identificador del médico encargado de la cita.
     */
    @Column(name = "medico_id", nullable = false)
    private Integer medicoId;

    /**
     * Fecha y hora programada para la cita.
     */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /**
     * Estado actual de la cita.
     */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    /**
     * Motivo por el cual se solicita la cita.
     */
    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;
}