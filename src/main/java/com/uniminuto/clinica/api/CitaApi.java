package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.enums.EstadoCita;
import com.uniminuto.clinica.models.CitaHistorialRs;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.EstadoCitaRq;
import com.uniminuto.clinica.models.EventoCalendarioRs;
import com.uniminuto.clinica.models.ReprogramarRq;
import com.uniminuto.clinica.models.RespuestaRs;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RequestMapping("/cita")
public interface CitaApi {

    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitas();

    @GetMapping(value = "/listar-rango", produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitasRango(@RequestParam LocalDateTime fechaInicio,
                                                @RequestParam LocalDateTime fechaFin);

    @GetMapping(value = "/listar-por-mascota", produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitasPorMascota(@RequestParam Integer mascotaId);

    /** Incluye canceladas y las de cualquier medico. */
    @GetMapping(value = "/historial-mascota/{mascotaId}", produces = {"application/json"})
    ResponseEntity<List<Cita>> historialMascota(@PathVariable("mascotaId") Integer mascotaId);

    @PostMapping(value = "/crear", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<Cita>> crearCita(@RequestBody CitaRq cita);

    @PostMapping(value = "/actualizar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<Cita>> actualizarCita(@RequestBody CitaRq cita);

    @GetMapping(value = "/calendario", produces = {"application/json"})
    ResponseEntity<List<EventoCalendarioRs>> calendario(@RequestParam String desde, @RequestParam String hasta,
                                                        @RequestParam(required = false) Long medicoId);

    @GetMapping(value = "/resumen-estados", produces = {"application/json"})
    ResponseEntity<Map<EstadoCita, Long>> resumenEstados(@RequestParam String desde, @RequestParam String hasta,
                                                         @RequestParam(required = false) Long medicoId);

    @GetMapping(value = "/reporte/excel")
    ResponseEntity<byte[]> reporteExcel(@RequestParam String desde, @RequestParam String hasta,
                                        @RequestParam(required = false) Long medicoId);

    @GetMapping(value = "/{id}", produces = {"application/json"})
    ResponseEntity<Cita> obtenerCita(@PathVariable("id") Long id);

    @PatchMapping(value = "/{id}/reprogramar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<Cita>> reprogramar(@PathVariable("id") Long id, @Valid @RequestBody ReprogramarRq rq);

    @PatchMapping(value = "/{id}/estado", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<Cita>> cambiarEstado(@PathVariable("id") Long id, @Valid @RequestBody EstadoCitaRq rq);

    @GetMapping(value = "/{id}/historial-estados", produces = {"application/json"})
    ResponseEntity<List<CitaHistorialRs>> historialEstados(@PathVariable("id") Long id);
}
