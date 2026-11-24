package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.TarifaActualizarRq;
import com.uniminuto.clinica.models.TarifaCrearRq;
import com.uniminuto.clinica.models.TarifaHistorialRs;
import com.uniminuto.clinica.models.TarifaRs;
import java.time.LocalDate;
import java.util.List;

public interface TarifaService {

    List<TarifaRs> listar(Long servicioId, boolean soloVigentes);

    TarifaRs crear(TarifaCrearRq rq);

    /** Cierra la tarifa vigente y crea la nueva; devuelve la nueva. */
    TarifaRs actualizar(Long id, TarifaActualizarRq rq);

    /** Historial completo del servicio de la tarifa. */
    List<TarifaHistorialRs> historial(Long tarifaId);

    List<TarifaHistorialRs> historial(LocalDate desde, LocalDate hasta);
}
