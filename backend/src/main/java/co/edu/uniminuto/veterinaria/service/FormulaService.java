package co.edu.uniminuto.veterinaria.service;

import co.edu.uniminuto.veterinaria.dto.FormulaActualizarRequest;
import co.edu.uniminuto.veterinaria.dto.FormulaDetalleResponse;
import co.edu.uniminuto.veterinaria.dto.FormulaFiltro;
import co.edu.uniminuto.veterinaria.dto.FormulaRequest;
import co.edu.uniminuto.veterinaria.dto.FormulaResumenResponse;
import co.edu.uniminuto.veterinaria.dto.PageResponse;

/**
 * Casos de uso de la gestión de fórmulas médicas. Las fórmulas no se eliminan: se inactivan.
 */
public interface FormulaService {

    /**
     * Lista fórmulas (vista resumida) con filtros, orden y paginación (por defecto: fecha de creación descendente).
     *
     * @param filtro    criterios de filtrado
     * @param pagina    número de página (base 0)
     * @param tamano    registros por página
     * @param orden     columna por la que ordenar
     * @param direccion "asc" o "desc"
     * @return página de fórmulas resumidas
     */
    PageResponse<FormulaResumenResponse> listar(FormulaFiltro filtro, int pagina, int tamano, String orden,
                                                String direccion);

    /**
     * Consulta el detalle completo de una fórmula.
     *
     * @param id identificador de la fórmula
     * @return detalle de la fórmula
     */
    FormulaDetalleResponse obtener(Long id);

    /**
     * Registra una fórmula validando mascota, historia, veterinario, fechas, dosis y medicamento.
     *
     * @param request datos de la fórmula
     * @return detalle de la fórmula creada
     */
    FormulaDetalleResponse crear(FormulaRequest request);

    /**
     * Modifica los datos permitidos de una fórmula activa.
     *
     * @param id      identificador de la fórmula
     * @param request nuevos datos
     * @return detalle de la fórmula actualizada
     */
    FormulaDetalleResponse actualizar(Long id, FormulaActualizarRequest request);

    /**
     * Inactiva (anula) una fórmula en lugar de eliminarla físicamente.
     *
     * @param id identificador de la fórmula
     * @return detalle de la fórmula inactiva
     */
    FormulaDetalleResponse inactivar(Long id);

    /**
     * Vuelve a activar una fórmula inactiva.
     *
     * @param id identificador de la fórmula
     * @return detalle de la fórmula activa
     */
    FormulaDetalleResponse activar(Long id);
}
