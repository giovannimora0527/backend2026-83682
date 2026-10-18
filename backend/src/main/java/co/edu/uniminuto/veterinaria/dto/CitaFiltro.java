package co.edu.uniminuto.veterinaria.dto;

import co.edu.uniminuto.veterinaria.entity.EstadoCita;

import java.time.LocalDate;

/**
 * Criterios de filtrado del listado de citas (todos opcionales).
 *
 * @param cliente       texto contenido en el nombre del cliente
 * @param mascota       texto contenido en el nombre de la mascota
 * @param clienteId     identificador exacto del cliente
 * @param mascotaId     identificador exacto de la mascota
 * @param veterinarioId identificador exacto del veterinario
 * @param estado        estado de la cita
 * @param desde         fecha inicial (inclusive)
 * @param hasta         fecha final (inclusive)
 * @param sinHistoria   si es {@code true}, solo citas que aún no tienen historia clínica
 */
public record CitaFiltro(String cliente, String mascota, Long clienteId, Long mascotaId, Long veterinarioId,
                         EstadoCita estado, LocalDate desde, LocalDate hasta, Boolean sinHistoria) {
}
