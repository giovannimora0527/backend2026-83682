package com.uniminuto.clinica.models;

import java.time.LocalTime;

/** Se reemplaza la lista completa; el id que llegue se ignora. */
public record HorarioRq(Long id, Integer diaSemana, LocalTime horaInicio, LocalTime horaFin, Boolean activo) {
}
