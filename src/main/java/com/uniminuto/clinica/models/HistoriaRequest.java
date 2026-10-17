package com.uniminuto.clinica.models;

import java.time.LocalDateTime;

public record HistoriaRequest(Long clienteId, Integer mascotaId, Long citaId, Long veterinarioId,
                              LocalDateTime fechaHora, String motivoConsulta, String diagnostico,
                              String observaciones) { }
