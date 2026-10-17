package com.uniminuto.clinica.models;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Clase que representa la solicitud para crear o actualizar una fórmula médica.
 */
@Data
public class FormulaMedicaRq {

    /**
     * Identificador de la fórmula médica (necesario para actualizar).
     */
    private Long id;

    /**
     * Identificador de la cita asociada.
     */
    private Long citaId;

    /**
     * Identificador del medicamento recetado.
     */
    private Long medicamentoId;

    /**
     * Dosis del medicamento.
     */
    private String dosis;

    /**
     * Indicaciones para el paciente.
     */
    private String indicaciones;

    /**
     * Fecha de emisión de la fórmula.
     */
    private LocalDateTime fechaCreacionRegistro;
}