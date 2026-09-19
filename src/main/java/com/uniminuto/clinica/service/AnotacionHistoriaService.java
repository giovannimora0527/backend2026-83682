package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import org.apache.coyote.BadRequestException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio encargado de definir las operaciones
 * relacionadas con las anotaciones de las historias médicas.
 */
public interface AnotacionHistoriaService {

    /**
     * Crea una nueva anotación de historia médica.
     *
     * @param anotacion información de la anotación.
     * @return anotación creada.
     */
    AnotacionHistoria crearAnotacion(
            AnotacionHistoria anotacion
    );

    /**
     * Busca las anotaciones comprendidas entre
     * una fecha inicial y una fecha final.
     *
     * @param fechaInicial fecha inicial de búsqueda.
     * @param fechaFinal fecha final de búsqueda.
     * @return lista de anotaciones encontradas.
     */
    List<AnotacionHistoria> buscarAnotacionesPorFechas(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal
    );

    /**
     * Actualiza una anotación existente.
     *
     * @param id identificador de la anotación.
     * @param anotacion información actualizada.
     * @return anotación actualizada.
     * @throws BadRequestException si la anotación no existe.
     */
    AnotacionHistoria actualizarAnotacion(
            Long id,
            AnotacionHistoria anotacion
    ) throws BadRequestException;
}