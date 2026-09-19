package com.clinicaveterinaria.clinica.api;

import com.clinicaveterinaria.clinica.models.RazaDTO;
import com.clinicaveterinaria.clinica.service.RazaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/razas")
@RequiredArgsConstructor
public class RazaController {

    private final RazaService razaService;

    @PostMapping
    public ResponseEntity<RazaDTO> crear(@RequestBody RazaDTO dto) {
        return new ResponseEntity<>(razaService.crear(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RazaDTO>> listarTodos() {
        return ResponseEntity.ok(razaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RazaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(razaService.buscarPorId(id));
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<RazaDTO> buscarPorNombre(@PathVariable String nombre) {
        return ResponseEntity.ok(razaService.buscarPorNombre(nombre));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RazaDTO> actualizar(@PathVariable Long id, @RequestBody RazaDTO dto) {
        return ResponseEntity.ok(razaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        razaService.eliminar(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}