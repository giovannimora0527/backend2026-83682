package co.edu.uniminuto.veterinaria.dto;

import java.time.LocalDate;

/**
 * Criterios de filtrado de las anotaciones de una historia (todos opcionales).
 *
 * @param texto         texto contenido en la descripción, observación o diagnóstico/evolución
 * @param veterinarioId identificador exacto del veterinario
 * @param desde         fecha inicial (inclusive)
 * @param hasta         fecha final (inclusive)
 */
public record AnotacionFiltro(String texto, Long veterinarioId, LocalDate desde, LocalDate hasta) {
}
