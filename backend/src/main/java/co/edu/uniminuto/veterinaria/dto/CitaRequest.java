package co.edu.uniminuto.veterinaria.dto;

import co.edu.uniminuto.veterinaria.entity.EstadoCita;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Datos para crear una cita.
 *
 * @param clienteId     cliente que solicita la cita
 * @param mascotaId     mascota (debe pertenecer al cliente)
 * @param veterinarioId veterinario asignado
 * @param fechaHora     fecha y hora de la cita (no puede ser pasada)
 * @param motivo        motivo de la consulta
 * @param estado        estado inicial (opcional, por defecto PENDIENTE)
 */
public record CitaRequest(
        @NotNull(message = "Seleccione un cliente.") Long clienteId,
        @NotNull(message = "Seleccione una mascota.") Long mascotaId,
        @NotNull(message = "Seleccione un veterinario.") Long veterinarioId,
        @NotNull(message = "Indique la fecha y la hora de la cita.") LocalDateTime fechaHora,
        @NotBlank(message = "Indique el motivo de la consulta.")
        @Size(max = 500, message = "El motivo no puede superar 500 caracteres.") String motivo,
        EstadoCita estado) {
}
