package com.uniminuto.clinica.models;

import jakarta.validation.constraints.NotNull;

/** Activar o inactivar un registro. */
public record EstadoRq(@NotNull(message = "El estado es obligatorio.") Boolean activo) {
}
