package com.clinicaveterinaria.clinica.api;

import com.clinicaveterinaria.clinica.models.HistoriaMedicaDTO;
import com.clinicaveterinaria.clinica.service.HistoriaMedicaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/historias-medicas")
@RequiredArgsConstructor
public class HistoriaMedicaController {

    private final HistoriaMedicaService historiaMedicaService;

    @PostMapping
    public ResponseEntity<HistoriaMedicaDTO> crear(@RequestBody HistoriaMedicaDTO dto) {
        return new ResponseEntity<>(historiaMedicaService.crear(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<HistoriaMedicaDTO>> listarTodos() {
        return ResponseEntity.ok(historiaMedicaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoriaMedicaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(historiaMedicaService.buscarPorId(id));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<HistoriaMedicaDTO>> buscarPorPaciente(@PathVariable Integer pacienteId) {
        return ResponseEntity.ok(historiaMedicaService.buscarPorPaciente(pacienteId));
    }

    @GetMapping("/filtrar")
    public ResponseEntity<List<HistoriaMedicaDTO>> buscarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFinal) {
        return ResponseEntity.ok(historiaMedicaService.buscarPorFecha(fechaInicial, fechaFinal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistoriaMedicaDTO> actualizar(@PathVariable Long id, @RequestBody HistoriaMedicaDTO dto) {
        return ResponseEntity.ok(historiaMedicaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        historiaMedicaService.eliminar(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}