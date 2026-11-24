package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.HorarioRq;
import com.uniminuto.clinica.models.HorarioRs;

import java.util.List;

public interface HorarioService {

    List<HorarioRs> listar(Long medicoId);

    /** Horarios del medico autenticado. */
    List<HorarioRs> misHorarios();

    List<HorarioRs> reemplazar(Long medicoId, List<HorarioRq> horarios);
}
