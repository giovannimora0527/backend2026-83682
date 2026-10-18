package co.edu.uniminuto.veterinaria.dto;

import java.time.LocalDate;

/**
 * Criterios de filtrado del listado de fórmulas (todos opcionales).
 *
 * @param mascota       texto contenido en el nombre de la mascota
 * @param cliente       texto contenido en el nombre del cliente
 * @param medicamento   texto contenido en el nombre del medicamento
 * @param mascotaId     identificador exacto de la mascota
 * @param veterinarioId identificador exacto del veterinario
 * @param activo        {@code true} solo vigentes, {@code false} solo inactivas
 * @param desde         fecha de emisión inicial (inclusive)
 * @param hasta         fecha de emisión final (inclusive)
 */
public record FormulaFiltro(String mascota, String cliente, String medicamento, Long mascotaId,
                            Long veterinarioId, Boolean activo, LocalDate desde, LocalDate hasta) {
}
