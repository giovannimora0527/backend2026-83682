package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.enums.EstadoFormula;
import com.uniminuto.clinica.exception.GoneException;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.FormulaRs;
import com.uniminuto.clinica.models.MotivoRq;
import com.uniminuto.clinica.models.RespuestaRs;
import com.uniminuto.clinica.service.FormulaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    private final FormulaService servicio;

    public FormulaMedicaApiController(FormulaService servicio) {
        this.servicio = servicio;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<List<FormulaRs>> listar(String texto, LocalDate desde, LocalDate hasta, EstadoFormula estado) {
        return ResponseEntity.ok(servicio.listar(texto, desde, hasta, estado));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<List<FormulaRs>> listarPorCita(Long citaId) {
        return ResponseEntity.ok(servicio.listarPorCita(citaId));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO', 'DESPACHO_MEDICO')")
    public ResponseEntity<FormulaRs> obtener(Long id) {
        return ResponseEntity.ok(servicio.obtener(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO', 'DESPACHO_MEDICO')")
    public ResponseEntity<byte[]> pdf(Long id) {
        return servicio.pdf(id).respuesta();
    }

    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<RespuestaRs<FormulaRs>> crear(FormulaRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Fórmula creada en borrador.", servicio.crear(rq)));
    }

    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<RespuestaRs<FormulaRs>> actualizar(Long id, FormulaRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Fórmula actualizada correctamente.", servicio.actualizar(id, rq)));
    }

    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<RespuestaRs<FormulaRs>> confirmar(Long id) {
        return ResponseEntity.ok(RespuestaRs.ok("Fórmula confirmada.", servicio.confirmar(id)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<RespuestaRs<FormulaRs>> anular(Long id, MotivoRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Fórmula anulada.", servicio.anular(id, rq.motivo())));
    }

    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<Void> guardarAntiguo() {
        throw new GoneException("Use POST /formula-medica/crear.");
    }

    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<Void> actualizarAntiguo() {
        throw new GoneException("Use PUT /formula-medica/{id}.");
    }
}
