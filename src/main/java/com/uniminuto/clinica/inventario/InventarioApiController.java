package com.uniminuto.clinica.inventario;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST del módulo: implementa {@link InventarioApi} (rutas, permisos y documentación Swagger)
 * y delega la lógica en la capa de servicio.
 */
@RestController
public class InventarioApiController implements InventarioApi {

    /** Dependencia inyectada por constructor: inventario. */
    private final InventarioService inventario;
    /** Dependencia inyectada por constructor: importacion. */
    private final ImportacionService importacion;

    /**
     * Crea el controlador con sus dependencias (inyección por constructor).
     *
     * @param inventario dependencia inventario.
     * @param importacion dependencia importacion.
     */
    public InventarioApiController(InventarioService inventario, ImportacionService importacion) {
        this.inventario = inventario;
        this.importacion = importacion;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> listar() {
        return ResponseEntity.ok(inventario.listar());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> consultar(long id) {
        return ResponseEntity.ok(inventario.ficha(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> crear(ProductoRq producto) {
        return ResponseEntity.ok(inventario.crear(producto));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> actualizar(long id, ProductoRq producto) {
        return ResponseEntity.ok(inventario.actualizar(id, producto));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> lotes(long id) {
        return ResponseEntity.ok(inventario.lotes(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> registrarLote(long id, LoteRq lote) {
        return ResponseEntity.ok(inventario.registrarLote(id, lote));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> movimientos(long id) {
        return ResponseEntity.ok(inventario.movimientos(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> registrarMovimiento(long id, long lote, MovimientoRq movimiento) {
        return ResponseEntity.ok(inventario.registrarMovimiento(id, lote, movimiento));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> previsualizar(MultipartFile archivo) {
        return ResponseEntity.ok(importacion.previsualizar(archivo));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> confirmar(long id) {
        return ResponseEntity.ok(importacion.confirmar(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> historial() {
        return ResponseEntity.ok(importacion.historial());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> detalle(long id) {
        return ResponseEntity.ok(importacion.detalle(id));
    }
}
