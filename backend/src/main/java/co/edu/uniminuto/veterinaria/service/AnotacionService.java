package co.edu.uniminuto.veterinaria.service;

import co.edu.uniminuto.veterinaria.dto.AnotacionFiltro;
import co.edu.uniminuto.veterinaria.dto.AnotacionRequest;
import co.edu.uniminuto.veterinaria.dto.AnotacionResponse;
import co.edu.uniminuto.veterinaria.dto.PageResponse;

/**
 * Casos de uso de las anotaciones de una historia clínica.
 */
public interface AnotacionService {

    /**
     * Lista las anotaciones de una historia (por defecto de la más reciente a la más antigua).
     *
     * @param historiaId identificador de la historia
     * @param filtro     criterios de filtrado
     * @param pagina     número de página (base 0)
     * @param tamano     registros por página
     * @param orden      columna por la que ordenar
     * @param direccion  "asc" o "desc"
     * @return página de anotaciones
     */
    PageResponse<AnotacionResponse> listar(Long historiaId, AnotacionFiltro filtro, int pagina, int tamano,
                                           String orden, String direccion);

    /**
     * Consulta el detalle de una anotación.
     *
     * @param historiaId   identificador de la historia
     * @param anotacionId  identificador de la anotación
     * @return la anotación
     */
    AnotacionResponse obtener(Long historiaId, Long anotacionId);

    /**
     * Agrega una anotación a una historia abierta.
     *
     * @param historiaId identificador de la historia
     * @param request    datos de la anotación
     * @return la anotación creada
     */
    AnotacionResponse crear(Long historiaId, AnotacionRequest request);
}
