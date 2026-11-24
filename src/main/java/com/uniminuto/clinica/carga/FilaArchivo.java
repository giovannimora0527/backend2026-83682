package com.uniminuto.clinica.carga;

import java.util.Map;

/** Fila tal como llego; numero es el que se ve en el archivo (el encabezado es la 1). */
public record FilaArchivo(int numero, Map<String, String> datos) {
}
