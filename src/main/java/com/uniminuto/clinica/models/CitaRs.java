package com.uniminuto.clinica.models;

import lombok.Data;
import java.time.LocalDateTime;

/** DTO de respuesta con los datos de una cita. */
@Data
public class CitaRs {
    /** Id de la cita. */
    private Long citaId;
    /** Fecha y hora de la cita. */
    private LocalDateTime fechaCita;
    /** Motivo de la cita. */
    private String motivo;
}
