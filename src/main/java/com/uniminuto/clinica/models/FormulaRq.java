package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.enums.ViaAdministracion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record FormulaRq(
        @NotNull(message = "La cita es obligatoria.") Long citaId,
        String indicacionesGenerales,
        @NotEmpty(message = "La formula debe tener al menos un medicamento.") @Valid List<@NotNull(message = "El detalle es obligatorio.") Detalle> detalles) {

    /** Si cantidadPrescrita no llega, se usa la sugerida. */
    public record Detalle(
            @NotNull(message = "El medicamento es obligatorio.") Long medicamentoId,
            @NotNull(message = "La dosis es obligatoria.") @DecimalMin(value = "0", inclusive = false, message = "La dosis debe ser mayor que 0.") @Digits(integer = 6, fraction = 2, message = "La dosis admite 6 enteros y 2 decimales.") BigDecimal dosis,
            @NotBlank(message = "La unidad de la dosis es obligatoria.") @Size(max = 30, message = "La unidad admite hasta 30 caracteres.") String unidadDosis,
            @NotNull(message = "La frecuencia es obligatoria.") @Min(value = 1, message = "La frecuencia debe estar entre 1 y 168 horas.") @Max(value = 168, message = "La frecuencia debe estar entre 1 y 168 horas.") Integer frecuenciaHoras,
            @NotNull(message = "La duracion es obligatoria.") @Min(value = 1, message = "La duracion debe ser mayor que 0.") @Max(value = 730, message = "La duracion admite hasta 730 dias.") Integer duracionDias,
            @NotNull(message = "La via de administracion es obligatoria.") ViaAdministracion viaAdministracion,
            @Min(value = 1, message = "La cantidad debe ser al menos 1.") Integer cantidadPrescrita,
            String indicaciones) {

        /** ceil(dosis x (24 / frecuencia) x duracion), como minimo 1. */
        public int cantidadSugerida() {
            return FormulaRs.sugerida(dosis, frecuenciaHoras, duracionDias);
        }
    }
}
