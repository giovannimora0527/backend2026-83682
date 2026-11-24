package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FacturaApi;
import com.uniminuto.clinica.models.FacturaRs;
import com.uniminuto.clinica.service.FacturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
public class FacturaApiController implements FacturaApi {

    private final FacturaService facturaService;

    public FacturaApiController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @Override
    public ResponseEntity<List<FacturaRs>> listarPorCliente(Long clienteId) {
        return ResponseEntity.ok(facturaService.listarPorCliente(clienteId));
    }

    @Override
    public ResponseEntity<List<FacturaRs>> listarPorMascota(Integer mascotaId) {
        return ResponseEntity.ok(facturaService.listarPorMascota(mascotaId));
    }
}
