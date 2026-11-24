package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.enums.MedioPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PagoRq(
        @NotNull(message = "El valor es obligatorio.") @DecimalMin(value = "0", inclusive = false, message = "El valor debe ser mayor que 0.") @Digits(integer = 12, fraction = 2, message = "El valor admite 2 decimales.") BigDecimal valor,
        @NotNull(message = "El medio de pago es obligatorio.") MedioPago medioPago,
        @Size(max = 100, message = "La referencia admite hasta 100 caracteres.") String referencia) {
}
