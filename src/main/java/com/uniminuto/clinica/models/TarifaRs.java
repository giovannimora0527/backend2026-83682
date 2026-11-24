package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.Servicio;
import com.uniminuto.clinica.entity.Tarifa;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TarifaRs(Long id, Long servicioId, String servicioCodigo, String servicioNombre,
                       String especializacionNombre, BigDecimal valor, Long impuestoId, String impuestoNombre,
                       BigDecimal porcentajeIva, LocalDate vigenteDesde, LocalDate vigenteHasta, Boolean activa) {

    public static TarifaRs de(Tarifa t) {
        Servicio s = t.getServicio();
        return new TarifaRs(t.getId(), s.getId(), s.getCodigo(), s.getNombre(),
                s.getEspecializacion() == null ? null : s.getEspecializacion().getNombre(), t.getValor(),
                t.getImpuesto().getId(), t.getImpuesto().getNombre(), t.getImpuesto().getPorcentaje(),
                t.getVigenteDesde(), t.getVigenteHasta(), t.getActiva());
    }
}
