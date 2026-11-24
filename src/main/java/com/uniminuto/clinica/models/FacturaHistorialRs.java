package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.FacturaHistorialEstado;
import com.uniminuto.clinica.entity.enums.EstadoFactura;
import java.time.LocalDateTime;

public record FacturaHistorialRs(Long id, EstadoFactura estadoAnterior, EstadoFactura estadoNuevo, String motivo,
                                 String usuario, LocalDateTime fecha) {

    public static FacturaHistorialRs de(FacturaHistorialEstado h) {
        return new FacturaHistorialRs(h.getId(), h.getEstadoAnterior(), h.getEstadoNuevo(), h.getMotivo(),
                h.getUsuario().getUsername(), h.getFecha());
    }
}
