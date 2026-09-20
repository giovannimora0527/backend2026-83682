package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/** Formula medica del inventario. Tabla {@code formula_medica}. */
@Entity
@Table(name = "formula_medica")
@Data
public class FormulaMedica {

    /** Id autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "formula_medica_id")
    private Long formulaMedicaId;

    /** Descripcion del medicamento o indicacion. */
    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    /** Fecha de creacion, usada para el ordenamiento del requerimiento 1. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}
