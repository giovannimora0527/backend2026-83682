package com.uniminuto.clinica.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FacturaPreviaRs(Long citaId, LocalDateTime fechaCita, Long clienteId, String clienteNombre,
                              String clienteDocumento, Integer mascotaId, String mascotaNombre, String medicoNombre,
                              String especialidadNombre, List<FacturaDetalleRs> detalles, BigDecimal subtotal,
                              BigDecimal descuentos, BigDecimal iva, BigDecimal total,
                              BigDecimal topeDescuento, List<String> advertencias) {
}
