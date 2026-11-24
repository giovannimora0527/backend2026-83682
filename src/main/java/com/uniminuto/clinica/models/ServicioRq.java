package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.enums.TipoServicio;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServicioRq(
        @NotBlank(message = "El codigo es obligatorio.") @Size(max = 20, message = "El codigo admite hasta 20 caracteres.") String codigo,
        @NotBlank(message = "El nombre es obligatorio.") @Size(max = 100, message = "El nombre admite hasta 100 caracteres.") String nombre,
        String descripcion,
        @NotNull(message = "El tipo de servicio es obligatorio.") TipoServicio tipoServicio,
        Long especializacionId,
        @Min(value = 1, message = "La duracion debe ser mayor que 0.") @Max(value = 720, message = "La duracion admite hasta 720 minutos.") Integer duracionMinutos) {
}
