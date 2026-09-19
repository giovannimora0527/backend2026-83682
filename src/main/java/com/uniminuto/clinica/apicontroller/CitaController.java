package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.dto.CitaDTO;
import com.uniminuto.clinica.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST de Cita.
 *
 * <p>Expone los servicios pedidos en los Requerimientos 2, 3 y 4 del
 * taller: filtrar citas por fecha, crear una cita y actualizar una cita.</p>
 */
@RestController
@CrossOrigin
@RequestMapping("/api/citas")
public class CitaController implements CitaApi {

    /** Servicio con la logica de negocio de Cita. */
    private final CitaService citaService;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param citaService servicio de citas
     */
    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Responde a peticiones {@code GET /api/citas/filtrar?fechaInicio=...&fechaFin=...}.
     * Cumple los Requerimientos 2 y 3: filtrar las citas por un rango de
     * fechas, devueltas de la mas reciente a la mas antigua.</p>
     *
     * <p>El formato esperado para las fechas es ISO-8601, por ejemplo:
     * {@code 2026-01-01T00:00:00}.</p>
     */
    @Override
    @GetMapping("/filtrar")
    public ResponseEntity<List<CitaDTO>> filtrarCitasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return ResponseEntity.ok(citaService.filtrarCitasPorFecha(fechaInicio, fechaFin));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Responde a peticiones {@code POST /api/citas}. Cumple el
     * Requerimiento 4: adicionar una nueva cita.</p>
     */
    @Override
    @PostMapping
    public ResponseEntity<CitaDTO> crearCita(@Valid @RequestBody CitaDTO citaDTO) {
        CitaDTO creada = citaService.crearCita(citaDTO);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Responde a peticiones {@code PUT /api/citas/{id}}. Cumple el
     * Requerimiento 4: actualizar una cita almacenada en el sistema.</p>
     */
    @Override
    @PutMapping("/{id}")
    public ResponseEntity<CitaDTO> actualizarCita(@PathVariable Long id, @RequestBody CitaDTO citaDTO) {
        return ResponseEntity.ok(citaService.actualizarCita(id, citaDTO));
    }
}
