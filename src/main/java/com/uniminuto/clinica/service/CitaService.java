
package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.EstadoCita;
import com.uniminuto.clinica.models.*;

/**
 * Define las operaciones para consultar, crear y actualizar citas
 * de la clínica veterinaria.
 */
public interface CitaService {

    /**
     * Consulta las citas de forma paginada, aplicando los filtros opcionales
     * y el ordenamiento solicitado.
     *
     * @param page índice de página, comenzando en cero
     * @param size cantidad máxima de citas por página
     * @param sort campo por el que se ordenarán los resultados
     * @param direction dirección del ordenamiento, ascendente o descendente
     * @param clienteId identificador opcional del cliente
     * @param mascotaId identificador opcional de la mascota
     * @param veterinarioId identificador opcional del veterinario
     * @param fecha fecha opcional para filtrar las citas
     * @param estado estado opcional de la cita
     * @return página de citas que coincide con los filtros indicados
     */
    PageResponse<CitaResponse> listar(
            int page,
            int size,
            String sort,
            String direction,
            Long clienteId,
            Integer mascotaId,
            Long veterinarioId,
            java.time.LocalDate fecha,
            EstadoCita estado
    );

    /**
     * Busca una cita mediante su identificador.
     *
     * @param id identificador único de la cita
     * @return información de la cita consultada
     */
    CitaResponse consultar(Long id);

    /**
     * Lista las citas entre dos fechas, de la más reciente a la más antigua (servicio del parcial).
     *
     * @param fechaInicio fecha y hora inicial
     * @param fechaFinal fecha y hora final
     * @return citas encontradas
     */
    java.util.List<CitaResponse> filtrarPorFechas(java.time.LocalDateTime fechaInicio,
                                                  java.time.LocalDateTime fechaFinal);

    /**
     * Registra una nueva cita con los datos proporcionados.
     *
     * @param request información necesaria para crear la cita
     * @return información de la cita creada
     */
    CitaResponse crear(CitaRequest request);

    /**
     * Modifica los datos de una cita existente.
     *
     * @param id identificador de la cita que se actualizará
     * @param request nuevos datos de la cita
     * @return información de la cita actualizada
     */
    CitaResponse actualizar(Long id, CitaRequest request);
}
