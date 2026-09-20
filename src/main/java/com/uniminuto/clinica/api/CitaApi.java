package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.CitaRs;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

/** Contrato REST del CRUD de citas (requerimientos 2, 3 y 4). */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
public interface CitaApi {

    /**
     * Crea una nueva cita.
     *
     * @param citaRq datos de la cita.
     * @return la cita creada.
     */
    @PostMapping(value = "/guardar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<CitaRs> guardarCita(@RequestBody CitaRq citaRq);

    /**
     * Actualiza una cita existente.
     *
     * @param citaRq datos de la cita, incluyendo su id.
     * @return la cita actualizada.
     */
    @PostMapping(value = "/actualizar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<CitaRs> actualizarCita(@RequestBody CitaRq citaRq);

    /**
     * Filtra citas por rango de fechas.
     *
     * @param fechaInicial limite inferior (formato yyyy-MM-ddTHH:mm:ss).
     * @param fechaFinal   limite superior (formato yyyy-MM-ddTHH:mm:ss).
     * @return citas del rango, de la mas reciente a la mas antigua.
     */
    @GetMapping(value = "/filtrar", produces = {"application/json"})
    ResponseEntity<List<CitaRs>> filtrarCitas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFinal);
}
