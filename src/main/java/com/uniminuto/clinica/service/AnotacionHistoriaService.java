package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface AnotacionHistoriaService {

    // Listar las anotaciones
    List<AnotacionHistoria> listarAnotaciones()
            throws BadRequestException;

    // Crear una nueva anotación
    MiRespuestaRS guardarAnotacion(
            AnotacionHistoriaRq anotacionHistoriaRq
    ) throws BadRequestException;

    // Actualizar una anotación existente
    MiRespuestaRS actualizarAnotacion(
            AnotacionHistoriaRq anotacionHistoriaRq
    ) throws BadRequestException;
}
