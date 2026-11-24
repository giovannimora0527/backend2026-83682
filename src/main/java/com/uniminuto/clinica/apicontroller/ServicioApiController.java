package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ServicioApi;
import com.uniminuto.clinica.models.EstadoRq;
import com.uniminuto.clinica.models.RespuestaRs;
import com.uniminuto.clinica.models.ServicioRq;
import com.uniminuto.clinica.models.ServicioRs;
import com.uniminuto.clinica.service.ServicioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class ServicioApiController implements ServicioApi {

    private final ServicioService servicio;

    public ServicioApiController(ServicioService servicio) {
        this.servicio = servicio;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServicioRs>> listar(boolean soloActivos, Long especializacionId) {
        return ResponseEntity.ok(servicio.listar(soloActivos, especializacionId));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<ServicioRs>> crear(ServicioRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Servicio creado correctamente.", servicio.crear(rq)));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<ServicioRs>> actualizar(Long id, ServicioRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Servicio actualizado correctamente.", servicio.actualizar(id, rq)));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<ServicioRs>> cambiarEstado(Long id, EstadoRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Estado actualizado correctamente.", servicio.cambiarEstado(id, rq.activo())));
    }
}
