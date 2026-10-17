package com.uniminuto.clinica.models;

import java.time.LocalDateTime;

public record HistoriaResponse(Long historiaClinicaId, Long clienteId, String clienteNombre,
                               Integer mascotaId, String mascotaNombre, Long citaId,
                               Long veterinarioId, String veterinarioNombre, LocalDateTime fechaHora,
                               String motivoConsulta, String diagnostico, String observaciones) { }
