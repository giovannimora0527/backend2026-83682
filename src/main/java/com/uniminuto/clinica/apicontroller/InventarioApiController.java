package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.InventarioApi;
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
import com.uniminuto.clinica.service.InventarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

@RestController
public class InventarioApiController implements InventarioApi {

    private final InventarioService servicio;

    public InventarioApiController(InventarioService servicio) {
        this.servicio = servicio;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DESPACHO_MEDICO', 'MEDICO')")
    public ResponseEntity<List<ProductoRs>> listar(String texto,
            TipoProducto tipo,
            EstadoStock estadoStock,
            boolean soloActivos) {
        return ResponseEntity.ok(servicio.listar(texto, tipo, estadoStock, soloActivos));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DESPACHO_MEDICO', 'MEDICO')")
    public ResponseEntity<ProductoRs> obtener(Long id) {
        return ResponseEntity.ok(servicio.obtener(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<ProductoRs>> crear(ProductoRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Producto creado correctamente.",
                servicio.crear(rq)));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<ProductoRs>> actualizar(Long id, ProductoRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Producto actualizado correctamente.",
                servicio.actualizar(id, rq)));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<ProductoRs>> cambiarEstado(Long id, EstadoRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Estado actualizado correctamente.",
                servicio.cambiarEstado(id, rq.activo())));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<ProductoRs>> ingresarLote(Long id, LoteRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Entrada registrada correctamente.",
                servicio.ingresarLote(id, rq)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DESPACHO_MEDICO')")
    public ResponseEntity<List<LoteRs>> lotes(Long id,
            boolean soloVigentes) {
        return ResponseEntity.ok(servicio.lotes(id, soloVigentes));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<ProductoRs>> ajustar(Long id, AjusteRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Ajuste registrado correctamente.",
                servicio.ajustar(id, rq)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DESPACHO_MEDICO')")
    public ResponseEntity<List<MovimientoRs>> movimientosProducto(Long id,
            LocalDate desde,
            LocalDate hasta) {
        return ResponseEntity.ok(servicio.movimientos(id, null, desde, hasta));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DESPACHO_MEDICO')")
    public ResponseEntity<List<MovimientoRs>> movimientos(LocalDate desde,
            LocalDate hasta,
            TipoMovimiento tipo,
            Long medicamentoId) {
        return ResponseEntity.ok(servicio.movimientos(medicamentoId, tipo, desde, hasta));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DESPACHO_MEDICO')")
    public ResponseEntity<AlertasRs> alertas() {
        return ResponseEntity.ok(servicio.alertas());
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DESPACHO_MEDICO', 'MEDICO')")
    public ResponseEntity<DisponibilidadRs> disponibilidad(Long medicamentoId) {
        return ResponseEntity.ok(servicio.disponibilidad(medicamentoId));
    }
}
