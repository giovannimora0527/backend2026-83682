package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.EstadoCita;
import java.time.LocalDate;
import java.time.LocalTime;

public record CitaRequest(Long clienteId, Integer mascotaId, Long veterinarioId, LocalDate fecha,
                          LocalTime hora, String motivoConsulta, EstadoCita estado) { }
