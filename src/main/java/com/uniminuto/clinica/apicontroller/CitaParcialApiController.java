package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.models.CitaResponse;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Conserva la ruta del parcial {@code /cita/filtrar-by-fechas}.
 * El módulo completo de citas está en {@link CitaApiController} ({@code /citas}).
 */
@RestController
@RequestMapping("/cita")
public class CitaParcialApiController {

    private final CitaService service;

    public CitaParcialApiController(CitaService service) {
        this.service = service;
    }

    /**
     * Lista las citas entre dos fechas (formato ISO, por ejemplo {@code 2026-10-17T00:00:00}).
     */
    @GetMapping(value = "/filtrar-by-fechas", produces = "application/json")
    public ResponseEntity<List<CitaResponse>> filtrarPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFinal) {
        return ResponseEntity.ok(service.filtrarPorFechas(fechaInicio, fechaFinal));
    }
}
