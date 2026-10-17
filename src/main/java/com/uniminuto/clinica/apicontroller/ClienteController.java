package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.models.Cliente;
import com.uniminuto.clinica.models.Paciente;
import com.uniminuto.clinica.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** API REST de clientes; delega las reglas de negocio en ClienteService. */
@RestController
@CrossOrigin
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService servicio;
    public ClienteController(ClienteService servicio) { this.servicio = servicio; }
    /** Lista los clientes registrados. */
    @GetMapping public List<Cliente> listar() { return servicio.listar(); }
    /** Consulta un cliente por identificador. */
    @GetMapping("/{id}") public Cliente obtener(@PathVariable Long id) { return servicio.obtener(id); }
    /** Registra un cliente. */
    @PostMapping public ResponseEntity<Cliente> crear(@Valid @RequestBody Cliente cliente) { return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(cliente)); }
    /** Actualiza los datos básicos de un cliente. */
    @PutMapping("/{id}") public Cliente actualizar(@PathVariable Long id, @Valid @RequestBody Cliente datos) { return servicio.actualizar(id, datos); }
    /** Lista las mascotas del cliente indicado. */
    @GetMapping("/{id}/mascotas") public List<Paciente> mascotas(@PathVariable Long id) { return servicio.listarMascotas(id); }
}
