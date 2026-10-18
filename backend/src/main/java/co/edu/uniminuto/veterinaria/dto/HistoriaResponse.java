package co.edu.uniminuto.veterinaria.dto;

import co.edu.uniminuto.veterinaria.entity.EstadoHistoria;

import java.time.LocalDateTime;

/**
 * Datos de una historia clínica expuestos por la API.
 *
 * @param id                identificador
 * @param fechaHora         fecha y hora de la consulta
 * @param clienteId         identificador del cliente
 * @param clienteNombre     nombre completo del cliente
 * @param mascotaId         identificador de la mascota
 * @param mascotaNombre     nombre de la mascota
 * @param citaId            identificador de la cita de origen
 * @param veterinarioId     identificador del veterinario
 * @param veterinarioNombre nombre completo del veterinario
 * @param motivo            motivo de consulta
 * @param diagnostico       diagnóstico
 * @param observaciones     observaciones
 * @param estado            estado de la historia
 */
public record HistoriaResponse(Long id, LocalDateTime fechaHora, Long clienteId, String clienteNombre,
                               Long mascotaId, String mascotaNombre, Long citaId, Long veterinarioId,
                               String veterinarioNombre, String motivo, String diagnostico, String observaciones,
                               EstadoHistoria estado) {
}
