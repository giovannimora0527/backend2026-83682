package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

/** Historia medica de una mascota. Lado "uno" de la relacion con {@link AnotacionHistoria}. */
@Entity
@Table(name = "historia_medica")
@Data
public class HistoriaMedica {

    /** Id autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "historia_medica_id")
    private Long historiaMedicaId;

    /** Resumen o descripcion general de la historia. */
    @Column(name = "descripcion_general", length = 255)
    private String descripcionGeneral;
}
