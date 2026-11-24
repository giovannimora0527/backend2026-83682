package com.uniminuto.clinica.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record FacturaGenerarRq(
        @NotNull(message = "La cita es obligatoria.") Long citaId,
        @Size(max = 500, message = "La observacion admite hasta 500 caracteres.") String observacion,
        @Valid List<@NotNull(message = "El descuento es obligatorio.") Descuento> descuentos) {

    public record Descuento(
            @NotBlank(message = "El renglon es obligatorio.") String itemRef,
            @NotNull(message = "El porcentaje es obligatorio.") @DecimalMin(value = "0", inclusive = false, message = "El porcentaje debe ser mayor que 0.") @DecimalMax(value = "100", message = "El porcentaje no puede superar 100.") @Digits(integer = 3, fraction = 2, message = "El porcentaje admite 2 decimales.") BigDecimal porcentaje,
            @NotBlank(message = "El motivo del descuento es obligatorio.") @Size(max = 255, message = "El motivo admite hasta 255 caracteres.") String motivo) {
    }
}
