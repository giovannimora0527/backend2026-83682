package com.clinicaveterinaria.clinica.api;

import com.clinicaveterinaria.clinica.models.AnotacionHistoriaDTO;
import com.clinicaveterinaria.clinica.service.AnotacionHistoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anotaciones")
@RequiredArgsConstructor
public class AnotacionHistoriaController {

    private final AnotacionHistoriaService anotacionHistoriaService;

    @PostMapping
    public ResponseEntity<AnotacionHistoriaDTO> crear(@RequestBody AnotacionHistoriaDTO dto) {
        return new ResponseEntity<>(anotacionHistoriaService.crear(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AnotacionHistoriaDTO>> listarTodos() {
        return ResponseEntity.ok(anotacionHistoriaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnotacionHistoriaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(anotacionHistoriaService.buscarPorId(id));
    }

    @GetMapping("/historia/{historiaId}")
    public ResponseEntity<List<AnotacionHistoriaDTO>> buscarPorHistoria(@PathVariable Integer historiaId) {
        return ResponseEntity.ok(anotacionHistoriaService.buscarPorHistoria(historiaId));
    }

    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<List<AnotacionHistoriaDTO>> buscarPorMedico(@PathVariable Integer medicoId) {
        return ResponseEntity.ok(anotacionHistoriaService.buscarPorMedico(medicoId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnotacionHistoriaDTO> actualizar(@PathVariable Long id, @RequestBody AnotacionHistoriaDTO dto) {
        return ResponseEntity.ok(anotacionHistoriaService.actualizar(id, dto));
    }

  /*  @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        anotacionHistoriaService.eliminar(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }*/
}