package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDateTime;

/**
 * Fórmula médica mapeada exactamente a las columnas existentes de {@code formula_medica}:
 * id, cita_id, medicamento_id, dosis, indicaciones, fecha_creacion_registro y
 * fecha_actualizacion_registro. La mascota, el cliente y el veterinario se obtienen de la cita.
 */
@Entity
@Table(name = "formula_medica")
@Getter
@Setter
public class FormulaMedica {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long formulaMedicaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
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
}
