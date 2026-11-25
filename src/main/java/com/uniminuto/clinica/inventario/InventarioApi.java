package com.uniminuto.clinica.inventario;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Requerimiento 1: módulo de inventario. ADMINISTRADOR registra y edita; MEDICO y DESPACHO_MEDICO
 * consultan (DESPACHO_MEDICO no puede crear, editar ni modificar precios o cantidades).
 */
@Tag(name = "Inventario", description = "Medicamentos, productos, lotes, kardex y carga masiva")
@RequestMapping("/inventario")
public interface InventarioApi {

    /** Datos de un lote: número, costo, fechas de ingreso y vencimiento, cantidad inicial y referencia. */
    record LoteRq(String numero, BigDecimal costo, String fechaIngreso, String fechaVence, Integer cantidad,
                  String referencia) {
    }

    /** Movimiento manual de inventario: ENTRADA, SALIDA o AJUSTE (con signo), cantidad, motivo y referencia. */
    record MovimientoRq(String tipo, Integer cantidad, String motivo, String referencia) {
    }

    /**
     * Listar productos con existencias, disponibilidad y alertas.
     * <p>disponible excluye lotes vencidos; incluye banderas vencido y proximoVencer (30 días).</p>
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Listar productos con existencias, disponibilidad y alertas",
            description = "disponible excluye lotes vencidos; incluye banderas vencido y proximoVencer (30 días).")
    /**
     * Lista los productos del inventario con existencias.
     *
     * @return productos.
     */
    @PreAuthorize("hasAuthority('INVENTARIO_CONSULTAR')")
    @GetMapping("/productos")
    ResponseEntity<List<Map<String, Object>>> listar();

    /**
     * Consultar un producto.
     *
     * @param id identificador del registro.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Consultar un producto")
    @PreAuthorize("hasAuthority('INVENTARIO_CONSULTAR')")
    @GetMapping("/productos/{id}")
    ResponseEntity<Map<String, Object>> consultar(@PathVariable long id);

    /**
     * Registrar producto.
     * <p>Código de 8 caracteres; INVIMA obligatorio para MEDICAMENTO.</p>
     *
     * @param producto datos del medicamento, producto o utensilio.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Registrar producto", description = "Código de 8 caracteres; INVIMA obligatorio para MEDICAMENTO.")
    @PreAuthorize("hasAuthority('INVENTARIO_GESTIONAR')")
    @PostMapping("/productos")
    ResponseEntity<Map<String, Object>> crear(@RequestBody ProductoRq producto);

    /**
     * Editar producto (no modifica existencias).
     *
     * @param id identificador del registro.
     * @param producto datos del medicamento, producto o utensilio.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Editar producto (no modifica existencias)")
    @PreAuthorize("hasAuthority('INVENTARIO_GESTIONAR')")
    @PutMapping("/productos/{id}")
    ResponseEntity<Map<String, Object>> actualizar(@PathVariable long id, @RequestBody ProductoRq producto);

    /**
     * Lotes de un producto ordenados por vencimiento (FEFO).
     *
     * @param id identificador del registro.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Lotes de un producto ordenados por vencimiento (FEFO)")
    @PreAuthorize("hasAuthority('INVENTARIO_CONSULTAR')")
    @GetMapping("/productos/{id}/lotes")
    ResponseEntity<List<Map<String, Object>>> lotes(@PathVariable long id);

    /**
     * Registrar lote con su entrada inicial.
     *
     * @param id identificador del registro.
     * @param lote identificador del lote.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Registrar lote con su entrada inicial")
    @PreAuthorize("hasAuthority('INVENTARIO_GESTIONAR')")
    @PostMapping("/productos/{id}/lotes")
    ResponseEntity<Map<String, Object>> registrarLote(@PathVariable long id, @RequestBody LoteRq lote);

    /**
     * Historial de entradas y salidas (kardex) de un producto.
     *
     * @param id identificador del registro.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Historial de entradas y salidas (kardex) de un producto")
    @PreAuthorize("hasAuthority('INVENTARIO_MOVIMIENTOS')")
    @GetMapping("/productos/{id}/movimientos")
    ResponseEntity<List<Map<String, Object>>> movimientos(@PathVariable long id);

    /**
     * Registrar movimiento manual (ENTRADA, SALIDA o AJUSTE con signo).
     *
     * @param id identificador del registro.
     * @param lote identificador del lote.
     * @param movimiento tipo, cantidad, motivo y referencia del movimiento.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Registrar movimiento manual (ENTRADA, SALIDA o AJUSTE con signo)")
    @PreAuthorize("hasAuthority('INVENTARIO_AJUSTAR')")
    @PostMapping("/productos/{id}/lotes/{lote}/movimientos")
    ResponseEntity<Map<String, Object>> registrarMovimiento(@PathVariable long id, @PathVariable long lote,
                                                           @RequestBody MovimientoRq movimiento);

    /**
     * Carga masiva: vista previa y validación de un archivo CSV o XLSX.
     *
     * @param archivo archivo .csv o .xlsx con los registros a cargar.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Carga masiva: vista previa y validación de un archivo CSV o XLSX")
    @PreAuthorize("hasAuthority('INVENTARIO_IMPORTAR')")
    @PostMapping(value = "/importaciones", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Map<String, Object>> previsualizar(@RequestPart("archivo") MultipartFile archivo);

    /**
     * Carga masiva: confirmar la importación de los registros válidos.
     *
     * @param id identificador del registro.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Carga masiva: confirmar la importación de los registros válidos")
    @PreAuthorize("hasAuthority('INVENTARIO_IMPORTAR')")
    @PostMapping("/importaciones/{id}/confirmar")
    ResponseEntity<Map<String, Object>> confirmar(@PathVariable long id);

    /**
     * Historial de cargas masivas (log de cargue).
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Historial de cargas masivas (log de cargue)")
    @PreAuthorize("hasAuthority('INVENTARIO_IMPORTAR')")
    @GetMapping("/importaciones")
    ResponseEntity<List<Map<String, Object>>> historial();

    /**
     * Detalle de una carga masiva con errores por fila.
     *
     * @param id identificador del registro.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Detalle de una carga masiva con errores por fila")
    @PreAuthorize("hasAuthority('INVENTARIO_IMPORTAR')")
    @GetMapping("/importaciones/{id}")
    ResponseEntity<Map<String, Object>> detalle(@PathVariable long id);
}
