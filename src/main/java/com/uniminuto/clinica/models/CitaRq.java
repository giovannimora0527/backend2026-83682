package com.uniminuto.clinica.models;

import lombok.Data;
import java.time.LocalDateTime;

/** DTO de entrada para crear/actualizar una cita (fechas en formato ISO-8601). */
@Data
public class CitaRq {
    /** Id de la cita; obligatorio solo al actualizar. */
    private Long citaId;
    /** Fecha y hora de la cita; obligatoria. */
    private LocalDateTime fechaCita;
    /** Motivo de la cita; opcional. */
    private String motivo;
}
