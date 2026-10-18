package co.edu.uniminuto.veterinaria.dto;

import co.edu.uniminuto.veterinaria.entity.EstadoCita;

import java.time.LocalDateTime;

/**
 * Datos de una cita expuestos por la API.
 *
 * @param id              identificador
 * @param fechaHora       fecha y hora
 * @param clienteId       identificador del cliente
 * @param clienteNombre   nombre completo del cliente
 * @param mascotaId       identificador de la mascota
 * @param mascotaNombre   nombre de la mascota
 * @param veterinarioId   identificador del veterinario
 * @param veterinarioNombre nombre completo del veterinario
 * @param estado          estado de la cita
 * @param motivo          motivo de la consulta
 * @param tieneHistoria   si la cita ya generó una historia clínica
 */
public record CitaResponse(Long id, LocalDateTime fechaHora, Long clienteId, String clienteNombre,
                           Long mascotaId, String mascotaNombre, Long veterinarioId, String veterinarioNombre,
                           EstadoCita estado, String motivo, boolean tieneHistoria) {
}
