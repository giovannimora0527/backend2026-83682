package com.uniminuto.clinica.models;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ImpuestoRq(
        @NotBlank(message = "El nombre es obligatorio.") @Size(max = 50, message = "El nombre admite hasta 50 caracteres.") String nombre,
        @NotNull(message = "El porcentaje es obligatorio.") @DecimalMin(value = "0", message = "El porcentaje debe estar entre 0 y 100.") @DecimalMax(value = "100", message = "El porcentaje debe estar entre 0 y 100.") @Digits(integer = 3, fraction = 2, message = "El porcentaje admite 2 decimales.") BigDecimal porcentaje,
        Boolean activo) {
}
