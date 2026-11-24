package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Citas y calendario")
@RequestMapping("/cita")
public interface CitaApi {

    @Operation(summary = "Listar citas")
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitas();

    @Operation(summary = "Listar citas por rango de fechas")
    @GetMapping(value = "/listar-rango", produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitasRango(@RequestParam LocalDateTime fechaInicio,
                                                @RequestParam LocalDateTime fechaFin);

    @Operation(summary = "Listar citas de una mascota")
    @GetMapping(value = "/listar-por-mascota", produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitasPorMascota(@RequestParam Integer mascotaId);

    /** Incluye canceladas y las de cualquier medico. */
    @Operation(summary = "Historial de citas de una mascota, incluidas canceladas")
    @GetMapping(value = "/historial-mascota/{mascotaId}", produces = {"application/json"})
    ResponseEntity<List<Cita>> historialMascota(@PathVariable("mascotaId") Integer mascotaId);

    @Operation(summary = "Crear una cita", description = "Valida horario, solapamientos, médico y mascota activos.")
    @PostMapping(value = "/crear", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<Cita>> crearCita(@RequestBody CitaRq cita);

    @Operation(summary = "Editar una cita")
    @PostMapping(value = "/actualizar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<Cita>> actualizarCita(@RequestBody CitaRq cita);

    @Operation(summary = "Eventos del calendario", description = "Un MEDICO recibe siempre los suyos.")
    @GetMapping(value = "/calendario", produces = {"application/json"})
    ResponseEntity<List<EventoCalendarioRs>> calendario(@RequestParam String desde, @RequestParam String hasta,
                                                        @RequestParam(required = false) Long medicoId);

    @Operation(summary = "Conteo de citas por estado")
    @GetMapping(value = "/resumen-estados", produces = {"application/json"})
    ResponseEntity<Map<EstadoCita, Long>> resumenEstados(@RequestParam String desde, @RequestParam String hasta,
                                                         @RequestParam(required = false) Long medicoId);

    @Operation(summary = "Reporte Excel de citas")
    @GetMapping(value = "/reporte/excel")
    ResponseEntity<byte[]> reporteExcel(@RequestParam String desde, @RequestParam String hasta,
                                        @RequestParam(required = false) Long medicoId);

    @Operation(summary = "Consultar una cita")
    @GetMapping(value = "/{id}", produces = {"application/json"})
    ResponseEntity<Cita> obtenerCita(@PathVariable("id") Long id);

    @Operation(summary = "Reprogramar una cita")
    @PatchMapping(value = "/{id}/reprogramar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<Cita>> reprogramar(@PathVariable("id") Long id, @Valid @RequestBody ReprogramarRq rq);

    @Operation(summary = "Confirmar o cancelar una cita", description = "EN_ATENCION y ATENDIDA solo se asignan desde la atención.")
    @PatchMapping(value = "/{id}/estado", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<Cita>> cambiarEstado(@PathVariable("id") Long id, @Valid @RequestBody EstadoCitaRq rq);

    @Operation(summary = "Historial de estados de la cita")
    @GetMapping(value = "/{id}/historial-estados", produces = {"application/json"})
    ResponseEntity<List<CitaHistorialRs>> historialEstados(@PathVariable("id") Long id);
}
