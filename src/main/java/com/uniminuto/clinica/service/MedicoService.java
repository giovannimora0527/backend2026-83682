package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicoService {

    List<Medico> listarMedicos();

    List<Medico> listarMedicosDisponiblesPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin)
            throws BadRequestException;

    MiRespuestaRS guardarMedico(MedicoRq medicoRq) throws BadRequestException;

    MiRespuestaRS actualizarMedico(MedicoRq medicoRq) throws BadRequestException;
}
