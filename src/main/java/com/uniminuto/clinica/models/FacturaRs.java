package com.uniminuto.clinica.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.uniminuto.clinica.entity.Factura;
import com.uniminuto.clinica.entity.enums.EstadoFactura;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** detalles y pagos solo se incluyen al consultar una factura. */
public record FacturaRs(Long id, String numero, LocalDateTime fecha, Long citaId, Long clienteId,
                        String clienteNombre, String clienteDocumento, Integer mascotaId, String mascotaNombre,
                        String medicoNombre, String especialidadNombre, BigDecimal subtotal, BigDecimal descuentos,
                        BigDecimal iva, BigDecimal total, BigDecimal pagado, BigDecimal saldo, EstadoFactura estado,
                        String observacion, String usuario, LocalDateTime fechaAnulacion, String usuarioAnulacion,
                        String motivoAnulacion,
                        @JsonInclude(JsonInclude.Include.NON_NULL) List<FacturaDetalleRs> detalles,
                        @JsonInclude(JsonInclude.Include.NON_NULL) List<PagoRs> pagos) {

    public static FacturaRs de(Factura f) {
        return de(f, null, null);
    }

    public static FacturaRs de(Factura f, List<FacturaDetalleRs> detalles, List<PagoRs> pagos) {
        return new FacturaRs(f.getId(), f.getNumero(), f.getFechaGeneracion(), f.getCita().getId(),
                f.getCliente().getId(), f.getClienteNombre(), f.getClienteDocumento(),
                f.getMascota().getMascotaId(), f.getMascotaNombre(), f.getMedicoNombre(),
                f.getEspecialidadNombre(), f.getSubtotal(), f.getTotalDescuento(), f.getTotalImpuestos(),
                f.getTotal(), f.getTotalPagado(), f.getSaldo(), f.getEstado(), f.getObservacion(),
                f.getUsuario().getUsername(), f.getFechaAnulacion(),
                f.getUsuarioAnulacion() == null ? null : f.getUsuarioAnulacion().getUsername(),
                f.getMotivoAnulacion(), detalles, pagos);
    }
}
