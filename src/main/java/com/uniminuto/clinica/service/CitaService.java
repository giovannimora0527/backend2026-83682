package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.enums.EstadoCita;
import com.uniminuto.clinica.models.CitaHistorialRs;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.Descarga;
import com.uniminuto.clinica.models.EstadoCitaRq;
import com.uniminuto.clinica.models.EventoCalendarioRs;
import com.uniminuto.clinica.models.ReprogramarRq;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CitaService {

    List<Cita> listarCitas();

    List<Cita> listarCitasRango(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<Cita> listarCitasPorMascota(Integer mascotaId);

    Cita obtenerCita(Long id);

    Cita crearCita(CitaRq citaRq);

    Cita actualizarCita(CitaRq citaRq);

    Cita reprogramar(Long id, ReprogramarRq rq);

    Cita cambiarEstado(Long id, EstadoCitaRq rq);

    List<CitaHistorialRs> historialEstados(Long id);

    /** Un MEDICO recibe siempre lo suyo, sin importar medicoId. */
    List<EventoCalendarioRs> calendario(String desde, String hasta, Long medicoId);

    Map<EstadoCita, Long> resumenEstados(String desde, String hasta, Long medicoId);

    Descarga reporteExcel(String desde, String hasta, Long medicoId);
}
