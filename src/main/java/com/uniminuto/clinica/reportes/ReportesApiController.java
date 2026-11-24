package com.uniminuto.clinica.reportes;

import com.uniminuto.clinica.comun.Archivo;
import com.uniminuto.clinica.comun.Tiempo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador REST del módulo: implementa {@link ReportesApi} (rutas, permisos y documentación Swagger)
 * y delega la lógica en la capa de servicio.
 */
@RestController
public class ReportesApiController implements ReportesApi {

    /** Dependencia inyectada por constructor: servicio. */
    private final ReportesService servicio;

    /**
     * Crea el controlador con sus dependencias (inyección por constructor).
     *
     * @param servicio dependencia servicio.
     */
    public ReportesApiController(ReportesService servicio) {
        this.servicio = servicio;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> consultar(String tipo, Map<String, String> filtros, Integer pagina) {
        return ResponseEntity.ok(servicio.consultar(tipo, filtros, pagina));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<byte[]> pdf(String tipo, Map<String, String> filtros) {
        return Archivo.pdf(servicio.pdf(tipo, filtros), nombre(tipo) + ".pdf");
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<byte[]> excel(String tipo, Map<String, String> filtros) {
        return Archivo.excel(servicio.excel(tipo, filtros), nombre(tipo) + ".xlsx");
    }

    /**
     * @param tipo tipo de reporte.
     * @return nombre base del archivo a descargar.
     */
    private static String nombre(String tipo) {
        return "reporte-" + tipo.toLowerCase().replaceAll("[^a-z_]", "") + "-" + Tiempo.hoy();
    }
}
