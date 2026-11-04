package com.uniminuto.clinica.models;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClienteActualizarRq {

    @NotBlank(message = "El tipo de documento es obligatorio.")
    @Size(max = 10, message = "El tipo de documento admite hasta 10 caracteres.")
    private String tipoDocumento;

    @NotBlank(message = "El numero de documento es obligatorio.")
    @Size(max = 20, message = "El numero de documento admite hasta 20 caracteres.")
    private String numeroDocumento;

    @NotBlank(message = "Los nombres son obligatorios.")
    @Size(max = 100, message = "Los nombres admiten hasta 100 caracteres.")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios.")
    @Size(max = 100, message = "Los apellidos admiten hasta 100 caracteres.")
    private String apellidos;

    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura.")
    private LocalDate fechaNacimiento;

    @Size(min = 1, max = 1, message = "El genero debe tener un caracter.")
    @Pattern(regexp = "\\S", message = "El genero no puede ser un espacio en blanco.")
    private String genero;

    @Size(max = 20, message = "El telefono admite hasta 20 caracteres.")
    private String telefono;

    private String direccion;

    @JsonAnySetter
    public void rechazarCampoDesconocido(String nombre, Object valor) {
        throw new IllegalArgumentException("El campo " + nombre + " no forma parte de la actualizacion del cliente.");
    }
}
