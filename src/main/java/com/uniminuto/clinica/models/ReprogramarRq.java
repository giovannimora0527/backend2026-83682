package com.uniminuto.clinica.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record ReprogramarRq(@NotNull(message = "La fecha de inicio es obligatoria.") LocalDateTime fechaInicio,
                            @Size(max = 255, message = "El motivo admite hasta 255 caracteres.") String motivo) {
}
