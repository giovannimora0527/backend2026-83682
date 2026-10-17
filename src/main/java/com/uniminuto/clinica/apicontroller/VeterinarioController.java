package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.models.Veterinario;
import com.uniminuto.clinica.service.VeterinarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** API REST de veterinarios; separa el transporte HTTP de la lógica de negocio. */
@RestController
@CrossOrigin
@RequestMapping("/api/veterinarios")
public class VeterinarioController {
    private final VeterinarioService servicio;
    public VeterinarioController(VeterinarioService servicio) { this.servicio = servicio; }
    /** Lista los veterinarios. */
    @GetMapping public List<Veterinario> listar() { return servicio.listar(); }
    /** Consulta un veterinario por identificador. */
    @GetMapping("/{id}") public Veterinario obtener(@PathVariable Long id) { return servicio.obtener(id); }
    /** Registra un veterinario. */
    @PostMapping public ResponseEntity<Veterinario> crear(@Valid @RequestBody Veterinario v) { return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(v)); }
    /** Actualiza los datos de un veterinario. */
    @PutMapping("/{id}") public Veterinario actualizar(@PathVariable Long id, @Valid @RequestBody Veterinario datos) { return servicio.actualizar(id, datos); }
}
