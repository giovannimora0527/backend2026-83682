package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.FormulaHistorialEstado;
import com.uniminuto.clinica.entity.enums.EstadoDespacho;
import com.uniminuto.clinica.entity.enums.EstadoFormula;
import java.time.LocalDateTime;

public record FormulaHistorialRs(Long id, EstadoDespacho estadoAnterior, EstadoDespacho estadoNuevo,
                                 EstadoFormula estadoFormulaAnterior, EstadoFormula estadoFormulaNuevo,
                                 String motivo, String usuario, LocalDateTime fecha) {

    public static FormulaHistorialRs de(FormulaHistorialEstado h) {
        return new FormulaHistorialRs(h.getId(), h.getEstadoDespachoAnterior(), h.getEstadoDespachoNuevo(),
                h.getEstadoFormulaAnterior(), h.getEstadoFormulaNuevo(), h.getMotivo(),
                h.getUsuario().getUsername(), h.getFecha());
    }
}
