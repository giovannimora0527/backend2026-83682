package co.edu.uniminuto.veterinaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Datos para registrar una historia clínica.
 *
 * @param clienteId     cliente dueño de la mascota
 * @param mascotaId     mascota (debe pertenecer al cliente)
 * @param citaId        cita atendida que origina la historia (debe ser de esa mascota)
 * @param veterinarioId veterinario que realizó la consulta
 * @param fechaHora     fecha y hora de la consulta (no puede ser futura)
 * @param motivo        motivo de consulta
 * @param diagnostico   diagnóstico
 * @param observaciones observaciones (opcional)
 */
public record HistoriaRequest(
        @NotNull(message = "Seleccione un cliente.") Long clienteId,
        @NotNull(message = "Seleccione una mascota.") Long mascotaId,
        @NotNull(message = "Seleccione la cita atendida.") Long citaId,
        @NotNull(message = "Seleccione un veterinario.") Long veterinarioId,
        @NotNull(message = "Indique la fecha y la hora de la consulta.") LocalDateTime fechaHora,
        @NotBlank(message = "Indique el motivo de consulta.")
        @Size(max = 500, message = "El motivo no puede superar 500 caracteres.") String motivo,
        @NotBlank(message = "Indique el diagnóstico.")
        @Size(max = 2000, message = "El diagnóstico no puede superar 2000 caracteres.") String diagnostico,
        @Size(max = 2000, message = "Las observaciones no pueden superar 2000 caracteres.") String observaciones) {
}
