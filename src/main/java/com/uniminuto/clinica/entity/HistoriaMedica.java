package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad que representa la historia clínica
 * de una mascota en la veterinaria.
 */
@Entity
@Table(name = "historia_medica")
@Data
public class HistoriaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Integer pacienteId;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ManyToOne
    @JoinColumn(name = "cita_id")
    private Cita cita;

    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;

    @Column(name = "motivo_consulta")
    private String motivoConsulta;

    @Column(name = "diagnostico")
    private String diagnostico;

    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "estado")
    private String estado;
}
