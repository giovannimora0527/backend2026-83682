package com.uniminuto.clinica.api;

import com.uniminuto.clinica.dto.CitaDTO;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz que define el "contrato" del servicio REST de citas.
 */
public interface CitaApi {

    /**
     * Filtra las citas entre una fecha inicial y una fecha final.
     *
     * @param fechaInicio fecha y hora inicial del rango
     * @param fechaFin    fecha y hora final del rango
     * @return respuesta HTTP con la lista de citas encontradas
     */
    ResponseEntity<List<CitaDTO>> filtrarCitasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Crea una nueva cita.
     *
     * @param citaDTO datos de la cita a crear
     * @return respuesta HTTP con la cita creada
     */
    ResponseEntity<CitaDTO> crearCita(CitaDTO citaDTO);

    /**
     * Actualiza una cita existente.
     *
     * @param id      identificador de la cita a actualizar
     * @param citaDTO nuevos datos de la cita
     * @return respuesta HTTP con la cita actualizada
     */
    ResponseEntity<CitaDTO> actualizarCita(Long id, CitaDTO citaDTO);
}
