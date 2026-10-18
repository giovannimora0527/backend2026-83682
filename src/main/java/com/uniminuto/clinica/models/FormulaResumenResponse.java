package com.uniminuto.clinica.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Resumen de una fórmula para el listado. Incluye los datos que muestra la tabla de Angular
 * ({@code nombreCliente}, {@code dosis} y {@code frecuencia}); las indicaciones solo van en el detalle.
 */
public record FormulaResumenResponse(Long formulaMedicaId, Integer mascotaId, String mascotaNombre,
        Long clienteId, String clienteNombre, Long veterinarioId, String veterinarioNombre,
        LocalDate fechaEmision, String medicamento, LocalDateTime fechaCreacion,
        BigDecimal dosis, String dosisTexto, String frecuencia, String nombreCliente) { }
