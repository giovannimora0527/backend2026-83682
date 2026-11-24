package com.uniminuto.clinica.historia;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas de la regla "la dosis y la duración deben ser valores válidos". */
class ValidacionFormulaTest {

    @Test
    void aceptaCantidadesPositivas() {
        assertTrue(HistoriaService.contieneCantidadPositiva("1 tableta"));
        assertTrue(HistoriaService.contieneCantidadPositiva("2,5 ml"));
        assertTrue(HistoriaService.contieneCantidadPositiva("7 días"));
        assertTrue(HistoriaService.contieneCantidadPositiva("800 mg"));
    }

    @Test
    void rechazaTextoSinCantidadOCero() {
        assertFalse(HistoriaService.contieneCantidadPositiva("una pastilla"));
        assertFalse(HistoriaService.contieneCantidadPositiva("0 días"));
        assertFalse(HistoriaService.contieneCantidadPositiva("indefinido"));
    }
}
