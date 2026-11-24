package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.uniminuto.clinica.entity.enums.EstadoStock;
import com.uniminuto.clinica.entity.enums.TipoMovimiento;
import com.uniminuto.clinica.entity.enums.TipoProducto;
import com.uniminuto.clinica.models.AjusteRq;
import com.uniminuto.clinica.models.AlertasRs;
import com.uniminuto.clinica.models.DisponibilidadRs;
import com.uniminuto.clinica.models.EstadoRq;
import com.uniminuto.clinica.models.LoteRq;
import com.uniminuto.clinica.models.LoteRs;
import com.uniminuto.clinica.models.MovimientoRs;
import com.uniminuto.clinica.models.ProductoRq;
import com.uniminuto.clinica.models.ProductoRs;
import com.uniminuto.clinica.models.RespuestaRs;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Inventario")
@RequestMapping("/inventario")
public interface InventarioApi {

    @Operation(summary = "Listar productos")
    @GetMapping(value = "/productos", produces = {"application/json"})
    ResponseEntity<List<ProductoRs>> listar(@RequestParam(required = false) String texto,
            @RequestParam(required = false) TipoProducto tipo,
            @RequestParam(required = false) EstadoStock estadoStock,
            @RequestParam(defaultValue = "false") boolean soloActivos);

    @Operation(summary = "Consultar un producto con sus lotes")
    @GetMapping(value = "/productos/{id}", produces = {"application/json"})
    ResponseEntity<ProductoRs> obtener(@PathVariable("id") Long id);

    @Operation(summary = "Crear un producto, con lote inicial opcional")
    @PostMapping(value = "/productos", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ProductoRs>> crear(@Valid @RequestBody ProductoRq rq);

    @Operation(summary = "Actualizar un producto", description = "No cambia cantidades.")
    @PutMapping(value = "/productos/{id}", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ProductoRs>> actualizar(@PathVariable("id") Long id, @Valid @RequestBody ProductoRq rq);

    @Operation(summary = "Activar o inactivar un producto")
    @PatchMapping(value = "/productos/{id}/estado", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ProductoRs>> cambiarEstado(@PathVariable("id") Long id, @Valid @RequestBody EstadoRq rq);

    @Operation(summary = "Registrar una entrada por compra")
    @PostMapping(value = "/productos/{id}/lotes", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ProductoRs>> ingresarLote(@PathVariable("id") Long id, @Valid @RequestBody LoteRq rq);

    @Operation(summary = "Listar lotes de un producto")
    @GetMapping(value = "/productos/{id}/lotes", produces = {"application/json"})
    ResponseEntity<List<LoteRs>> lotes(@PathVariable("id") Long id,
            @RequestParam(defaultValue = "false") boolean soloVigentes);

    @Operation(summary = "Ajustar existencias con motivo")
    @PostMapping(value = "/productos/{id}/ajustes", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ProductoRs>> ajustar(@PathVariable("id") Long id, @Valid @RequestBody AjusteRq rq);

    @Operation(summary = "Movimientos de un producto")
    @GetMapping(value = "/productos/{id}/movimientos", produces = {"application/json"})
    ResponseEntity<List<MovimientoRs>> movimientosProducto(@PathVariable("id") Long id,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta);

    @Operation(summary = "Movimientos de inventario")
    @GetMapping(value = "/movimientos", produces = {"application/json"})
    ResponseEntity<List<MovimientoRs>> movimientos(@RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta,
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) Long medicamentoId);

    @Operation(summary = "Conteo de alertas")
    @GetMapping(value = "/alertas", produces = {"application/json"})
    ResponseEntity<AlertasRs> alertas();

    @Operation(summary = "Disponibilidad vigente de un producto")
    @GetMapping(value = "/disponibilidad/{medicamentoId}", produces = {"application/json"})
    ResponseEntity<DisponibilidadRs> disponibilidad(@PathVariable("medicamentoId") Long medicamentoId);
}
