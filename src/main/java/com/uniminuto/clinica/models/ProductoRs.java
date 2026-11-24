package com.uniminuto.clinica.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.uniminuto.clinica.entity.enums.EstadoStock;
import com.uniminuto.clinica.entity.enums.TipoProducto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ProductoRs(Long id, String codigo, String nombre, TipoProducto tipoProducto, String descripcion,
                         String presentacion, String unidadMedida, Boolean requiereInvima, String registroInvima,
                         BigDecimal precioVenta, Long impuestoId, BigDecimal porcentajeIva, Integer stockMinimo,
                         LocalDate fechaProximoPedido, Boolean activo, int cantidadDisponible,
                         BigDecimal precioCompraUltimo, BigDecimal precioCompraPromedio,
                         LocalDate proximoVencimiento, List<EstadoStock> estadosStock,
                         @JsonInclude(JsonInclude.Include.NON_NULL) List<LoteRs> lotes) {
}
