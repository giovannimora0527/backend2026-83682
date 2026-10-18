package co.edu.uniminuto.veterinaria.dto;

import co.edu.uniminuto.veterinaria.entity.EstadoCita;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Datos editables de una cita. El cliente y la mascota no se pueden cambiar una vez creada la cita,
 * porque de ellas dependen las historias clínicas y fórmulas.
 *
 * @param veterinarioId veterinario asignado
 * @param fechaHora     nueva fecha y hora
 * @param motivo        motivo de la consulta
 * @param estado        nuevo estado (se valida la transición)
 */
public record CitaActualizarRequest(
        @NotNull(message = "Seleccione un veterinario.") Long veterinarioId,
        @NotNull(message = "Indique la fecha y la hora de la cita.") LocalDateTime fechaHora,
        @NotBlank(message = "Indique el motivo de la consulta.")
        @Size(max = 500, message = "El motivo no puede superar 500 caracteres.") String motivo,
        EstadoCita estado) {
}
