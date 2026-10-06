package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaService {

    List<Cita> listarCitas() throws BadRequestException;

    List<Cita> listarCitasRango(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws BadRequestException;

    MiRespuestaRS crearCita(CitaRq citaRq) throws BadRequestException;

    MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException;
}
