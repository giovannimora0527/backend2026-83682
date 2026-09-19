package com.clinicaveterinaria.clinica.api;

import com.clinicaveterinaria.clinica.models.SessionDTO;
import com.clinicaveterinaria.clinica.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessiones")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<SessionDTO> crear(@RequestBody SessionDTO dto) {
        return new ResponseEntity<>(sessionService.crear(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SessionDTO>> listarTodos() {
        return ResponseEntity.ok(sessionService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.buscarPorId(id));
    }

    @GetMapping("/token/{token}")
    public ResponseEntity<SessionDTO> buscarPorToken(@PathVariable String token) {
        return ResponseEntity.ok(sessionService.buscarPorToken(token));
    }

    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<SessionDTO>> buscarPorUsuario(@PathVariable Integer userId) {
        return ResponseEntity.ok(sessionService.buscarPorUsuario(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionDTO> actualizar(@PathVariable Long id, @RequestBody SessionDTO dto) {
        return ResponseEntity.ok(sessionService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        sessionService.eliminar(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}