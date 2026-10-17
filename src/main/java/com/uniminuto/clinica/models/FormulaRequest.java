package com.uniminuto.clinica.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FormulaRequest(Integer mascotaId, Long historiaClinicaId, Long veterinarioId,
                            LocalDate fechaEmision, String medicamento, BigDecimal dosis,
                            String frecuencia, Integer duracionDias, String indicaciones,
                            Long citaId, Long medicamentoId) {
    public FormulaRequest(Integer mascotaId, Long historiaClinicaId, Long veterinarioId,
            LocalDate fechaEmision, String medicamento, BigDecimal dosis, String frecuencia,
            Integer duracionDias, String indicaciones) {
        this(mascotaId, historiaClinicaId, veterinarioId, fechaEmision, medicamento, dosis,
                frecuencia, duracionDias, indicaciones, null, null);
    }
}
