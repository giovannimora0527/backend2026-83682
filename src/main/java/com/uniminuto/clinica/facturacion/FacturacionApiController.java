package com.uniminuto.clinica.facturacion;

import com.uniminuto.clinica.comun.Archivo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST del módulo: implementa {@link FacturacionApi} (rutas, permisos y documentación Swagger)
 * y delega la lógica en la capa de servicio.
 */
@RestController
public class FacturacionApiController implements FacturacionApi {

    /** Dependencia inyectada por constructor: servicio. */
    private final FacturacionService servicio;

    /**
     * Crea el controlador con sus dependencias (inyección por constructor).
     *
     * @param servicio dependencia servicio.
     */
    public FacturacionApiController(FacturacionService servicio) {
        this.servicio = servicio;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> elegibles(Integer pagina) {
        return ResponseEntity.ok(servicio.elegibles(pagina));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> previa(long id, PreviaRq datos) {
        return ResponseEntity.ok(servicio.previa(id, datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> emitir(long id, EmitirRq datos) {
        return ResponseEntity.ok(servicio.emitir(id, datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> listar(String desde, String hasta, String propietario, String mascota,
                                                            String estado, Integer pagina) {
        return ResponseEntity.ok(servicio.listar(desde, hasta, propietario, mascota, estado, pagina));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> detalle(long id) {
        return ResponseEntity.ok(servicio.detalle(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> pagar(long id, PagoRq datos) {
        return ResponseEntity.ok(servicio.pagar(id, datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> anular(long id, MotivoRq datos) {
        return ResponseEntity.ok(servicio.anular(id, datos == null ? null : datos.motivo()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<byte[]> pdf(long id) {
        Map<String, Object> factura = servicio.detalle(id);
        @SuppressWarnings("unchecked")
        Object numero = ((Map<String, Object>) factura.get("factura")).get("numero");
        return Archivo.pdf(servicio.pdf(id), "factura-" + numero + ".pdf");
    }
}
