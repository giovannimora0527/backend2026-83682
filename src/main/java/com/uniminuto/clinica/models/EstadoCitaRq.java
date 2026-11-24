package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.enums.EstadoCita;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EstadoCitaRq(@NotNull(message = "El estado es obligatorio.") EstadoCita estado,
                           @Size(max = 255, message = "El motivo admite hasta 255 caracteres.") String motivo) {
}
