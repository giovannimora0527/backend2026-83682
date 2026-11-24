package com.uniminuto.clinica.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

/** items vacio: solo cuando nada de lo pendiente tiene existencias. */
public record DespachoConfirmarRq(
        @Size(max = 500, message = "La observacion admite hasta 500 caracteres.") String observacion,
        @Valid List<@NotNull(message = "El item es obligatorio.") Item> items) {

    public record Item(
            @NotNull(message = "El detalle de la formula es obligatorio.") Long formulaDetalleId,
            @NotNull(message = "El lote es obligatorio.") Long loteId,
            @NotNull(message = "La cantidad es obligatoria.") @Positive(message = "La cantidad debe ser mayor que 0.") Integer cantidad) {
    }
}
