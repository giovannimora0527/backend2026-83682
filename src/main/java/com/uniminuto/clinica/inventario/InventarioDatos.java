package com.uniminuto.clinica.inventario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class InventarioDatos {
    private InventarioDatos() {}

    public enum TipoProducto { MEDICAMENTO, PRODUCTO, UTENSILIO }
    public enum TipoMovimiento { ENTRADA, SALIDA, AJUSTE }

    public record Producto(String codigo, String nombre, String descripcion, String presentacion,
            TipoProducto tipo, String invima, BigDecimal precioCompra, BigDecimal precioVenta,
            Integer stockMinimo, LocalDate fechaCompra, LocalDate fechaIngreso,
            LocalDate fechaProximoPedido, LocalDate fechaVence, Boolean activo) {}

    public record Ficha(long id, Producto producto, long existencia, long disponible,
            boolean vencido, boolean proximoVencer, LocalDate vencimientoLote,
            LocalDateTime fechaCreacion, LocalDateTime fechaModificacion) {}

    public record RegistrarLote(String numero, BigDecimal costo, LocalDate fechaIngreso,
            LocalDate fechaVence, Integer cantidad, String referencia) {}

    public record Lote(long id, long medicamentoId, String numero, BigDecimal costo,
            LocalDate fechaIngreso, LocalDate fechaVence, int cantidad, boolean vencido,
            boolean proximoVencer, boolean disponible) {}

    public record RegistrarMovimiento(TipoMovimiento tipo, Integer cantidad, String motivo, String referencia) {}

    public record Movimiento(long id, long medicamentoId, long loteId, String lote, long usuarioId,
            String usuario, TipoMovimiento tipo, int cantidad, int saldo, LocalDateTime fecha,
            String motivo, String referencia) {}

    public record Fila(int numero, Producto producto, RegistrarLote lote, List<String> errores) {}
    public record Resumen(long id, long usuarioId, String archivo, LocalDateTime fecha,
            LocalDateTime fechaConfirmacion, int importados, int rechazados) {}
    public record Importacion(Resumen resumen, List<Fila> filas) {}
}
