package co.edu.uniminuto.veterinaria.service;

import co.edu.uniminuto.veterinaria.dto.HistoriaFiltro;
import co.edu.uniminuto.veterinaria.dto.HistoriaRequest;
import co.edu.uniminuto.veterinaria.dto.HistoriaResponse;
import co.edu.uniminuto.veterinaria.dto.PageResponse;

/**
 * Casos de uso de la gestión de historias clínicas.
 */
public interface HistoriaService {

    /**
     * Lista historias con filtros, orden y paginación (por defecto: fecha y hora descendente).
     *
     * @param filtro    criterios de filtrado
     * @param pagina    número de página (base 0)
     * @param tamano    registros por página
     * @param orden     columna por la que ordenar
     * @param direccion "asc" o "desc"
     * @return página de historias
     */
    PageResponse<HistoriaResponse> listar(HistoriaFiltro filtro, int pagina, int tamano, String orden,
                                          String direccion);

    /**
     * Consulta una historia clínica.
     *
     * @param id identificador de la historia
     * @return la historia
     */
    HistoriaResponse obtener(Long id);

    /**
     * Registra una historia clínica a partir de una cita atendida.
     *
     * @param request datos de la historia
     * @return la historia creada
     */
    HistoriaResponse crear(HistoriaRequest request);

    /**
     * Cierra una historia clínica (queda de solo lectura).
     *
     * @param id identificador de la historia
     * @return la historia cerrada
     */
    HistoriaResponse cerrar(Long id);
}
