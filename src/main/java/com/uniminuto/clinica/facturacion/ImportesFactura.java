package com.uniminuto.clinica.facturacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

final class ImportesFactura {
    private static final BigDecimal CERO = new BigDecimal("0.00");
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final BigDecimal MAXIMO = new BigDecimal("99999999999999999.99");

    record Importes(@com.fasterxml.jackson.annotation.JsonFormat(shape=com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING) BigDecimal subtotal,
            @com.fasterxml.jackson.annotation.JsonFormat(shape=com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING) BigDecimal descuento,
            @com.fasterxml.jackson.annotation.JsonFormat(shape=com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING) BigDecimal iva,
            @com.fasterxml.jackson.annotation.JsonFormat(shape=com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING) BigDecimal otros,
            @com.fasterxml.jackson.annotation.JsonFormat(shape=com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING) BigDecimal total) {}

    private ImportesFactura() {}

    static Importes linea(int cantidad, BigDecimal unitario, BigDecimal descuento,
            BigDecimal iva, BigDecimal otros) {
        if (cantidad <= 0 || unitario == null || unitario.signum() < 0
                || unitario.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Cantidad o precio unitario inválido.");
        }
        validarPorcentaje(descuento);
        validarPorcentaje(iva);
        validarPorcentaje(otros);
        if (iva.add(otros).compareTo(CIEN) > 0) {
            throw new IllegalArgumentException("Los impuestos superan el 100 %.");
        }
        BigDecimal subtotal = unitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2);
        BigDecimal rebaja = porcentaje(subtotal, descuento);
        BigDecimal base = subtotal.subtract(rebaja);
        BigDecimal valorIva = porcentaje(base, iva);
        BigDecimal valorOtros = porcentaje(base, otros);
        return verificarLimite(new Importes(subtotal, rebaja, valorIva, valorOtros, base.add(valorIva).add(valorOtros)));
    }

    static Importes sumar(List<Importes> lineas) {
        BigDecimal subtotal = CERO;
        BigDecimal descuento = CERO;
        BigDecimal iva = CERO;
        BigDecimal otros = CERO;
        BigDecimal total = CERO;
        for (var linea : lineas) {
            subtotal = subtotal.add(linea.subtotal());
            descuento = descuento.add(linea.descuento());
            iva = iva.add(linea.iva());
            otros = otros.add(linea.otros());
            total = total.add(linea.total());
        }
        return verificarLimite(new Importes(subtotal, descuento, iva, otros, total));
    }

    private static Importes verificarLimite(Importes importes) {
        if (importes.subtotal().max(importes.total()).compareTo(MAXIMO) > 0) {
            throw new IllegalArgumentException("El importe excede la capacidad de la factura.");
        }
        return importes;
    }

    private static BigDecimal porcentaje(BigDecimal base, BigDecimal tasa) {
        return base.multiply(tasa).movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
    }

    private static void validarPorcentaje(BigDecimal tasa) {
        if (tasa == null || tasa.signum() < 0 || tasa.compareTo(CIEN) > 0
                || tasa.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Porcentaje inválido.");
        }
    }
}
