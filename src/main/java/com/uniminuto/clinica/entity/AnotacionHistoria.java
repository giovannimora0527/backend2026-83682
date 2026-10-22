package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "anotacion_historia")
@Data
public class AnotacionHistoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @JoinColumn(name = "historia_id", nullable = false)
    @ManyToOne
    private HistoriaMedica historia;

    @JoinColumn(name = "medico_id", nullable = false)
    @ManyToOne
    private Medico medico;

    @JoinColumn(name = "cita_id", nullable = false)
    @ManyToOne
    private Cita cita;

    @Column(name = "fecha", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fecha;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}
