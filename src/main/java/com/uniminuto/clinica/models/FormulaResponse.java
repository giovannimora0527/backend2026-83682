package com.uniminuto.clinica.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FormulaResponse(Long formulaMedicaId, Integer mascotaId, String mascotaNombre,
                              Long clienteId, String clienteNombre, Long historiaClinicaId,
                              Long veterinarioId, String veterinarioNombre, LocalDate fechaEmision,
                              String medicamento, BigDecimal dosis, String frecuencia,
                              Integer duracionDias, String indicaciones, LocalDateTime fechaCreacion,
                              String dosisTexto) { }
