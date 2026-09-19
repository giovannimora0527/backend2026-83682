package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad que representa la historia médica de un paciente en la clínica veterinaria.
 * Cada historia médica está asociada a un paciente y contiene anotaciones médicas
 * realizadas por los veterinarios durante las consultas.
 *
 * @author ANRUIZSS
 * @version 1.0
 */
@Entity
@Table(name = "historia_medica")
@Data
@NoArgsConstructor
public class HistoriaMedica {

    /**
     * Identificador único y autoincremental de la historia médica.
     * Corresponde a la llave primaria de la tabla.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    /**
     * Identificador del paciente (mascota) asociado a la historia médica.
     * Este campo hace referencia al paciente que recibe la atención.
     */
    @Column(name = "paciente_id", nullable = false)
    private Integer pacienteId;

    /**
     * Fecha y hora de creación del registro de la historia médica.
     * Se genera automáticamente cuando se crea la historia.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}