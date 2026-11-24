package com.uniminuto.clinica.models;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Entrada de existencias por compra. */
public record LoteRq(
        @NotBlank(message = "El numero de lote es obligatorio.") @Size(max = 50, message = "El lote admite hasta 50 caracteres.") String numeroLote,
        @NotNull(message = "El precio de compra es obligatorio.") @DecimalMin(value = "0", message = "El precio de compra no puede ser negativo.") @Digits(integer = 12, fraction = 2, message = "El precio de compra admite 2 decimales.") BigDecimal precioCompra,
        @NotNull(message = "La cantidad es obligatoria.") @Positive(message = "La cantidad debe ser mayor que 0.") Integer cantidad,
        @NotNull(message = "La fecha de compra es obligatoria.") @PastOrPresent(message = "La fecha de compra no puede ser futura.") LocalDate fechaCompra,
        LocalDate fechaIngreso,
        LocalDate fechaVencimiento,
        @Size(max = 500, message = "La observacion admite hasta 500 caracteres.") String observacion) {
}
