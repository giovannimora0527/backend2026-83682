package com.uniminuto.clinica.models;

import java.time.LocalDateTime;

/**
 * Datos de una historia clínica.
 *
 * @param estado estado calculado ("Con anotaciones" o "Sin anotaciones"); no es una columna de la base de datos
 */
public record HistoriaResponse(Long historiaClinicaId, Long clienteId, String clienteNombre,
                               Integer mascotaId, String mascotaNombre, Long citaId,
                               Long veterinarioId, String veterinarioNombre, LocalDateTime fechaHora,
                               String motivoConsulta, String diagnostico, String observaciones,
                               String estado) {

    /** Constructor compatible con la firma anterior, sin estado calculado. */
    public HistoriaResponse(Long historiaClinicaId, Long clienteId, String clienteNombre,
                            Integer mascotaId, String mascotaNombre, Long citaId,
                            Long veterinarioId, String veterinarioNombre, LocalDateTime fechaHora,
                            String motivoConsulta, String diagnostico, String observaciones) {
        this(historiaClinicaId, clienteId, clienteNombre, mascotaId, mascotaNombre, citaId, veterinarioId,
                veterinarioNombre, fechaHora, motivoConsulta, diagnostico, observaciones, null);
    }
}
