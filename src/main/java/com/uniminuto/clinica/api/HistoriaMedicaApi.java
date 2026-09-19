package com.uniminuto.clinica.api;

import com.uniminuto.clinica.dto.HistoriaMedicaDTO;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz que define el "contrato" del servicio REST de historias medicas.
 */
public interface HistoriaMedicaApi {

    /**
     * Crea una nueva historia medica.
     *
     * @param historiaMedicaDTO datos de la historia medica a crear
     * @return respuesta HTTP con la historia medica creada
     */
    ResponseEntity<HistoriaMedicaDTO> crearHistoriaMedica(HistoriaMedicaDTO historiaMedicaDTO);

    /**
     * Lista todas las historias medicas, o las filtra por un rango de
     * fechas si se envian los parametros fechaInicio y fechaFin.
     *
     * @param fechaInicio fecha inicial del rango (opcional)
     * @param fechaFin    fecha final del rango (opcional)
     * @return respuesta HTTP con la lista de historias medicas
     */
    ResponseEntity<List<HistoriaMedicaDTO>> listarHistoriasMedicas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Obtiene una historia medica por su id.
     *
     * @param id identificador de la historia medica
     * @return respuesta HTTP con la historia medica encontrada
     */
    ResponseEntity<HistoriaMedicaDTO> obtenerHistoriaMedicaPorId(Long id);

    /**
     * Actualiza una historia medica existente.
     *
     * @param id                 identificador de la historia medica a actualizar
     * @param historiaMedicaDTO  nuevos datos de la historia medica
     * @return respuesta HTTP con la historia medica actualizada
     */
    ResponseEntity<HistoriaMedicaDTO> actualizarHistoriaMedica(Long id, HistoriaMedicaDTO historiaMedicaDTO);

    /**
     * Elimina una historia medica del sistema.
     *
     * @param id identificador de la historia medica a eliminar
     * @return respuesta HTTP vacia confirmando la eliminacion
     */
    ResponseEntity<Void> eliminarHistoriaMedica(Long id);
}
