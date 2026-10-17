
package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.*;
import java.time.LocalDate;

/**
 * Define las operaciones disponibles para consultar y crear anotaciones
 * clínicas asociadas a las historias médicas de los pacientes.
 */
public interface AnotacionService {

    /**
     * Consulta las anotaciones clínicas de forma paginada y permite aplicar
     * filtros por historia clínica, veterinario y fecha.
     *
     * @param page índice de la página, comenzando en cero
     * @param size cantidad máxima de anotaciones por página
     * @param sort campo por el que se ordenarán los resultados
     * @param direction dirección del ordenamiento, ascendente o descendente
     * @param historiaClinicaId identificador opcional de la historia clínica
     * @param veterinarioId identificador opcional del veterinario
     * @param fecha fecha opcional para filtrar las anotaciones
     * @return página con las anotaciones que cumplen los filtros indicados
     */
    PageResponse<AnotacionResponse> listar(
            int page,
            int size,
            String sort,
            String direction,
            Long historiaClinicaId,
            Long veterinarioId,
            LocalDate fecha
    );

    /**
     * Consulta las anotaciones asociadas a una historia clínica específica,
     * permitiendo paginación y ordenamiento.
     *
     * @param historiaClinicaId identificador de la historia clínica
     * @param page índice de la página, comenzando en cero
     * @param size cantidad máxima de anotaciones por página
     * @param sort campo por el que se ordenarán los resultados
     * @param direction dirección del ordenamiento, ascendente o descendente
     * @return página con las anotaciones de la historia clínica
     */
    PageResponse<AnotacionResponse> listarHistoria(
            Long historiaClinicaId,
            int page,
            int size,
            String sort,
            String direction
    );

    /**
     * Busca una anotación clínica mediante su identificador.
     *
     * @param id identificador único de la anotación
     * @return información de la anotación consultada
     */
    AnotacionResponse consultar(Long id);

    /**
     * Registra una nueva anotación clínica con los datos recibidos.
     *
     * @param request información necesaria para crear la anotación
     * @return información de la anotación creada
     */
    AnotacionResponse crear(AnotacionRequest request);
}