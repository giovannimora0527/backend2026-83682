package com.uniminuto.clinica.service;

import com.uniminuto.clinica.dto.AnotacionHistoriaDTO;

import java.util.List;

/**
 * Interfaz de servicio para la entidad AnotacionHistoria.
 *
 * <p>Segun el Requerimiento 5 del taller, para esta entidad solo se deben
 * implementar los servicios de crear, listar y actualizar (no se pide
 * eliminar).</p>
 */
public interface AnotacionHistoriaService {

    /**
     * Crea una nueva anotacion dentro de una historia medica.
     *
     * @param anotacionDTO datos de la anotacion a crear
     * @return la anotacion creada, con su id asignado
     */
    AnotacionHistoriaDTO crearAnotacion(AnotacionHistoriaDTO anotacionDTO);

    /**
     * Lista todas las anotaciones que pertenecen a una historia medica,
     * ordenadas de la mas reciente a la mas antigua.
     *
     * @param historiaMedicaId identificador de la historia medica
     * @return lista de anotaciones de esa historia medica
     */
    List<AnotacionHistoriaDTO> listarAnotacionesPorHistoria(Long historiaMedicaId);

    /**
     * Actualiza el contenido de una anotacion existente.
     *
     * @param id           identificador de la anotacion a actualizar
     * @param anotacionDTO nuevos datos de la anotacion
     * @return la anotacion ya actualizada
     */
    AnotacionHistoriaDTO actualizarAnotacion(Long id, AnotacionHistoriaDTO anotacionDTO);
}
