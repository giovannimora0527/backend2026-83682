package com.uniminuto.clinica.models;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TarifaCrearRq(
        @NotNull(message = "El servicio es obligatorio.") Long servicioId,
        @NotNull(message = "El valor es obligatorio.") @DecimalMin(value = "0", message = "El valor no puede ser negativo.") @Digits(integer = 12, fraction = 2, message = "El valor admite 2 decimales.") BigDecimal valor,
        @NotNull(message = "El impuesto es obligatorio.") Long impuestoId,
        LocalDate vigenteDesde) {
}
