package com.uniminuto.clinica.models;

import lombok.Data;
import java.time.LocalDateTime;

/** DTO de entrada para crear/actualizar una anotacion de historia medica. */
@Data
public class AnotacionHistoriaRq {
    /** Id de la anotacion; obligatorio solo al actualizar. */
    private Long anotacionHistoriaId;
    /** Id de la historia medica asociada; obligatorio al crear. */
    private Long historiaMedicaId;
    /** Fecha y hora de la anotacion; obligatoria. */
    private LocalDateTime fechaAnotacion;
    /** Texto de la anotacion; obligatorio. */
    private String detalle;
}
