package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.CitaHistorialEstado;
import com.uniminuto.clinica.entity.enums.EstadoCita;
import java.time.LocalDateTime;

public record CitaHistorialRs(Long id, EstadoCita estadoAnterior, EstadoCita estadoNuevo,
                              LocalDateTime fechaInicioAnterior, LocalDateTime fechaInicioNueva, String motivo,
                              String usuario, LocalDateTime fecha) {

    public static CitaHistorialRs de(CitaHistorialEstado h) {
        return new CitaHistorialRs(h.getId(), h.getEstadoAnterior(), h.getEstadoNuevo(),
                h.getFechaInicioAnterior(), h.getFechaInicioNueva(), h.getMotivo(),
                h.getUsuario().getUsername(), h.getFecha());
    }
}
