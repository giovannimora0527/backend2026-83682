package com.uniminuto.clinica.reportes;

import com.uniminuto.clinica.comun.Archivo;
import com.uniminuto.clinica.comun.Tiempo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ReportesApiController implements ReportesApi {

    private final ReportesService servicio;

    public ReportesApiController(ReportesService servicio) {
        this.servicio = servicio;
    }

    @Override
    public ResponseEntity<Map<String, Object>> consultar(String tipo, Map<String, String> filtros, Integer pagina) {
        return ResponseEntity.ok(servicio.consultar(tipo, filtros, pagina));
    }

    @Override
    public ResponseEntity<byte[]> pdf(String tipo, Map<String, String> filtros) {
        return Archivo.pdf(servicio.pdf(tipo, filtros), nombre(tipo) + ".pdf");
    }

    @Override
    public ResponseEntity<byte[]> excel(String tipo, Map<String, String> filtros) {
        return Archivo.excel(servicio.excel(tipo, filtros), nombre(tipo) + ".xlsx");
    }

    private static String nombre(String tipo) {
        return "reporte-" + tipo.toLowerCase().replaceAll("[^a-z_]", "") + "-" + Tiempo.hoy();
    }
}
