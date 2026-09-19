package com.clinicaveterinaria.clinica.api;

import com.clinicaveterinaria.clinica.models.FormulaMedicaDTO;
import com.clinicaveterinaria.clinica.service.FormulaMedicaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/formulas")
@RequiredArgsConstructor
public class FormulaMedicaController {

    private final FormulaMedicaService formulaMedicaService;

    @PostMapping
    public ResponseEntity<FormulaMedicaDTO> crear(@RequestBody FormulaMedicaDTO dto) {
        return new ResponseEntity<>(formulaMedicaService.crear(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FormulaMedicaDTO>> listarTodos() {
        return ResponseEntity.ok(formulaMedicaService.listarTodos());
    }

    @GetMapping("/OrdenadoPorFecha")
    public ResponseEntity<List<FormulaMedicaDTO>> OrdenarListaFecha(){
        return ResponseEntity.ok(formulaMedicaService.OrdenarListaFecha());

    }


    @GetMapping("/{id}")
    public ResponseEntity<FormulaMedicaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(formulaMedicaService.buscarPorId(id));
    }

    @GetMapping("/cita/{citaId}")
    public ResponseEntity<List<FormulaMedicaDTO>> buscarPorCita(@PathVariable Integer citaId) {
        return ResponseEntity.ok(formulaMedicaService.buscarPorCita(citaId));
    }

    @GetMapping("/medicamento/{medicamentoId}")
    public ResponseEntity<List<FormulaMedicaDTO>> buscarPorMedicamento(@PathVariable Integer medicamentoId) {
        return ResponseEntity.ok(formulaMedicaService.buscarPorMedicamento(medicamentoId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormulaMedicaDTO> actualizar(@PathVariable Long id, @RequestBody FormulaMedicaDTO dto) {
        return ResponseEntity.ok(formulaMedicaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        formulaMedicaService.eliminar(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}