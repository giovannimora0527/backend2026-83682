package com.uniminuto.clinica.inventario;

import com.uniminuto.clinica.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LectorArchivoTest {

    @Test
    void leeCsvConComillasYSeparadorPuntoYComa() {
        List<List<String>> filas = LectorArchivo.csv("a;b;c\r\n\"x;1\";\"dice \"\"hola\"\"\";3\n");
        assertEquals(2, filas.size());
        assertEquals(List.of("x;1", "dice \"hola\"", "3"), filas.get(1));
    }

    @Test
    void validaEncabezados() {
        byte[] archivo = "codigo,nombre\nX,Y\n".getBytes(StandardCharsets.UTF_8);
        BadRequestException e = assertThrows(BadRequestException.class,
                () -> LectorArchivo.leer("carga.csv", archivo, ImportacionService.COLUMNAS));
        assertTrue(e.getMessage().contains("Faltan columnas"));
    }

    @Test
    void ignoraFilasVaciasYConservaNumeroDeFila() {
        String encabezado = String.join(",", ImportacionService.COLUMNAS);
        byte[] archivo = (encabezado + "\n\nMED00099,Producto,,,MEDICAMENTO,INV,1,2,0,2026-01-01,2026-01-01,,2027-01-01,true,L1,5\n")
                .getBytes(StandardCharsets.UTF_8);
        List<LectorArchivo.Fila> filas = LectorArchivo.leer("carga.csv", archivo, ImportacionService.COLUMNAS);
        assertEquals(1, filas.size());
        assertEquals(3, filas.get(0).numero());
        assertEquals("MED00099", filas.get(0).valores().get("codigo"));
        assertEquals("5", filas.get(0).valores().get("cantidad"));
    }

    @Test
    void rechazaFormatoNoSoportado() {
        assertThrows(BadRequestException.class, () -> LectorArchivo.leer("carga.txt", new byte[]{1}, ImportacionService.COLUMNAS));
    }
}
