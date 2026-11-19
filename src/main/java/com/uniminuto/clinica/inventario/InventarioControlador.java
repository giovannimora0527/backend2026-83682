package com.uniminuto.clinica.inventario;

import static com.uniminuto.clinica.inventario.InventarioDatos.*;

import com.uniminuto.clinica.service.IdentidadService;
import com.uniminuto.clinica.service.IdentidadService.Identidad;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/inventario")
public class InventarioControlador {
    private final InventarioServicio inventario;
    private final ImportacionInventario importacion;

    public InventarioControlador(InventarioServicio inventario, ImportacionInventario importacion) {
        this.inventario=inventario;
        this.importacion=importacion;
    }

    @GetMapping("/productos")
    List<Ficha> productos() { return inventario.catalogo(); }

    @PostMapping("/productos")
    @ResponseStatus(HttpStatus.CREATED)
    Ficha crear(@RequestBody Producto producto) { return inventario.guardar(null,producto); }

    @PutMapping("/productos/{id}")
    Ficha editar(@PathVariable long id,@RequestBody Producto producto) { return inventario.guardar(id,producto); }

    @GetMapping("/productos/{id}/lotes")
    List<Lote> lotes(@PathVariable long id,@RequestParam(defaultValue="false") boolean disponibles) {
        return inventario.lotes(id,disponibles);
    }

    @PostMapping("/productos/{id}/lotes")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String,Long> crearLote(@PathVariable long id,@RequestBody RegistrarLote lote,
            @RequestAttribute(IdentidadService.ATRIBUTO) Identidad usuario) {
        return Map.of("id",inventario.registrarLote(id,lote,usuario.usuarioId()));
    }

    @GetMapping("/productos/{id}/movimientos")
    List<Movimiento> movimientos(@PathVariable long id) { return inventario.movimientos(id); }

    @PostMapping("/productos/{id}/lotes/{lote}/movimientos")
    Map<String,Integer> registrarMovimiento(@PathVariable long id,@PathVariable long lote,
            @RequestBody RegistrarMovimiento movimiento,@RequestAttribute(IdentidadService.ATRIBUTO) Identidad usuario) {
        return Map.of("saldo",inventario.registrarMovimiento(id,lote,movimiento,usuario.usuarioId()));
    }

    @PostMapping(value="/importaciones",consumes="multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    Importacion previsualizar(@RequestParam MultipartFile archivo,@RequestAttribute(IdentidadService.ATRIBUTO) Identidad usuario) {
        return importacion.previsualizar(archivo,usuario.usuarioId());
    }

    @GetMapping("/importaciones")
    List<Resumen> historial() { return importacion.historial(); }

    @GetMapping("/importaciones/{id}")
    Importacion detalle(@PathVariable long id) { return importacion.detalle(id); }

    @PostMapping("/importaciones/{id}/confirmar")
    Importacion confirmar(@PathVariable long id,@RequestAttribute(IdentidadService.ATRIBUTO) Identidad usuario) {
        return importacion.confirmar(id,usuario.usuarioId());
    }
}
