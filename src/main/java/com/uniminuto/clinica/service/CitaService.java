package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.CitaRs;
import java.time.LocalDateTime;
import java.util.List;

/** Logica de negocio de las citas. */
public interface CitaService {

    /** Crea una cita nueva. */
    CitaRs crearCita(CitaRq citaRq);

    /** Actualiza una cita existente identificada por {@code citaRq.citaId}. */
    CitaRs actualizarCita(CitaRq citaRq);

    /** Citas dentro del rango de fechas, de la mas reciente a la mas antigua. */
    List<CitaRs> filtrarCitas(LocalDateTime fechaInicial, LocalDateTime fechaFinal);
}
