package co.edu.uniminuto.veterinaria.service;

import co.edu.uniminuto.veterinaria.dto.CitaActualizarRequest;
import co.edu.uniminuto.veterinaria.dto.CitaFiltro;
import co.edu.uniminuto.veterinaria.dto.CitaRequest;
import co.edu.uniminuto.veterinaria.dto.CitaResponse;
import co.edu.uniminuto.veterinaria.dto.PageResponse;

/**
 * Casos de uso de la gestión de citas médicas (reglas de negocio 1 a 10 del parcial).
 */
public interface CitaService {

    /**
     * Lista citas con filtros, orden y paginación (por defecto: fecha y hora descendente).
     *
     * @param filtro    criterios de filtrado
     * @param pagina    número de página (base 0)
     * @param tamano    registros por página
     * @param orden     columna por la que ordenar
     * @param direccion "asc" o "desc"
     * @return página de citas
     */
    PageResponse<CitaResponse> listar(CitaFiltro filtro, int pagina, int tamano, String orden, String direccion);

    /**
     * Consulta una cita.
     *
     * @param id identificador de la cita
     * @return la cita
     */
    CitaResponse obtener(Long id);

    /**
     * Crea una cita validando cliente, mascota, veterinario, fecha futura y cruces de horario.
     *
     * @param request datos de la nueva cita
     * @return la cita creada
     */
    CitaResponse crear(CitaRequest request);

    /**
     * Modifica una cita respetando las restricciones según su estado.
     *
     * @param id      identificador de la cita
     * @param request nuevos datos
     * @return la cita actualizada
     */
    CitaResponse actualizar(Long id, CitaActualizarRequest request);

    /**
     * Reactiva (operación explícita) una cita cancelada, dejándola en estado PENDIENTE.
     *
     * @param id identificador de la cita
     * @return la cita reactivada
     */
    CitaResponse reactivar(Long id);
}
