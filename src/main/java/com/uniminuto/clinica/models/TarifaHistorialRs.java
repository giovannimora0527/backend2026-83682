package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.TarifaHistorial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TarifaHistorialRs(Long id, Long tarifaId, Long servicioId, String servicioNombre,
                                BigDecimal valorAnterior, BigDecimal valorNuevo, BigDecimal ivaAnterior,
                                BigDecimal ivaNuevo, String motivo, String usuario, LocalDateTime fecha) {

    public static TarifaHistorialRs de(TarifaHistorial h) {
        return new TarifaHistorialRs(h.getId(), h.getTarifa().getId(), h.getServicio().getId(),
                h.getServicio().getNombre(), h.getValorAnterior(), h.getValorNuevo(),
                h.getPorcentajeIvaAnterior(), h.getPorcentajeIvaNuevo(), h.getMotivo(),
                h.getUsuario().getUsername(), h.getFecha());
    }
}
