package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.enums.EstadoFactura;
import com.uniminuto.clinica.models.CitaPendienteFacturaRs;
import com.uniminuto.clinica.models.FacturaGenerarRq;
import com.uniminuto.clinica.models.FacturaHistorialRs;
import com.uniminuto.clinica.models.FacturaPreviaRs;
import com.uniminuto.clinica.models.FacturaRs;
import com.uniminuto.clinica.models.MotivoRq;
import com.uniminuto.clinica.models.PagoRq;
import com.uniminuto.clinica.models.PagoRs;
import com.uniminuto.clinica.models.RespuestaRs;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/factura")
public interface FacturaApi {

    @GetMapping(value = "/citas-pendientes", produces = {"application/json"})
    ResponseEntity<List<CitaPendienteFacturaRs>> citasPendientes();

    @GetMapping(value = "/previsualizar/{citaId}", produces = {"application/json"})
    ResponseEntity<FacturaPreviaRs> previsualizar(@PathVariable("citaId") Long citaId);

    @PostMapping(value = "/generar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<FacturaRs>> generar(@Valid @RequestBody FacturaGenerarRq rq);

    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<FacturaRs>> listar(@RequestParam(required = false) String numero, @RequestParam(required = false) EstadoFactura estado, @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta, @RequestParam(required = false) Long clienteId, @RequestParam(required = false) Integer mascotaId);

    @GetMapping(value = "/{id}", produces = {"application/json"})
    ResponseEntity<FacturaRs> obtener(@PathVariable("id") Long id);

    @GetMapping(value = "/{id}/pdf")
    ResponseEntity<byte[]> pdf(@PathVariable("id") Long id);

    @PostMapping(value = "/{id}/pagos", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<FacturaRs>> pagar(@PathVariable("id") Long id, @Valid @RequestBody PagoRq rq);

    @GetMapping(value = "/{id}/pagos", produces = {"application/json"})
    ResponseEntity<List<PagoRs>> pagos(@PathVariable("id") Long id);

    @PostMapping(value = "/{id}/anular", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<FacturaRs>> anular(@PathVariable("id") Long id, @Valid @RequestBody MotivoRq rq);

    @GetMapping(value = "/{id}/historial-estados", produces = {"application/json"})
    ResponseEntity<List<FacturaHistorialRs>> historialEstados(@PathVariable("id") Long id);

    @GetMapping(value = "/cliente/{clienteId}", produces = {"application/json"})
    ResponseEntity<List<FacturaRs>> listarPorCliente(@PathVariable("clienteId") Long clienteId);

    @GetMapping(value = "/mascota/{mascotaId}", produces = {"application/json"})
    ResponseEntity<List<FacturaRs>> listarPorMascota(@PathVariable("mascotaId") Integer mascotaId);
}
