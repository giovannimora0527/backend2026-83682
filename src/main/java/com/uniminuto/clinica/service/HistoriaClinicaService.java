
package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.*;
import java.time.LocalDate;

/**
 * Define las operaciones para consultar y crear historias clínicas.
 */
public interface HistoriaClinicaService {

    /**
     * Consulta las historias clínicas aplicando filtros opcionales y paginación.
     *
     * @param page índice de página, comenzando en cero
     * @param size cantidad máxima de resultados por página
     * @param sort campo utilizado para ordenar los resultados
     * @param direction dirección del ordenamiento
     * @param clienteId identificador opcional del cliente
     * @param mascotaId identificador opcional de la mascota
     * @param veterinarioId identificador opcional del veterinario asociado
     *                     a las anotaciones de la historia clínica
     * @param fecha fecha opcional de la historia clínica
     * @return página con los resúmenes de las historias encontradas
     */
    PageResponse<HistoriaResumenResponse> listar(
            int page,
            int size,
            String sort,
            String direction,
            Long clienteId,
            Integer mascotaId,
            Long veterinarioId,
            LocalDate fecha
    );

    /**
     * Consulta una historia clínica por su identificador.
     *
     * @param id identificador de la historia clínica
     * @return historia clínica encontrada
     */
    HistoriaResponse consultar(Long id);

    /**
     * Crea una nueva historia clínica.
     *
     * @param request datos necesarios para registrar la historia clínica
     * @return historia clínica creada
     */
    HistoriaResponse crear(HistoriaRequest request);
}
