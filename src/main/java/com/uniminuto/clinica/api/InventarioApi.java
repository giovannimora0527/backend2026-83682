package com.uniminuto.clinica.api;

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

@RequestMapping("/inventario")
public interface InventarioApi {

    @GetMapping(value = "/productos", produces = {"application/json"})
    ResponseEntity<List<ProductoRs>> listar(@RequestParam(required = false) String texto,
            @RequestParam(required = false) TipoProducto tipo,
            @RequestParam(required = false) EstadoStock estadoStock,
            @RequestParam(defaultValue = "false") boolean soloActivos);

    @GetMapping(value = "/productos/{id}", produces = {"application/json"})
    ResponseEntity<ProductoRs> obtener(@PathVariable("id") Long id);

    @PostMapping(value = "/productos", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ProductoRs>> crear(@Valid @RequestBody ProductoRq rq);

    @PutMapping(value = "/productos/{id}", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ProductoRs>> actualizar(@PathVariable("id") Long id, @Valid @RequestBody ProductoRq rq);

    @PatchMapping(value = "/productos/{id}/estado", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ProductoRs>> cambiarEstado(@PathVariable("id") Long id, @Valid @RequestBody EstadoRq rq);

    @PostMapping(value = "/productos/{id}/lotes", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ProductoRs>> ingresarLote(@PathVariable("id") Long id, @Valid @RequestBody LoteRq rq);

    @GetMapping(value = "/productos/{id}/lotes", produces = {"application/json"})
    ResponseEntity<List<LoteRs>> lotes(@PathVariable("id") Long id,
            @RequestParam(defaultValue = "false") boolean soloVigentes);

    @PostMapping(value = "/productos/{id}/ajustes", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ProductoRs>> ajustar(@PathVariable("id") Long id, @Valid @RequestBody AjusteRq rq);

    @GetMapping(value = "/productos/{id}/movimientos", produces = {"application/json"})
    ResponseEntity<List<MovimientoRs>> movimientosProducto(@PathVariable("id") Long id,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta);

    @GetMapping(value = "/movimientos", produces = {"application/json"})
    ResponseEntity<List<MovimientoRs>> movimientos(@RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta,
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) Long medicamentoId);

    @GetMapping(value = "/alertas", produces = {"application/json"})
    ResponseEntity<AlertasRs> alertas();

    @GetMapping(value = "/disponibilidad/{medicamentoId}", produces = {"application/json"})
    ResponseEntity<DisponibilidadRs> disponibilidad(@PathVariable("medicamentoId") Long medicamentoId);
}
