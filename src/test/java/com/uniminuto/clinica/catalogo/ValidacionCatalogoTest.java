package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Pruebas de las validaciones de formato de razas y médicos. */
class ValidacionCatalogoTest {

    /** Datos válidos de un médico para modificar en cada prueba. */
    private static MedicoApi.MedicoRq medico(String tipo, String doc, String tel, String registro) {
        return new MedicoApi.MedicoRq(tipo, doc, "Ana  María", "Gómez", tel, registro, 1L, null, null);
    }

    /** Normaliza nombre y especie de la raza. */
    @Test
    void razaNormalizaMayusculaYEspacios() {
        assertArrayEquals(new String[]{"Golden retriever", "Canino"},
                RazaApiController.validar(new RazaApi.RazaRq("  golden   retriever ", "canino")));
    }

    /** Rechaza razas con números o símbolos. */
    @Test
    void razaRechazaNumeros() {
        assertThrows(BadRequestException.class, () -> RazaApiController.validar(new RazaApi.RazaRq("Raza 1", "Canino")));
        assertThrows(BadRequestException.class, () -> RazaApiController.validar(new RazaApi.RazaRq("Pug", "C")));
    }

    /** Acepta un médico válido y normaliza los campos. */
    @Test
    void medicoValido() {
        Map<String, Object> p = MedicoApiController.validarFormato(medico("cc", "1012345678", "3001234567", "rp-2020"));
        assertEquals("CC", p.get("tipo"));
        assertEquals("Ana María", p.get("nombres"));
        assertEquals("RP-2020", p.get("registro"));
        assertEquals(true, p.get("activo"));
    }

    /** Rechaza tipo de documento, cédula, teléfono y registro inválidos. */
    @Test
    void medicoInvalido() {
        assertThrows(BadRequestException.class, () -> MedicoApiController.validarFormato(medico("TI", "1012345678", null, "RP-1")));
        assertThrows(BadRequestException.class, () -> MedicoApiController.validarFormato(medico("CC", "12AB45", null, "RP-1")));
        assertThrows(BadRequestException.class, () -> MedicoApiController.validarFormato(medico("CC", "1012345678", "123", "RP-1")));
        assertThrows(BadRequestException.class, () -> MedicoApiController.validarFormato(medico("CC", "1012345678", null, "R!")));
    }
}
