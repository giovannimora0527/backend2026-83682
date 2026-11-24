package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.enums.EstadoCita;
import com.uniminuto.clinica.models.CitaHistorialRs;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.EstadoCitaRq;
import com.uniminuto.clinica.models.EventoCalendarioRs;
import com.uniminuto.clinica.models.ReprogramarRq;
import com.uniminuto.clinica.models.RespuestaRs;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class CitaApiController implements CitaApi {

    private final CitaService citaService;

    public CitaApiController(CitaService citaService) {
        this.citaService = citaService;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<List<Cita>> listarCitasRango(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ResponseEntity.ok(citaService.listarCitasRango(fechaInicio, fechaFin));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA', 'MEDICO')")
    public ResponseEntity<List<Cita>> listarCitasPorMascota(Integer mascotaId) {
        return ResponseEntity.ok(citaService.listarCitasPorMascota(mascotaId));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA', 'MEDICO')")
    public ResponseEntity<List<Cita>> historialMascota(Integer mascotaId) {
        return ResponseEntity.ok(citaService.listarCitasPorMascota(mascotaId));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA', 'MEDICO')")
    public ResponseEntity<RespuestaRs<Cita>> crearCita(CitaRq cita) {
        return ResponseEntity.ok(RespuestaRs.ok("Cita creada correctamente.", citaService.crearCita(cita)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA', 'MEDICO')")
    public ResponseEntity<RespuestaRs<Cita>> actualizarCita(CitaRq cita) {
        return ResponseEntity.ok(RespuestaRs.ok("Cita actualizada correctamente.", citaService.actualizarCita(cita)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA', 'MEDICO')")
    public ResponseEntity<List<EventoCalendarioRs>> calendario(String desde, String hasta, Long medicoId) {
        return ResponseEntity.ok(citaService.calendario(desde, hasta, medicoId));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<Map<EstadoCita, Long>> resumenEstados(String desde, String hasta, Long medicoId) {
        return ResponseEntity.ok(citaService.resumenEstados(desde, hasta, medicoId));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<byte[]> reporteExcel(String desde, String hasta, Long medicoId) {
        return citaService.reporteExcel(desde, hasta, medicoId).respuesta();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA', 'MEDICO')")
    public ResponseEntity<Cita> obtenerCita(Long id) {
        return ResponseEntity.ok(citaService.obtenerCita(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA', 'MEDICO')")
    public ResponseEntity<RespuestaRs<Cita>> reprogramar(Long id, ReprogramarRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Cita reprogramada correctamente.", citaService.reprogramar(id, rq)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA', 'MEDICO')")
    public ResponseEntity<RespuestaRs<Cita>> cambiarEstado(Long id, EstadoCitaRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Estado actualizado correctamente.", citaService.cambiarEstado(id, rq)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA', 'MEDICO')")
    public ResponseEntity<List<CitaHistorialRs>> historialEstados(Long id) {
        return ResponseEntity.ok(citaService.historialEstados(id));
    }
}
