package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.dto.FormulaMedicaDTO;
import com.uniminuto.clinica.service.FormulaMedicaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** Endpoints REST para consultar y administrar fórmulas médicas. */
@RestController
@CrossOrigin
@RequestMapping("/api/formulas")
public class FormulaMedicaController implements FormulaMedicaApi {
    private final FormulaMedicaService service;
    /** Constructor con inyección del servicio. */
    public FormulaMedicaController(FormulaMedicaService service) { this.service = service; }

    /** Lista fórmulas ordenadas de más reciente a más antigua. */
    @Override @GetMapping
    public ResponseEntity<List<FormulaMedicaDTO>> listarFormulasMedicas() { return ResponseEntity.ok(service.listarFormulasMedicas()); }

    /** Obtiene el detalle de una fórmula. */
    @GetMapping("/{id}") public ResponseEntity<FormulaMedicaDTO> obtener(@PathVariable Long id) { return ResponseEntity.ok(service.obtenerPorId(id)); }
    /** Registra una fórmula y responde 201 Created. */
    @PostMapping public ResponseEntity<FormulaMedicaDTO> crear(@Valid @RequestBody FormulaMedicaDTO dto) { return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto)); }
    /** Actualiza una fórmula y responde con el registro actualizado. */
    @PutMapping("/{id}") public ResponseEntity<FormulaMedicaDTO> actualizar(@PathVariable Long id, @Valid @RequestBody FormulaMedicaDTO dto) { return ResponseEntity.ok(service.actualizar(id, dto)); }
}
