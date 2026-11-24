package com.uniminuto.clinica.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

/** Calculo de un renglon (5.8): subtotal, descuento, IVA sobre la base y total, a 2 decimales HALF_UP. */
public record RenglonCobro(String tipo, String descripcion, int cantidad, BigDecimal precioUnitario,
                           BigDecimal subtotal, BigDecimal porcentajeDescuento, BigDecimal descuento,
                           BigDecimal porcentajeIva, BigDecimal iva, BigDecimal total) {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    public static RenglonCobro de(String tipo, String descripcion, int cantidad, BigDecimal precioUnitario,
                                  BigDecimal porcentajeDescuento, BigDecimal porcentajeIva) {
        BigDecimal subtotal = redondear(precioUnitario.multiply(BigDecimal.valueOf(cantidad)));
        BigDecimal descuento = redondear(subtotal.multiply(porcentajeDescuento).divide(CIEN));
        BigDecimal base = subtotal.subtract(descuento);
        BigDecimal iva = redondear(base.multiply(porcentajeIva).divide(CIEN));
        return new RenglonCobro(tipo, descripcion, cantidad, precioUnitario, subtotal, porcentajeDescuento, descuento,
                porcentajeIva, iva, base.add(iva));
    }

    public static BigDecimal sumar(List<RenglonCobro> renglones, Function<RenglonCobro, BigDecimal> campo) {
        return renglones.stream().map(campo).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    private static BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
