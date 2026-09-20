package com.uniminuto.clinica.models;

import lombok.Data;
import java.time.LocalDateTime;

/** DTO de respuesta con los datos de una formula medica. */
@Data
public class FormulaMedicaRs {
    /** Id de la formula. */
    private Long formulaMedicaId;
    /** Descripcion del medicamento o indicacion. */
    private String descripcion;
    /** Fecha de creacion de la formula. */
    private LocalDateTime fechaCreacion;
}
