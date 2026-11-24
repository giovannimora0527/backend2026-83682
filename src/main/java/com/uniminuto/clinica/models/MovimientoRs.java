package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.entity.enums.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** saldo es el del producto; saldoLote, el del lote. */
public record MovimientoRs(Long id, LocalDateTime fecha, TipoMovimiento tipo, Long medicamentoId,
                           String medicamentoCodigo, String medicamentoNombre, Long loteId, String numeroLote,
                           Integer cantidad, Integer saldoLote, Integer saldo, BigDecimal costoUnitario,
                           String usuario, Long formulaId, Long despachoId, Long cargaId,
                           Long movimientoRevertidoId, String referencia, String motivo) {

    public static MovimientoRs de(MovimientoInventario m) {
        String referencia = null;
        if (m.getDespacho() != null) {
            referencia = m.getDespacho().getNumero()
                    + (m.getFormula() == null ? "" : " / " + m.getFormula().getNumero());
        } else if (m.getCarga() != null) {
            referencia = "Carga " + m.getCarga().getId();
        }
        return new MovimientoRs(m.getId(), m.getFecha(), m.getTipo(), m.getMedicamento().getId(),
                m.getMedicamento().getCodigo(), m.getMedicamento().getNombre(), m.getLote().getId(),
                m.getLote().getNumeroLote(), m.getCantidad(), m.getSaldoLoteResultante(),
                m.getSaldoProductoResultante(), m.getCostoUnitario(), m.getUsuario().getUsername(),
                m.getFormula() == null ? null : m.getFormula().getId(),
                m.getDespacho() == null ? null : m.getDespacho().getId(),
                m.getCarga() == null ? null : m.getCarga().getId(),
                m.getMovimientoRevertido() == null ? null : m.getMovimientoRevertido().getId(),
                referencia, m.getObservacion());
    }
}
