package com.clinicaveterinaria.clinica.api;

import com.clinicaveterinaria.clinica.models.MedicamentoDTO;
import com.clinicaveterinaria.clinica.service.MedicamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
@RequiredArgsConstructor
public class MedicamentoController {

    private final MedicamentoService medicamentoService;

    @PostMapping
    public ResponseEntity<MedicamentoDTO> crear(@RequestBody MedicamentoDTO dto) {
        return new ResponseEntity<>(medicamentoService.crear(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MedicamentoDTO>> listarTodos() {
        return ResponseEntity.ok(medicamentoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicamentoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(medicamentoService.buscarPorId(id));
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<MedicamentoDTO> buscarPorNombre(@PathVariable String nombre) {
        return ResponseEntity.ok(medicamentoService.buscarPorNombre(nombre));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicamentoDTO> actualizar(@PathVariable Long id, @RequestBody MedicamentoDTO dto) {
        return ResponseEntity.ok(medicamentoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        medicamentoService.eliminar(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}