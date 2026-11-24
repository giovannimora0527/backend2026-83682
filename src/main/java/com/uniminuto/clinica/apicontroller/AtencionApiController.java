package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AtencionApi;
import com.uniminuto.clinica.models.AtencionCitaRs;
import com.uniminuto.clinica.models.AtencionRq;
import com.uniminuto.clinica.models.AtencionRs;
import com.uniminuto.clinica.models.RespuestaRs;
import com.uniminuto.clinica.service.AtencionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AtencionApiController implements AtencionApi {

    private final AtencionService servicio;

    public AtencionApiController(AtencionService servicio) {
        this.servicio = servicio;
    }

    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<RespuestaRs<AtencionRs>> iniciar(Long citaId) {
        return ResponseEntity.ok(RespuestaRs.ok("Atención iniciada.", servicio.iniciar(citaId)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<AtencionCitaRs> consultar(Long citaId) {
        return ResponseEntity.ok(servicio.consultar(citaId));
    }

    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<RespuestaRs<AtencionRs>> guardar(Long id, AtencionRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Borrador guardado correctamente.", servicio.guardarBorrador(id, rq)));
    }

    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<RespuestaRs<AtencionRs>> finalizar(Long id) {
        return ResponseEntity.ok(RespuestaRs.ok("Atención finalizada correctamente.", servicio.finalizar(id)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<byte[]> prefactura(Long id) {
        return servicio.prefactura(id).respuesta();
    }
}
