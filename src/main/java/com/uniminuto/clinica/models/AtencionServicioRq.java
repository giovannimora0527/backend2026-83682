package com.uniminuto.clinica.models;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AtencionServicioRq(@NotNull(message = "El servicio es obligatorio.") Long servicioId,
                                @Positive(message = "La cantidad debe ser mayor que 0.") @Max(value = 1000, message = "La cantidad no es válida.") Integer cantidad) {
}
