package co.edu.uniminuto.veterinaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para agregar una anotación a una historia clínica. La fecha y hora la asigna el servidor
 * para que no pueda alterarse el historial.
 *
 * @param veterinarioId       veterinario que escribe la anotación
 * @param descripcion         descripción de lo realizado
 * @param observacion         observación del veterinario
 * @param diagnosticoEvolucion diagnóstico o evolución del paciente
 */
public record AnotacionRequest(
        @NotNull(message = "Seleccione un veterinario.") Long veterinarioId,
        @NotBlank(message = "Indique la descripción.")
        @Size(max = 2000, message = "La descripción no puede superar 2000 caracteres.") String descripcion,
        @NotBlank(message = "Indique la observación.")
        @Size(max = 2000, message = "La observación no puede superar 2000 caracteres.") String observacion,
        @NotBlank(message = "Indique el diagnóstico o la evolución.")
        @Size(max = 2000, message = "El diagnóstico/evolución no puede superar 2000 caracteres.")
        String diagnosticoEvolucion) {
}
