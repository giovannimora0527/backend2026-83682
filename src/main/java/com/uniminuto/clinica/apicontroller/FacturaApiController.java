package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FacturaApi;
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
import com.uniminuto.clinica.service.FacturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

@RestController
public class FacturaApiController implements FacturaApi {

    private final FacturaService servicio;

    public FacturaApiController(FacturaService servicio) {
        this.servicio = servicio;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<List<CitaPendienteFacturaRs>> citasPendientes() {
        return ResponseEntity.ok(servicio.citasPendientes());
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<FacturaPreviaRs> previsualizar(Long citaId) {
        return ResponseEntity.ok(servicio.previsualizar(citaId));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<RespuestaRs<FacturaRs>> generar(FacturaGenerarRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Factura generada correctamente.", servicio.generar(rq)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<List<FacturaRs>> listar(String numero, EstadoFactura estado, LocalDate desde, LocalDate hasta, Long clienteId, Integer mascotaId) {
        return ResponseEntity.ok(servicio.listar(numero, estado, desde, hasta, clienteId, mascotaId));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<FacturaRs> obtener(Long id) {
        return ResponseEntity.ok(servicio.obtener(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<byte[]> pdf(Long id) {
        return servicio.pdf(id).respuesta();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<RespuestaRs<FacturaRs>> pagar(Long id, PagoRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Pago registrado correctamente.", servicio.pagar(id, rq)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<List<PagoRs>> pagos(Long id) {
        return ResponseEntity.ok(servicio.pagos(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<FacturaRs>> anular(Long id, MotivoRq rq) {
        return ResponseEntity.ok(servicio.anular(id, rq.motivo()));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<List<FacturaHistorialRs>> historialEstados(Long id) {
        return ResponseEntity.ok(servicio.historialEstados(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<List<FacturaRs>> listarPorCliente(Long clienteId) {
        return ResponseEntity.ok(servicio.listarPorCliente(clienteId));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<List<FacturaRs>> listarPorMascota(Integer mascotaId) {
        return ResponseEntity.ok(servicio.listarPorMascota(mascotaId));
    }
}
