package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.model.Cita;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para gestionar las citas médicas.
 * Expone endpoints para operaciones CRUD y filtrado de citas.
 *
 * @author TuNombre
 * @version 1.0
 */
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    /**
     * Servicio para gestionar citas médicas.
     */
    @Autowired
    private CitaService citaService;

    /**
     * Filtra las citas dentro de un rango de fechas específico.
     * Los resultados se ordenan de la más reciente a la más antigua.
     *
     * @param fechaInicio Fecha inicial del rango de filtrado (formato: yyyy-MM-ddTHH:mm).
     * @param fechaFin Fecha final del rango de filtrado (formato: yyyy-MM-ddTHH:mm).
     * @return ResponseEntity con la lista de citas filtradas y el estado HTTP.
     */
    @GetMapping("/filtrar")
    public ResponseEntity<List<Cita>> filtrarCitasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        List<Cita> citas = citaService.filtrarCitasPorRangoFechas(fechaInicio, fechaFin);
        return ResponseEntity.ok(citas);
    }

    /**
     * Obtiene todas las citas del sistema.
     *
     * @return ResponseEntity con la lista de todas las citas.
     */
    @GetMapping
    public ResponseEntity<List<Cita>> listarTodasLasCitas() {
        List<Cita> citas = citaService.listarTodas();
        return ResponseEntity.ok(citas);
    }

    /**
     * Busca una cita por su identificador.
     *
     * @param id Identificador de la cita a buscar.
     * @return ResponseEntity con la cita encontrada o estado 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Cita> buscarCitaPorId(@PathVariable Long id) {
        return citaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea una nueva cita en el sistema.
     *
     * @param cita La cita a crear con los datos necesarios.
     * @return ResponseEntity con la cita creada y estado 201 (Created).
     */
    @PostMapping
    public ResponseEntity<Cita> crearCita(@RequestBody Cita cita) {
        Cita nuevaCita = citaService.crearCita(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
    }

    /**
     * Actualiza una cita existente en el sistema.
     *
     * @param id Identificador de la cita a actualizar.
     * @param citaDetalles Los nuevos datos de la cita.
     * @return ResponseEntity con la cita actualizada o estado 404 si no existe.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Cita> actualizarCita(
            @PathVariable Long id,
            @RequestBody Cita citaDetalles) {

        Cita citaActualizada = citaService.actualizarCita(id, citaDetalles);

        if (citaActualizada != null) {
            return ResponseEntity.ok(citaActualizada);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}