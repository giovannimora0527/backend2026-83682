package co.edu.uniminuto.veterinaria.dto;

import java.time.LocalDateTime;

/**
 * Datos de una anotación de historia clínica expuestos por la API.
 *
 * @param id                  identificador
 * @param historiaId          identificador de la historia
 * @param fechaHora           fecha y hora de la anotación
 * @param veterinarioId       identificador del veterinario
 * @param veterinarioNombre   nombre completo del veterinario
 * @param descripcion         descripción
 * @param observacion         observación
 * @param diagnosticoEvolucion diagnóstico o evolución
 */
public record AnotacionResponse(Long id, Long historiaId, LocalDateTime fechaHora, Long veterinarioId,
                                String veterinarioNombre, String descripcion, String observacion,
                                String diagnosticoEvolucion) {
}
