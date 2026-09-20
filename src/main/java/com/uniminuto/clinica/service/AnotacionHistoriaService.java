package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;
import java.time.LocalDateTime;
import java.util.List;

/** Logica de negocio de las anotaciones de historia medica. */
public interface AnotacionHistoriaService {

    /** Crea una anotacion nueva asociada a una historia medica existente. */
    AnotacionHistoriaRs crearAnotacion(AnotacionHistoriaRq anotacionRq);

    /** Actualiza una anotacion existente identificada por su id. */
    AnotacionHistoriaRs actualizarAnotacion(AnotacionHistoriaRq anotacionRq);

    /** Todas las anotaciones, de la mas reciente a la mas antigua. */
    List<AnotacionHistoriaRs> listarAnotaciones();

    /** Anotaciones dentro del rango de fechas, de la mas reciente a la mas antigua. */
    List<AnotacionHistoriaRs> filtrarAnotaciones(LocalDateTime fechaInicial, LocalDateTime fechaFinal);
}
