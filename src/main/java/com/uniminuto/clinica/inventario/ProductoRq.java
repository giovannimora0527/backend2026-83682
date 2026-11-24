package com.uniminuto.clinica.inventario;

import java.math.BigDecimal;

/**
 * Datos de un medicamento, producto o utensilio del inventario.
 *
 * @param codigo             código alfanumérico de 8 caracteres.
 * @param tipo               MEDICAMENTO, PRODUCTO o UTENSILIO.
 * @param invima             registro INVIMA (obligatorio para MEDICAMENTO).
 * @param ivaPorcentaje      IVA aplicable a la venta (opcional; por defecto 0% medicamentos y 19% otros).
 */
public record ProductoRq(String codigo, String nombre, String descripcion, String presentacion, String tipo,
                         String invima, BigDecimal precioCompra, BigDecimal precioVenta, Integer stockMinimo,
                         String fechaCompra, String fechaIngreso, String fechaProximoPedido, String fechaVence,
                         Boolean activo, BigDecimal ivaPorcentaje) {
}
