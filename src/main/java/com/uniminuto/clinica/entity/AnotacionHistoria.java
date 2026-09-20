package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/** Anotacion de una historia medica. Lado "muchos" de la relacion con {@link HistoriaMedica}. */
@Entity
@Table(name = "anotacion_historia")
@Data
public class AnotacionHistoria {

    /** Id autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "anotacion_historia_id")
    private Long anotacionHistoriaId;

    /** Historia medica a la que pertenece la anotacion. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "historia_medica_id", nullable = false)
    private HistoriaMedica historiaMedica;

    /** Fecha y hora de la anotacion; campo de filtro y de orden. */
    @Column(name = "fecha_anotacion", nullable = false)
    private LocalDateTime fechaAnotacion;

    /** Texto escrito por el veterinario. */
    @Column(name = "detalle", nullable = false, length = 255)
    private String detalle;
}
