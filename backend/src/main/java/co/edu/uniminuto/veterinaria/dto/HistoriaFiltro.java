package co.edu.uniminuto.veterinaria.dto;

import co.edu.uniminuto.veterinaria.entity.EstadoHistoria;

import java.time.LocalDate;

/**
 * Criterios de filtrado del listado de historias clínicas (todos opcionales).
 *
 * @param cliente       texto contenido en el nombre del cliente
 * @param mascota       texto contenido en el nombre de la mascota
 * @param motivo        texto contenido en el motivo de consulta
 * @param clienteId     identificador exacto del cliente
 * @param mascotaId     identificador exacto de la mascota
 * @param veterinarioId identificador exacto del veterinario
 * @param estado        estado de la historia
 * @param desde         fecha inicial (inclusive)
 * @param hasta         fecha final (inclusive)
 */
public record HistoriaFiltro(String cliente, String mascota, String motivo, Long clienteId, Long mascotaId,
                             Long veterinarioId, EstadoHistoria estado, LocalDate desde, LocalDate hasta) {
}
