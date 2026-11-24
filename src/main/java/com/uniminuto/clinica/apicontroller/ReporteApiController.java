package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ReporteApi;
import com.uniminuto.clinica.models.ReporteCatalogoRs;
import com.uniminuto.clinica.models.ReporteRs;
import com.uniminuto.clinica.service.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ReporteApiController implements ReporteApi {

    private final ReporteService servicio;

    public ReporteApiController(ReporteService servicio) {
        this.servicio = servicio;
    }

    @Override
    public ResponseEntity<List<ReporteCatalogoRs>> catalogo() {
        return ResponseEntity.ok(servicio.catalogo());
    }

    @Override
    public ResponseEntity<ReporteRs> generar(String codigo, Map<String, String> filtros) {
        return ResponseEntity.ok(servicio.generar(codigo, filtros));
    }

    @Override
    public ResponseEntity<byte[]> exportar(String codigo, String formato, Map<String, String> filtros) {
        return servicio.exportar(codigo, formato, filtros).respuesta();
    }
}
