package com.uniminuto.clinica.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uniminuto.clinica.entity.HorarioMedico;

import java.time.LocalTime;

/** diaSemana: 1 = lunes ... 7 = domingo. */
public record HorarioRs(Long id, Integer diaSemana, @JsonFormat(pattern = "HH:mm") LocalTime horaInicio,
                        @JsonFormat(pattern = "HH:mm") LocalTime horaFin, Boolean activo) {

    public static HorarioRs de(HorarioMedico h) {
        return new HorarioRs(h.getId(), h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin(), h.getActivo());
    }
}
