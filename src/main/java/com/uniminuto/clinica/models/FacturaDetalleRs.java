package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.FacturaDetalle;
import com.uniminuto.clinica.entity.enums.TipoItem;
import java.math.BigDecimal;

/** itemRef: SERVICIO-<servicioId> o MEDICAMENTO-<despachoDetalleId>. */
public record FacturaDetalleRs(String itemRef, TipoItem tipoItem, String descripcion, Integer cantidad,
                               BigDecimal precioUnitario, BigDecimal subtotal, BigDecimal porcentajeDescuento,
                               BigDecimal valorDescuento, String motivoDescuento, BigDecimal porcentajeIva,
                               BigDecimal valorIva, BigDecimal total) {

    public static FacturaDetalleRs de(FacturaDetalle d) {
        String ref = d.getTipoItem() == TipoItem.SERVICIO ? "SERVICIO-" + d.getServicio().getId()
                : "MEDICAMENTO-" + d.getDespachoDetalle().getId();
        return new FacturaDetalleRs(ref, d.getTipoItem(), d.getDescripcion(), d.getCantidad(), d.getPrecioUnitario(),
                d.getSubtotal(), d.getPorcentajeDescuento(), d.getValorDescuento(), d.getMotivoDescuento(),
                d.getPorcentajeIva(), d.getValorIva(), d.getTotal());
    }
}
