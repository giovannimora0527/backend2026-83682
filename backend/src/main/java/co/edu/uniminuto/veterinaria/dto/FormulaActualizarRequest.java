package co.edu.uniminuto.veterinaria.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Datos editables de una fórmula. La mascota, la historia, el veterinario y la fecha de emisión no se
 * modifican: son parte del registro clínico y cambiarlos podría dejar información inconsistente.
 *
 * @param medicamentoId   medicamento (no vencido)
 * @param dosisValor      cantidad de la dosis (mayor que 0)
 * @param dosisUnidad     unidad de la dosis
 * @param frecuenciaHoras cada cuántas horas se administra (1 a 168)
 * @param duracionDias    duración del tratamiento en días (1 a 365)
 * @param indicaciones    indicaciones para el cliente
 */
public record FormulaActualizarRequest(
        @NotNull(message = "Seleccione un medicamento.") Long medicamentoId,
        @NotNull(message = "Indique la dosis.")
        @DecimalMin(value = "0.01", message = "La dosis debe ser mayor que cero.")
        @DecimalMax(value = "10000", message = "La dosis es demasiado alta.") BigDecimal dosisValor,
        @NotBlank(message = "Indique la unidad de la dosis.")
        @Size(max = 20, message = "La unidad no puede superar 20 caracteres.") String dosisUnidad,
        @NotNull(message = "Indique la frecuencia.")
        @Min(value = 1, message = "La frecuencia mínima es 1 hora.")
        @Max(value = 168, message = "La frecuencia máxima es 168 horas.") Integer frecuenciaHoras,
        @NotNull(message = "Indique la duración del tratamiento.")
        @Min(value = 1, message = "La duración mínima es 1 día.")
        @Max(value = 365, message = "La duración máxima es 365 días.") Integer duracionDias,
        @NotBlank(message = "Indique las indicaciones.")
        @Size(max = 1000, message = "Las indicaciones no pueden superar 1000 caracteres.") String indicaciones) {
}
