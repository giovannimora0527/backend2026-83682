package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/** Cita de la clinica veterinaria. Tabla {@code cita}. */
@Entity
@Table(name = "cita")
@Data
public class Cita {

    /** Id autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cita_id")
    private Long citaId;

    /** Fecha y hora de la cita; campo de filtro y de orden. */
    @Column(name = "fecha_cita", nullable = false)
    private LocalDateTime fechaCita;

    /** Motivo de la cita (opcional). */
    @Column(name = "motivo", length = 255)
    private String motivo;
}
