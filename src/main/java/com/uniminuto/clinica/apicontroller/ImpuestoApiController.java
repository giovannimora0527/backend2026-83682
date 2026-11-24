package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ImpuestoApi;
import com.uniminuto.clinica.models.ImpuestoRq;
import com.uniminuto.clinica.models.ImpuestoRs;
import com.uniminuto.clinica.models.RespuestaRs;
import com.uniminuto.clinica.service.ImpuestoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class ImpuestoApiController implements ImpuestoApi {

    private final ImpuestoService servicio;

    public ImpuestoApiController(ImpuestoService servicio) {
        this.servicio = servicio;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ImpuestoRs>> listar() {
        return ResponseEntity.ok(servicio.listar());
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<ImpuestoRs>> crear(ImpuestoRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Impuesto creado correctamente.", servicio.crear(rq)));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<ImpuestoRs>> actualizar(Long id, ImpuestoRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Impuesto actualizado correctamente.", servicio.actualizar(id, rq)));
    }
}
