package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.Factura;
import com.uniminuto.clinica.entity.enums.EstadoFactura;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FacturaRs(Long id, String numero, LocalDateTime fecha, Long citaId, Long clienteId,
                        String clienteNombre, String clienteDocumento, Integer mascotaId, String mascotaNombre,
                        String medicoNombre, String especialidadNombre, BigDecimal subtotal, BigDecimal descuentos,
                        BigDecimal iva, BigDecimal total, BigDecimal pagado, BigDecimal saldo,
                        EstadoFactura estado, String observacion) {

    public static FacturaRs de(Factura f) {
        return new FacturaRs(f.getId(), f.getNumero(), f.getFechaGeneracion(), f.getCita().getId(),
                f.getCliente().getId(), f.getClienteNombre(), f.getClienteDocumento(),
                f.getMascota().getMascotaId(), f.getMascotaNombre(), f.getMedicoNombre(),
                f.getEspecialidadNombre(), f.getSubtotal(), f.getTotalDescuento(), f.getTotalImpuestos(),
                f.getTotal(), f.getTotalPagado(), f.getSaldo(), f.getEstado(), f.getObservacion());
    }
}
