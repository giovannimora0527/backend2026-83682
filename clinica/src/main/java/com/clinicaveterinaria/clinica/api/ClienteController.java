package com.clinicaveterinaria.clinica.api;

import com.clinicaveterinaria.clinica.models.ClienteDTO;
import com.clinicaveterinaria.clinica.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    // CREAR (POST)
    @PostMapping
    public ResponseEntity<ClienteDTO> crear(@RequestBody ClienteDTO dto) {
        ClienteDTO creado = clienteService.crear(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    // LISTAR TODOS (GET)
    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    // BUSCAR POR ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    // BUSCAR POR DOCUMENTO (GET)
    @GetMapping("/documento/{numeroDocumento}")
    public ResponseEntity<ClienteDTO> buscarPorDocumento(@PathVariable String numeroDocumento) {
        return ResponseEntity.ok(clienteService.buscarPorDocumento(numeroDocumento));
    }

    // ACTUALIZAR (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO>  actualizarporid(@PathVariable Long id, @RequestBody ClienteDTO dto) {
        return ResponseEntity.ok(clienteService.actualizarporid(id, dto));
    }

    // ACTUALIZAR (PUT)
    @PutMapping("/documento/{numeroDocumento}")
    public ResponseEntity<ClienteDTO> actualizarPorDocumento(@PathVariable String numeroDocumento , @RequestBody ClienteDTO dto) {
        return ResponseEntity.ok(clienteService.actualizarPorDocumento(numeroDocumento, dto));
    }


    // ELIMINAR (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}