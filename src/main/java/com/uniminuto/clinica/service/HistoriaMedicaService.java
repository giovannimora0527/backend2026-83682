package com.uniminuto.clinica.service;

import com.uniminuto.clinica.dto.HistoriaMedicaDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz de servicio para la entidad HistoriaMedica.
 *
 * <p>Cubre el CRUD completo pedido en el Requerimiento 5 del taller:
 * crear, listar (con filtro opcional de fechas), obtener por id, actualizar
 * y eliminar una historia medica.</p>
 */
public interface HistoriaMedicaService {

    /**
     * Crea una nueva historia medica para un paciente.
     *
     * @param historiaMedicaDTO datos de la historia medica a crear
     * @return la historia medica creada, con su id asignado
     */
    HistoriaMedicaDTO crearHistoriaMedica(HistoriaMedicaDTO historiaMedicaDTO);

    /**
     * Lista todas las historias medicas registradas, ordenadas de la mas
     * reciente a la mas antigua.
     *
     * @return lista completa de historias medicas
     */
    List<HistoriaMedicaDTO> listarHistoriasMedicas();

    /**
     * Lista las historias medicas cuya fecha de creacion esta entre una
     * fecha inicial y una fecha final, ordenadas de la mas reciente a la
     * mas antigua (Requerimiento 5).
     *
     * @param fechaInicio fecha y hora inicial del rango (inclusive)
     * @param fechaFin    fecha y hora final del rango (inclusive)
     * @return lista de historias medicas dentro del rango, de mas reciente a mas antigua
     */
    List<HistoriaMedicaDTO> listarHistoriasMedicasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Busca una historia medica especifica por su id.
     *
     * @param id identificador de la historia medica
     * @return la historia medica encontrada
     */
    HistoriaMedicaDTO obtenerHistoriaMedicaPorId(Long id);

    /**
     * Actualiza los datos de una historia medica existente.
     *
     * @param id                 identificador de la historia medica a actualizar
     * @param historiaMedicaDTO  nuevos datos de la historia medica
     * @return la historia medica ya actualizada
     */
    HistoriaMedicaDTO actualizarHistoriaMedica(Long id, HistoriaMedicaDTO historiaMedicaDTO);

    /**
     * Elimina una historia medica del sistema.
     *
     * @param id identificador de la historia medica a eliminar
     */
    void eliminarHistoriaMedica(Long id);
}
