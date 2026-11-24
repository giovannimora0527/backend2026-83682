package com.uniminuto.clinica.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MotivoRq(@NotBlank(message = "El motivo es obligatorio.") @Size(max = 500, message = "El motivo admite hasta 500 caracteres.") String motivo) {
}
