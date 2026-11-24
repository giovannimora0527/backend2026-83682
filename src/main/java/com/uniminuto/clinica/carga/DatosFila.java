package com.uniminuto.clinica.carga;

import com.uniminuto.clinica.entity.enums.TipoProducto;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Valores ya convertidos de una fila. */
public record DatosFila(String codigo, String nombre, TipoProducto tipo, String descripcion, String presentacion,
                        String unidadMedida, boolean requiereInvima, String registroInvima, BigDecimal precioCompra,
                        BigDecimal precioVenta, BigDecimal porcentajeIva, Integer stockMinimo, String numeroLote,
                        Integer cantidad, LocalDate fechaCompra, LocalDate fechaVencimiento,
                        LocalDate fechaProximoPedido) {
}
