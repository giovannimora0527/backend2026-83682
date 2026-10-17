package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.EstadoCita;
import java.time.LocalDate;
import java.time.LocalTime;

public record CitaResponse(Long citaId, Long clienteId, String clienteNombre, Integer mascotaId,
                           String mascotaNombre, Long veterinarioId, String veterinarioNombre,
                           LocalDate fecha, LocalTime hora, String motivoConsulta, EstadoCita estado) { }
