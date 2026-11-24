package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.Pago;
import com.uniminuto.clinica.entity.enums.MedioPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoRs(Long id, LocalDateTime fecha, BigDecimal valor, MedioPago medioPago, String referencia,
                     String usuario, Boolean anulado) {

    public static PagoRs de(Pago p) {
        return new PagoRs(p.getId(), p.getFecha(), p.getValor(), p.getMedioPago(), p.getReferencia(),
                p.getUsuario().getUsername(), p.getAnulado());
    }
}
