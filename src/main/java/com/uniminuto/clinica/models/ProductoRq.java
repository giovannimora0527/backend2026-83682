package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.enums.TipoProducto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Editar no cambia cantidades. El impuesto se indica por impuestoId o por porcentajeIva. */
public record ProductoRq(
        @NotNull(message = "El codigo es obligatorio.") @Pattern(regexp = "^[A-Za-z0-9]{8}$", message = "El codigo debe tener 8 caracteres alfanumericos.") String codigo,
        @NotBlank(message = "El nombre es obligatorio.") @Size(max = 100, message = "El nombre admite hasta 100 caracteres.") String nombre,
        @NotNull(message = "El tipo de producto es obligatorio.") TipoProducto tipoProducto,
        String descripcion,
        @Size(max = 100, message = "La presentacion admite hasta 100 caracteres.") String presentacion,
        @Size(max = 30, message = "La unidad de medida admite hasta 30 caracteres.") String unidadMedida,
        Boolean requiereInvima,
        @Size(max = 50, message = "El registro Invima admite hasta 50 caracteres.") String registroInvima,
        @NotNull(message = "El precio de venta es obligatorio.") @DecimalMin(value = "0", message = "El precio de venta no puede ser negativo.") @Digits(integer = 12, fraction = 2, message = "El precio de venta admite 2 decimales.") BigDecimal precioVenta,
        Long impuestoId,
        BigDecimal porcentajeIva,
        @NotNull(message = "El stock minimo es obligatorio.") @Min(value = 0, message = "El stock minimo no puede ser negativo.") Integer stockMinimo,
        LocalDate fechaProximoPedido,
        @Valid LoteRq loteInicial) {
}
