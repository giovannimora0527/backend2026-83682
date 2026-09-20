package com.uniminuto.clinica.models;

import lombok.Data;
import java.time.LocalDateTime;

/** DTO de respuesta con los datos de una anotacion, aplanando la historia asociada. */
@Data
public class AnotacionHistoriaRs {
    /** Id de la anotacion. */
    private Long anotacionHistoriaId;
    /** Id de la historia medica asociada. */
    private Long historiaMedicaId;
    /** Descripcion general de la historia medica asociada. */
    private String descripcionGeneralHistoria;
    /** Fecha y hora de la anotacion. */
    private LocalDateTime fechaAnotacion;
    /** Texto de la anotacion. */
    private String detalle;
}
