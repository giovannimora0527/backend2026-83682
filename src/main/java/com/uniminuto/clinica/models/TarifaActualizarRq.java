package com.uniminuto.clinica.models;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TarifaActualizarRq(
        @NotNull(message = "El valor es obligatorio.") @DecimalMin(value = "0", message = "El valor no puede ser negativo.") @Digits(integer = 12, fraction = 2, message = "El valor admite 2 decimales.") BigDecimal valor,
        @NotNull(message = "El impuesto es obligatorio.") Long impuestoId,
        @NotBlank(message = "El motivo es obligatorio.") @Size(max = 500, message = "El motivo admite hasta 500 caracteres.") String motivo) {
}
