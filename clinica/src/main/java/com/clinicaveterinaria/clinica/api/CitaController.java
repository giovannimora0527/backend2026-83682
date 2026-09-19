package com.clinicaveterinaria.clinica.api;

import com.clinicaveterinaria.clinica.models.CitaDTO;
import com.clinicaveterinaria.clinica.service.CitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    @PostMapping
    public ResponseEntity<CitaDTO> crear(@RequestBody CitaDTO dto) {
        return new ResponseEntity<>(citaService.crear(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CitaDTO>> listarTodos() {
        return ResponseEntity.ok(citaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CitaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.buscarPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<CitaDTO>> buscarPorCliente(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(citaService.buscarPorCliente(clienteId));
    }

    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<List<CitaDTO>> buscarPorMascota(@PathVariable Integer mascotaId) {
        return ResponseEntity.ok(citaService.buscarPorMascota(mascotaId));
    }

    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<List<CitaDTO>> buscarPorMedico(@PathVariable Integer medicoId) {
        return ResponseEntity.ok(citaService.buscarPorMedico(medicoId));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<CitaDTO>> buscarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(citaService.buscarPorEstado(estado));
    }

    @GetMapping("/filtrar")
    public ResponseEntity<List<CitaDTO>> buscarPorFecha(@RequestParam LocalDateTime fechaInicial, @RequestParam LocalDateTime fechaFinal) {
        return ResponseEntity.ok(citaService.buscarPorFecha(fechaInicial,fechaFinal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CitaDTO> actualizar(@PathVariable Long id, @RequestBody CitaDTO dto) {
        return ResponseEntity.ok(citaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        citaService.eliminar(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }




}