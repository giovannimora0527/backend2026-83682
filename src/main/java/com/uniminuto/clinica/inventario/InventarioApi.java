package com.uniminuto.clinica.inventario;

import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.Permitido;
import io.swagger.v3.oas.annotations.Operation;
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

    record LoteRq(String numero, BigDecimal costo, String fechaIngreso, String fechaVence, Integer cantidad,
                  String referencia) {
    }

    record MovimientoRq(String tipo, Integer cantidad, String motivo, String referencia) {
    }

    @Operation(summary = "Listar productos con existencias, disponibilidad y alertas",
            description = "disponible excluye lotes vencidos; incluye banderas vencido y proximoVencer (30 días).")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.MEDICO, Perfiles.DESPACHO_MEDICO})
    @GetMapping("/productos")
    ResponseEntity<List<Map<String, Object>>> listar();

    @Operation(summary = "Consultar un producto")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.MEDICO, Perfiles.DESPACHO_MEDICO})
    @GetMapping("/productos/{id}")
    ResponseEntity<Map<String, Object>> consultar(@PathVariable long id);

    @Operation(summary = "Registrar producto", description = "Código de 8 caracteres; INVIMA obligatorio para MEDICAMENTO.")
    @Permitido(Perfiles.ADMINISTRADOR)
    @PostMapping("/productos")
    ResponseEntity<Map<String, Object>> crear(@RequestBody ProductoRq producto);

    @Operation(summary = "Editar producto (no modifica existencias)")
    @Permitido(Perfiles.ADMINISTRADOR)
    @PutMapping("/productos/{id}")
    ResponseEntity<Map<String, Object>> actualizar(@PathVariable long id, @RequestBody ProductoRq producto);

    @Operation(summary = "Lotes de un producto ordenados por vencimiento (FEFO)")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.MEDICO, Perfiles.DESPACHO_MEDICO})
    @GetMapping("/productos/{id}/lotes")
    ResponseEntity<List<Map<String, Object>>> lotes(@PathVariable long id);

    @Operation(summary = "Registrar lote con su entrada inicial")
    @Permitido(Perfiles.ADMINISTRADOR)
    @PostMapping("/productos/{id}/lotes")
    ResponseEntity<Map<String, Object>> registrarLote(@PathVariable long id, @RequestBody LoteRq lote);

    @Operation(summary = "Historial de entradas y salidas (kardex) de un producto")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.DESPACHO_MEDICO})
    @GetMapping("/productos/{id}/movimientos")
    ResponseEntity<List<Map<String, Object>>> movimientos(@PathVariable long id);

    @Operation(summary = "Registrar movimiento manual (ENTRADA, SALIDA o AJUSTE con signo)")
    @Permitido(Perfiles.ADMINISTRADOR)
    @PostMapping("/productos/{id}/lotes/{lote}/movimientos")
    ResponseEntity<Map<String, Object>> registrarMovimiento(@PathVariable long id, @PathVariable long lote,
                                                           @RequestBody MovimientoRq movimiento);

    @Operation(summary = "Carga masiva: vista previa y validación de un archivo CSV o XLSX")
    @Permitido(Perfiles.ADMINISTRADOR)
    @PostMapping(value = "/importaciones", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Map<String, Object>> previsualizar(@RequestPart("archivo") MultipartFile archivo);

    @Operation(summary = "Carga masiva: confirmar la importación de los registros válidos")
    @Permitido(Perfiles.ADMINISTRADOR)
    @PostMapping("/importaciones/{id}/confirmar")
    ResponseEntity<Map<String, Object>> confirmar(@PathVariable long id);

    @Operation(summary = "Historial de cargas masivas (log de cargue)")
    @Permitido(Perfiles.ADMINISTRADOR)
    @GetMapping("/importaciones")
    ResponseEntity<List<Map<String, Object>>> historial();

    @Operation(summary = "Detalle de una carga masiva con errores por fila")
    @Permitido(Perfiles.ADMINISTRADOR)
    @GetMapping("/importaciones/{id}")
    ResponseEntity<Map<String, Object>> detalle(@PathVariable long id);
}
