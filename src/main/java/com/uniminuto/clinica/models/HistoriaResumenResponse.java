package com.uniminuto.clinica.models;

import java.time.LocalDateTime;

/** Campos mínimos de una historia para la tabla de resultados. */
public record HistoriaResumenResponse(Long historiaClinicaId, Long clienteId, String clienteNombre,
        Integer mascotaId, String mascotaNombre, Long veterinarioId, String veterinarioNombre,
        LocalDateTime fechaHora, String motivoConsulta) { }
