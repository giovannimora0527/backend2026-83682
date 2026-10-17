package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/** Evolución asociada a una historia clínica. */
@Entity
@Table(name = "anotacion_historia")
@Getter
@Setter
public class Anotacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long anotacionId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "historia_id", nullable = false)
    private HistoriaClinica historiaClinica;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Veterinario veterinario;

    @Column(name = "fecha")
    private LocalDateTime fechaHora;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Transient
    private String observacion;

    @Transient
    private String diagnosticoEvolucion;
}
