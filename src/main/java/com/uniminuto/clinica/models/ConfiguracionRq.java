package com.uniminuto.clinica.models;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Los prefijos de formula y despacho y el tope son opcionales: si no llegan, se conservan. */
public record ConfiguracionRq(
        @NotBlank(message = "El nombre es obligatorio.") @Size(max = 150, message = "El nombre admite hasta 150 caracteres.") String nombre,
        @NotBlank(message = "El NIT es obligatorio.") @Size(max = 30, message = "El NIT admite hasta 30 caracteres.") String nit,
        @Size(max = 200, message = "La direccion admite hasta 200 caracteres.") String direccion,
        @Size(max = 30, message = "El telefono admite hasta 30 caracteres.") String telefono,
        @Email(message = "El correo no es valido.") @Size(max = 255, message = "El correo admite hasta 255 caracteres.") String correo,
        @NotBlank(message = "El prefijo es obligatorio.") @Pattern(regexp = "^[A-Za-z0-9]{1,10}$", message = "El prefijo admite de 1 a 10 letras o numeros.") String prefijoFactura,
        @Pattern(regexp = "^[A-Za-z0-9]{1,10}$", message = "El prefijo admite de 1 a 10 letras o numeros.") String prefijoFormula,
        @Pattern(regexp = "^[A-Za-z0-9]{1,10}$", message = "El prefijo admite de 1 a 10 letras o numeros.") String prefijoDespacho,
        @DecimalMin(value = "0", message = "El tope debe estar entre 0 y 100.") @DecimalMax(value = "100", message = "El tope debe estar entre 0 y 100.") @Digits(integer = 3, fraction = 2, message = "El tope admite 2 decimales.") BigDecimal topeDescuentoSecretaria) {
}
