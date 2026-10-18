
package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.*;
import java.time.LocalDate;

/**
 * Define las operaciones disponibles para consultar, crear y actualizar
 * fórmulas médicas.
 */
public interface FormulaMedicaService {

    /**
     * Consulta las fórmulas médicas aplicando filtros opcionales y paginación.
     *
     * @param page índice de página, comenzando en cero
     * @param size cantidad máxima de resultados por página
     * @param sort campo utilizado para ordenar los resultados
     * @param direction dirección del ordenamiento
     * @param clienteId identificador opcional del cliente
     * @param mascotaId identificador opcional de la mascota
     * @param veterinarioId identificador opcional del veterinario
     * @param medicamento nombre o texto para buscar el medicamento
     * @param fecha fecha opcional de emisión de la fórmula
     * @return página con los resúmenes de las fórmulas encontradas
     */
    PageResponse<FormulaResumenResponse> listar(
            int page,
            int size,
            String sort,
            String direction,
            Long clienteId,
            Integer mascotaId,
            Long veterinarioId,
            String medicamento,
            LocalDate fecha
    );

    /**
     * Lista todas las fórmulas ordenadas por fecha de registro (servicio del parcial).
     *
     * @param ascendente {@code true} para orden ascendente, {@code false} para descendente
     * @return fórmulas ordenadas
     */
    java.util.List<FormulaResponse> listarOrdenadas(boolean ascendente);

    /**
     * Consulta una fórmula médica por su identificador.
     *
     * @param id identificador de la fórmula médica
     * @return detalle de la fórmula encontrada
     */
    FormulaResponse consultar(Long id);

    /**
     * Registra una nueva fórmula médica.
     *
     * @param request datos necesarios para crear la fórmula
     * @return fórmula médica creada
     */
    FormulaResponse crear(FormulaRequest request);

    /**
     * Actualiza los datos de una fórmula médica existente.
     *
     * @param id identificador de la fórmula que se actualizará
     * @param request nuevos datos de la fórmula médica
     * @return fórmula médica actualizada
     */
    FormulaResponse actualizar(Long id, FormulaRequest request);
}
