package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/** Registro clínico longitudinal de una mascota. */
@Entity
@Table(name = "historia_medica")
@Getter
@Setter
public class HistoriaClinica {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long historiaClinicaId;

    @Transient
    private Cliente cliente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Mascota mascota;

    @Transient
    private Cita cita;

    @Transient
    private Veterinario veterinario;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaHora;

    @Transient
    private String motivoConsulta;

    @Transient
    private String diagnostico;

    @Transient
    private String observaciones;
}
