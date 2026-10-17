package com.uniminuto.clinica.api;

import com.uniminuto.clinica.dto.AnotacionHistoriaDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Interfaz que define el "contrato" del servicio REST de anotaciones de
 * historia medica.
 */
public interface AnotacionHistoriaApi {

    /**
     * Crea una nueva anotacion dentro de una historia medica.
     *
     * @param anotacionDTO datos de la anotacion a crear
     * @return respuesta HTTP con la anotacion creada
     */
    ResponseEntity<AnotacionHistoriaDTO> crearAnotacion(AnotacionHistoriaDTO anotacionDTO);

    /**
     * Lista las anotaciones de una historia medica especifica.
     *
     * @param historiaMedicaId identificador de la historia medica
     * @return respuesta HTTP con la lista de anotaciones
     */
    ResponseEntity<List<AnotacionHistoriaDTO>> listarAnotacionesPorHistoria(Long historiaMedicaId);

    /**
     * Actualiza una anotacion existente.
     *
     * @param id           identificador de la anotacion a actualizar
     * @param anotacionDTO nuevos datos de la anotacion
     * @return respuesta HTTP con la anotacion actualizada
     */
    ResponseEntity<AnotacionHistoriaDTO> actualizarAnotacion(Long id, AnotacionHistoriaDTO anotacionDTO);
}
