package com.clinicaveterinaria.clinica.api;

import com.clinicaveterinaria.clinica.models.MedicoDTO;
import com.clinicaveterinaria.clinica.service.MedicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicos")
@RequiredArgsConstructor
public class MedicoController {

    private final MedicoService medicoService;

    @PostMapping
    public ResponseEntity<MedicoDTO> crear(@RequestBody MedicoDTO dto) {
        return new ResponseEntity<>(medicoService.crear(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MedicoDTO>> listarTodos() {
        return ResponseEntity.ok(medicoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(medicoService.buscarPorId(id));
    }

    @GetMapping("/documento/{numeroDocumento}")
    public ResponseEntity<MedicoDTO> buscarPorDocumento(@PathVariable String numeroDocumento) {
        return ResponseEntity.ok(medicoService.buscarPorDocumento(numeroDocumento));
    }

    @GetMapping("/registro/{registro}")
    public ResponseEntity<MedicoDTO> buscarPorRegistro(@PathVariable String registro) {
        return ResponseEntity.ok(medicoService.buscarPorRegistro(registro));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicoDTO> actualizar(@PathVariable Long id, @RequestBody MedicoDTO dto) {
        return ResponseEntity.ok(medicoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        medicoService.eliminar(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}