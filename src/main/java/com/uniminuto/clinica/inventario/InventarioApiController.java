package com.uniminuto.clinica.inventario;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
public class InventarioApiController implements InventarioApi {

    private final InventarioService inventario;
    private final ImportacionService importacion;

    public InventarioApiController(InventarioService inventario, ImportacionService importacion) {
        this.inventario = inventario;
        this.importacion = importacion;
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> listar() {
        return ResponseEntity.ok(inventario.listar());
    }

    @Override
    public ResponseEntity<Map<String, Object>> consultar(long id) {
        return ResponseEntity.ok(inventario.ficha(id));
    }

    @Override
    public ResponseEntity<Map<String, Object>> crear(ProductoRq producto) {
        return ResponseEntity.ok(inventario.crear(producto));
    }

    @Override
    public ResponseEntity<Map<String, Object>> actualizar(long id, ProductoRq producto) {
        return ResponseEntity.ok(inventario.actualizar(id, producto));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> lotes(long id) {
        return ResponseEntity.ok(inventario.lotes(id));
    }

    @Override
    public ResponseEntity<Map<String, Object>> registrarLote(long id, LoteRq lote) {
        return ResponseEntity.ok(inventario.registrarLote(id, lote));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> movimientos(long id) {
        return ResponseEntity.ok(inventario.movimientos(id));
    }

    @Override
    public ResponseEntity<Map<String, Object>> registrarMovimiento(long id, long lote, MovimientoRq movimiento) {
        return ResponseEntity.ok(inventario.registrarMovimiento(id, lote, movimiento));
    }

    @Override
    public ResponseEntity<Map<String, Object>> previsualizar(MultipartFile archivo) {
        return ResponseEntity.ok(importacion.previsualizar(archivo));
    }

    @Override
    public ResponseEntity<Map<String, Object>> confirmar(long id) {
        return ResponseEntity.ok(importacion.confirmar(id));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> historial() {
        return ResponseEntity.ok(importacion.historial());
    }

    @Override
    public ResponseEntity<Map<String, Object>> detalle(long id) {
        return ResponseEntity.ok(importacion.detalle(id));
    }
}
