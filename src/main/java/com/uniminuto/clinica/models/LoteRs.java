package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.LoteMedicamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record LoteRs(Long id, String numeroLote, BigDecimal precioCompra, Integer cantidadInicial,
                     Integer cantidadDisponible, LocalDate fechaCompra, LocalDate fechaIngreso,
                     LocalDate fechaVencimiento, boolean vencido, Long diasParaVencer) {

    public static LoteRs de(LoteMedicamento l, LocalDate hoy) {
        LocalDate vence = l.getFechaVencimiento();
        return new LoteRs(l.getId(), l.getNumeroLote(), l.getPrecioCompra(), l.getCantidadInicial(),
                l.getCantidadDisponible(), l.getFechaCompra(), l.getFechaIngreso(), vence,
                vence != null && vence.isBefore(hoy), vence == null ? null : ChronoUnit.DAYS.between(hoy, vence));
    }
}
