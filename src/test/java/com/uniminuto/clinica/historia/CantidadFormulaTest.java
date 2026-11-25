package com.uniminuto.clinica.historia;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Cálculo de la cantidad total requerida según la formulación (Req. 1.3). */
class CantidadFormulaTest {

    /** Dosis × tomas por día × días. */
    @Test
    void cadaDoceHorasPorSieteDias() {
        assertEquals(14, FormulaIntegracionService.calcularCantidad("1 tableta", "Cada 12 horas", "7 días"));
    }

    /** Las fracciones se redondean hacia arriba al total. */
    @Test
    void mediaTabletaCadaOchoHoras() {
        assertEquals(5, FormulaIntegracionService.calcularCantidad("1/2 tableta", "Cada 8 horas", "3 días"));
    }

    /** Semanas y veces al día. */
    @Test
    void dosVecesAlDiaPorDosSemanas() {
        assertEquals(28, FormulaIntegracionService.calcularCantidad("1 cápsula", "2 veces al día", "2 semanas"));
    }

    /** Frecuencia no reconocida: una toma diaria; nunca menos de 1 unidad. */
    @Test
    void frecuenciaLibre() {
        assertEquals(10, FormulaIntegracionService.calcularCantidad("2 ml", "Diaria", "5 días"));
        assertEquals(1, FormulaIntegracionService.calcularCantidad("0.1 ml", "Única", "1 día"));
    }
}
