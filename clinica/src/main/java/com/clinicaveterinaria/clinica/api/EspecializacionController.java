package com.clinicaveterinaria.clinica.api;

import com.clinicaveterinaria.clinica.models.EspecializacionDTO;
import com.clinicaveterinaria.clinica.service.EspecializacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especializaciones")
@RequiredArgsConstructor
public class EspecializacionController {

    private final EspecializacionService especializacionService;

    @PostMapping
    public ResponseEntity<EspecializacionDTO> crear(@RequestBody EspecializacionDTO dto) {
        return new ResponseEntity<>(especializacionService.crear(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EspecializacionDTO>> listarTodos() {
        return ResponseEntity.ok(especializacionService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspecializacionDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(especializacionService.buscarPorId(id));
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<EspecializacionDTO> buscarPorNombre(@PathVariable String nombre) {
        return ResponseEntity.ok(especializacionService.buscarPorNombre(nombre));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<EspecializacionDTO> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(especializacionService.buscarPorCodigo(codigo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspecializacionDTO> actualizar(@PathVariable Long id, @RequestBody EspecializacionDTO dto) {
        return ResponseEntity.ok(especializacionService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        especializacionService.eliminar(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}