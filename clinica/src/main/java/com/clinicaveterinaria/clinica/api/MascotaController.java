package com.clinicaveterinaria.clinica.api;

import com.clinicaveterinaria.clinica.models.MascotaDTO;
import com.clinicaveterinaria.clinica.service.MascotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaService mascotaService;

    @PostMapping
    public ResponseEntity<MascotaDTO> crear(@RequestBody MascotaDTO dto) {
        return new ResponseEntity<>(mascotaService.crear(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MascotaDTO>> listarTodos() {
        return ResponseEntity.ok(mascotaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MascotaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(mascotaService.buscarPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<MascotaDTO>> buscarPorCliente(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(mascotaService.buscarPorCliente(clienteId));
    }

    @GetMapping("/raza/{razaId}")
    public ResponseEntity<List<MascotaDTO>> buscarPorRaza(@PathVariable Integer razaId) {
        return ResponseEntity.ok(mascotaService.buscarPorRaza(razaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MascotaDTO> actualizar(@PathVariable Long id, @RequestBody MascotaDTO dto) {
        return ResponseEntity.ok(mascotaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mascotaService.eliminar(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}