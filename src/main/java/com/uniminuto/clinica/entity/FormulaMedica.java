package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/** Fórmula almacenada según las tablas existentes de la clínica. */
@Entity
@Table(name = "formula_medica")
@Getter
@Setter
public class FormulaMedica {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long formulaMedicaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = true)
    private Cita cita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historia_medica_id")
    private HistoriaClinica historiaClinica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id")
    private Veterinario veterinario;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamentoEntity;

    @Column(name = "dosis", nullable = false, columnDefinition = "TEXT")
    private String dosisTexto;

    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;

    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion_registro")
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_emision")
    private java.time.LocalDate fechaEmision;
}
