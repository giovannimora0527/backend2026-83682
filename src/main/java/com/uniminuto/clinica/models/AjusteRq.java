package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.enums.TipoMovimiento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AjusteRq(
        @NotNull(message = "El lote es obligatorio.") Long loteId,
        @NotNull(message = "El tipo de ajuste es obligatorio.") TipoMovimiento tipo,
        @NotNull(message = "La cantidad es obligatoria.") @Positive(message = "La cantidad debe ser mayor que 0.") Integer cantidad,
        @NotBlank(message = "El motivo es obligatorio.") @Size(max = 500, message = "El motivo admite hasta 500 caracteres.") String motivo) {
}
