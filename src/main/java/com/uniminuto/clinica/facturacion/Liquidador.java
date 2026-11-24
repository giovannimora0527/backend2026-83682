package com.uniminuto.clinica.facturacion;

import com.uniminuto.clinica.exception.BadRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Cálculo de importes de factura y prefactura.
 *
 * <p>Por cada concepto: subtotal = precio × cantidad; el descuento global se prorratea según el peso del
 * subtotal de cada línea (la última línea absorbe la diferencia de redondeo); la base gravable es
 * subtotal − descuento; IVA y otros impuestos se calculan sobre la base con el porcentaje configurado
 * para el servicio (tarifa) o el producto (inventario). Todos los valores se redondean a 2 decimales.</p>
 */
public final class Liquidador {

    /** Constante 100 para porcentajes. */
    private static final BigDecimal CIEN = new BigDecimal("100");

    /** Concepto a liquidar: un servicio de la atención o un medicamento efectivamente dispensado. */
    public record Concepto(Long servicioId, Long tarifaId, Long movimientoId, String nombre, int cantidad,
                           BigDecimal precio, BigDecimal ivaPorcentaje, BigDecimal impuestoPorcentaje) {
    }

    /** Línea liquidada. */
    public record Linea(Concepto concepto, BigDecimal subtotal, BigDecimal descuento, BigDecimal iva,
                        BigDecimal otros, BigDecimal total) {
    }

    /** Resultado total. */
    public record Resultado(List<Linea> lineas, BigDecimal subtotal, BigDecimal descuento, BigDecimal iva,
                            BigDecimal otros, BigDecimal total) {
    }

    /** Clase de utilidades: no se instancia. */
    private Liquidador() {
    }

    /**
     * Calcula subtotal, descuentos, IVA y total de los conceptos.
     *
     * @param conceptos conceptos a facturar.
     * @param descuentoGlobal porcentaje de descuento global.
     * @return liquidación.
     */
    public static Resultado liquidar(List<Concepto> conceptos, BigDecimal descuentoGlobal) {
        BigDecimal descuento = escala(descuentoGlobal == null ? BigDecimal.ZERO : descuentoGlobal);
        if (descuento.signum() < 0) {
            throw new BadRequestException("El descuento no puede ser negativo.");
        }
        BigDecimal subtotal = BigDecimal.ZERO;
        List<BigDecimal> subtotales = new ArrayList<>();
        for (Concepto c : conceptos) {
            BigDecimal s = escala(c.precio().multiply(BigDecimal.valueOf(c.cantidad())));
            subtotales.add(s);
            subtotal = subtotal.add(s);
        }
        if (descuento.compareTo(subtotal) > 0) {
            throw new BadRequestException("El descuento no puede superar el subtotal (" + subtotal.toPlainString() + ").");
        }
        // Prorrateo: se trunca cada parte y el sobrante (centavos) se reparte en orden sin superar cada subtotal.
        List<BigDecimal> descuentos = new ArrayList<>();
        BigDecimal asignado = BigDecimal.ZERO;
        for (BigDecimal s : subtotales) {
            BigDecimal d = subtotal.signum() == 0 ? BigDecimal.ZERO
                    : descuento.multiply(s).divide(subtotal, 2, RoundingMode.DOWN);
            descuentos.add(d);
            asignado = asignado.add(d);
        }
        BigDecimal sobrante = descuento.subtract(asignado);
        for (int i = 0; i < descuentos.size() && sobrante.signum() > 0; i++) {
            BigDecimal capacidad = subtotales.get(i).subtract(descuentos.get(i));
            BigDecimal extra = capacidad.min(sobrante);
            descuentos.set(i, descuentos.get(i).add(extra));
            sobrante = sobrante.subtract(extra);
            asignado = asignado.add(extra);
        }
        List<Linea> lineas = new ArrayList<>();
        BigDecimal totalIva = BigDecimal.ZERO;
        BigDecimal totalOtros = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < conceptos.size(); i++) {
            Concepto c = conceptos.get(i);
            BigDecimal s = subtotales.get(i);
            BigDecimal d = descuentos.get(i);
            BigDecimal base = s.subtract(d);
            BigDecimal iva = escala(base.multiply(nz(c.ivaPorcentaje())).divide(CIEN, 2, RoundingMode.HALF_UP));
            BigDecimal otros = escala(base.multiply(nz(c.impuestoPorcentaje())).divide(CIEN, 2, RoundingMode.HALF_UP));
            BigDecimal t = base.add(iva).add(otros);
            lineas.add(new Linea(c, s, d, iva, otros, t));
            totalIva = totalIva.add(iva);
            totalOtros = totalOtros.add(otros);
            total = total.add(t);
        }
        return new Resultado(lineas, subtotal, asignado, totalIva, totalOtros, total);
    }

    /**
     * @param v valor.
     * @return valor redondeado a 2 decimales (HALF_UP).
     */
    static BigDecimal escala(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @param v valor.
     * @return el valor o cero si es nulo.
     */
    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
