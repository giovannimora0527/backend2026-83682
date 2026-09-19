package com.uniminuto.clinica.service;

import com.uniminuto.clinica.dto.CitaDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz de servicio para la entidad Cita.
 *
 * <p>Aqui se declaran los servicios pedidos en los requerimientos 2, 3 y 4
 * del taller: filtrar citas por rango de fechas, crear una cita nueva y
 * actualizar una cita existente.</p>
 */
public interface CitaService {

    /**
     * Filtra las citas del sistema que esten entre una fecha inicial y una
     * fecha final, y las devuelve ordenadas desde la mas reciente hasta la
     * mas antigua (Requerimientos 2 y 3).
     *
     * @param fechaInicio fecha y hora inicial del rango (inclusive)
     * @param fechaFin    fecha y hora final del rango (inclusive)
     * @return lista de citas dentro del rango, de la mas reciente a la mas antigua
     */
    List<CitaDTO> filtrarCitasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Crea una nueva cita en el sistema (Requerimiento 4).
     *
     * @param citaDTO datos de la cita a crear
     * @return la cita creada, incluyendo el id asignado por la base de datos
     */
    CitaDTO crearCita(CitaDTO citaDTO);

    /**
     * Actualiza una cita ya existente en el sistema (Requerimiento 4).
     *
     * @param id      identificador de la cita a actualizar
     * @param citaDTO nuevos datos de la cita
     * @return la cita ya actualizada
     */
    CitaDTO actualizarCita(Long id, CitaDTO citaDTO);
}
