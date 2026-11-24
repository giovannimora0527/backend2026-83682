package com.uniminuto.clinica.despacho;

import com.uniminuto.clinica.exception.ConflictException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeleccionFefoTest {

    /** Lotes vigentes en orden FEFO: A vence primero (5 und), B después (10 und). */
    private final List<Map<String, Object>> lotes = List.of(
            lote(1, "A", "2026-12-05", 5), lote(2, "B", "2027-09-01", 10));

    private static Map<String, Object> lote(long id, String numero, String vence, int cantidad) {
        Map<String, Object> l = new LinkedHashMap<>();
        l.put("id", id);
        l.put("numero", numero);
        l.put("fechaVence", LocalDate.parse(vence));
        l.put("cantidad", cantidad);
        return l;
    }

    @Test
    void aceptaTomarPrimeroElLoteQueVenceAntes() {
        assertDoesNotThrow(() -> DespachoService.validarSeleccion("Amoxicilina", 8, lotes, Map.of(1L, 5, 2L, 3), 1));
        assertDoesNotThrow(() -> DespachoService.validarSeleccion("Amoxicilina", 8, lotes, Map.of(1L, 4), 1));
    }

    @Test
    void rechazaSaltarseElLoteMasProximoAVencer() {
        ConflictException e = assertThrows(ConflictException.class,
                () -> DespachoService.validarSeleccion("Amoxicilina", 8, lotes, Map.of(2L, 3), 1));
        assertTrue(e.getMessage().contains("FEFO"));
    }

    @Test
    void rechazaCantidadSuperiorALaExistenciaDelLote() {
        assertThrows(ConflictException.class,
                () -> DespachoService.validarSeleccion("Amoxicilina", 20, lotes, Map.of(1L, 6), 1));
    }

    @Test
    void rechazaEntregarMasDeLoPendiente() {
        assertThrows(ConflictException.class,
                () -> DespachoService.validarSeleccion("Amoxicilina", 4, lotes, Map.of(1L, 5), 1));
    }

    @Test
    void rechazaLoteVencidoONoListado() {
        assertThrows(ConflictException.class,
                () -> DespachoService.validarSeleccion("Amoxicilina", 4, lotes, Map.of(99L, 1), 1));
    }
}
