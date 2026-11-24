package com.uniminuto.clinica.facturacion;

import com.uniminuto.clinica.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LiquidadorTest {

    private static Liquidador.Concepto servicio(String precio, int cantidad, String iva, String otros) {
        return new Liquidador.Concepto(1L, 1L, null, "Servicio", cantidad, new BigDecimal(precio),
                new BigDecimal(iva), new BigDecimal(otros));
    }

    @Test
    void calculaSubtotalImpuestosYTotalSinDescuento() {
        Liquidador.Resultado r = Liquidador.liquidar(List.of(
                servicio("85000", 1, "0", "0"),
                servicio("45000", 2, "19", "0"),
                servicio("40000", 1, "19", "4")), BigDecimal.ZERO);
        assertEquals(new BigDecimal("215000.00"), r.subtotal());
        assertEquals(new BigDecimal("24700.00"), r.iva());     // 90000*19% + 40000*19%
        assertEquals(new BigDecimal("1600.00"), r.otros());    // 40000*4%
        assertEquals(new BigDecimal("241300.00"), r.total());
    }

    @Test
    void prorrateaElDescuentoSinPerderCentavos() {
        Liquidador.Resultado r = Liquidador.liquidar(List.of(
                servicio("100", 1, "0", "0"), servicio("100", 1, "0", "0"), servicio("100", 1, "0", "0")),
                new BigDecimal("100"));
        assertEquals(new BigDecimal("100.00"), r.descuento());
        BigDecimal suma = r.lineas().stream().map(Liquidador.Linea::descuento).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("100.00"), suma);
        assertEquals(new BigDecimal("200.00"), r.total());
    }

    @Test
    void elIvaSeCalculaSobreLaBaseConDescuento() {
        Liquidador.Resultado r = Liquidador.liquidar(List.of(servicio("1000", 1, "19", "0")), new BigDecimal("100"));
        assertEquals(new BigDecimal("171.00"), r.iva());
        assertEquals(new BigDecimal("1071.00"), r.total());
    }

    @Test
    void rechazaDescuentoMayorAlSubtotal() {
        assertThrows(BadRequestException.class,
                () -> Liquidador.liquidar(List.of(servicio("1000", 1, "0", "0")), new BigDecimal("1000.01")));
    }
}
