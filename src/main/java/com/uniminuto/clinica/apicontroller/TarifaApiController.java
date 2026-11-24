package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.TarifaApi;
import com.uniminuto.clinica.models.RespuestaRs;
import com.uniminuto.clinica.models.TarifaActualizarRq;
import com.uniminuto.clinica.models.TarifaCrearRq;
import com.uniminuto.clinica.models.TarifaHistorialRs;
import com.uniminuto.clinica.models.TarifaRs;
import com.uniminuto.clinica.service.TarifaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

@RestController
public class TarifaApiController implements TarifaApi {

    private final TarifaService servicio;

    public TarifaApiController(TarifaService servicio) {
        this.servicio = servicio;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TarifaRs>> listar(Long servicioId, boolean soloVigentes) {
        return ResponseEntity.ok(servicio.listar(servicioId, soloVigentes));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<TarifaRs>> crear(TarifaCrearRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Tarifa creada correctamente.", servicio.crear(rq)));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<TarifaRs>> actualizar(Long id, TarifaActualizarRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Tarifa actualizada correctamente.", servicio.actualizar(id, rq)));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TarifaHistorialRs>> historialTarifa(Long id) {
        return ResponseEntity.ok(servicio.historial(id));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TarifaHistorialRs>> historial(LocalDate desde, LocalDate hasta) {
        return ResponseEntity.ok(servicio.historial(desde, hasta));
    }
}
