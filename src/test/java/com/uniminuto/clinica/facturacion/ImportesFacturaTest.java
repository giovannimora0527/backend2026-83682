package com.uniminuto.clinica.facturacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ImportesFacturaTest {
    @Test
    void aplicaElDescuentoAntesDeRedondearCadaImpuestoPorLinea() {
        var linea = ImportesFactura.linea(3, decimal("10.05"), decimal("10"), decimal("19"), decimal("2"));
        assertEquals(new ImportesFactura.Importes(decimal("30.15"), decimal("3.02"), decimal("5.15"),
                decimal("0.54"), decimal("32.82")), linea);
    }

    @Test
    void sumaLineasRedondeadasYAdmiteTotalCero() {
        var linea = ImportesFactura.linea(1, decimal("0.05"), decimal("10"), decimal("19"), decimal("0"));
        assertEquals(decimal("0.10"), ImportesFactura.sumar(List.of(linea, linea)).total());
        var gratis = ImportesFactura.linea(2, decimal("1.00"), decimal("100"), decimal("19"), decimal("0"));
        assertEquals(decimal("0.00"), gratis.total());
    }

    @Test
    void rechazaCantidadesYPorcentajesInvalidos() {
        assertThrows(IllegalArgumentException.class,
                () -> ImportesFactura.linea(0, decimal("1"), decimal("0"), decimal("0"), decimal("0")));
        assertThrows(IllegalArgumentException.class,
                () -> ImportesFactura.linea(1, decimal("1"), decimal("0"), decimal("90"), decimal("11")));
        assertThrows(IllegalArgumentException.class,
                () -> ImportesFactura.linea(1, decimal("1"), decimal("1.001"), decimal("0"), decimal("0")));
    }

    private static BigDecimal decimal(String valor) {
        return new BigDecimal(valor);
    }
}
