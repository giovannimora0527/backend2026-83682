package com.uniminuto.clinica.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Resumen de una fórmula; dosis e indicaciones solo se entregan en el detalle. */
public record FormulaResumenResponse(Long formulaMedicaId, Integer mascotaId, String mascotaNombre,
        Long clienteId, String clienteNombre, Long veterinarioId, String veterinarioNombre,
        LocalDate fechaEmision, String medicamento, LocalDateTime fechaCreacion) { }
