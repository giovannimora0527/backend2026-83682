package com.uniminuto.clinica.despacho;

import com.uniminuto.clinica.comun.Archivo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST del módulo: implementa {@link DespachoApi} (rutas, permisos y documentación Swagger)
 * y delega la lógica en la capa de servicio.
 */
@RestController
public class DespachoApiController implements DespachoApi {

    /** Dependencia inyectada por constructor: servicio. */
    private final DespachoService servicio;

    /**
     * Crea el controlador con sus dependencias (inyección por constructor).
     *
     * @param servicio dependencia servicio.
     */
    public DespachoApiController(DespachoService servicio) {
        this.servicio = servicio;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> formulas(String id, String propietario, String mascota, String fecha,
                                                              String estado, Integer pagina) {
        return ResponseEntity.ok(servicio.formulas(id, propietario, mascota, fecha, estado, pagina));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> detalle(long id) {
        return ResponseEntity.ok(servicio.detalle(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> preparar(long id, PrepararRq datos) {
        return ResponseEntity.ok(servicio.preparar(id, datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> cancelar(long id, MotivoRq datos) {
        return ResponseEntity.ok(servicio.cambiar(id, "cancelar", datos == null ? null : datos.motivo()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> disponibilidad(long id, MotivoRq datos) {
        return ResponseEntity.ok(servicio.cambiar(id, "disponibilidad", datos == null ? null : datos.motivo()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> entregas(String desde, String hasta, String medicamento, Integer pagina) {
        return ResponseEntity.ok(servicio.historial(desde, hasta, medicamento, pagina));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> confirmar(long preparacion) {
        return ResponseEntity.ok(servicio.confirmar(preparacion));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> revertir(long id, MotivoRq datos) {
        return ResponseEntity.ok(servicio.revertir(id, datos == null ? null : datos.motivo()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<byte[]> pdf(long id) {
        return Archivo.pdf(servicio.pdf(id), "despacho-" + id + ".pdf");
    }
}
