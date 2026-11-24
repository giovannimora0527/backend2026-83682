package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.DespachoApi;
import com.uniminuto.clinica.entity.enums.EstadoDespacho;
import com.uniminuto.clinica.models.DespachoConfirmarRq;
import com.uniminuto.clinica.models.DespachoFormulaRs;
import com.uniminuto.clinica.models.DespachoMedicamentoRs;
import com.uniminuto.clinica.models.DespachoRs;
import com.uniminuto.clinica.models.FormulaHistorialRs;
import com.uniminuto.clinica.models.FormulaPendienteRs;
import com.uniminuto.clinica.models.MotivoRq;
import com.uniminuto.clinica.models.RespuestaRs;
import com.uniminuto.clinica.service.DespachoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

@RestController
public class DespachoApiController implements DespachoApi {

    private final DespachoService servicio;

    public DespachoApiController(DespachoService servicio) {
        this.servicio = servicio;
    }

    @Override
    @PreAuthorize("hasRole('DESPACHO_MEDICO')")
    public ResponseEntity<List<FormulaPendienteRs>> formulasPendientes(String texto, String numero, LocalDate desde, LocalDate hasta, EstadoDespacho estado) {
        return ResponseEntity.ok(servicio.formulasPendientes(texto, numero, desde, hasta, estado));
    }

    @Override
    @PreAuthorize("hasRole('DESPACHO_MEDICO')")
    public ResponseEntity<DespachoFormulaRs> consultar(Long formulaId) {
        return ResponseEntity.ok(servicio.consultar(formulaId));
    }

    @Override
    @PreAuthorize("hasRole('DESPACHO_MEDICO')")
    public ResponseEntity<RespuestaRs<DespachoFormulaRs>> preparar(Long formulaId) {
        return ResponseEntity.ok(RespuestaRs.ok("Preparación iniciada.",
                servicio.preparar(formulaId)));
    }

    @Override
    @PreAuthorize("hasRole('DESPACHO_MEDICO')")
    public ResponseEntity<RespuestaRs<DespachoRs>> confirmar(Long despachoId, DespachoConfirmarRq rq, String idempotencyKey) {
        return ResponseEntity.ok(RespuestaRs.ok("Despacho confirmado.",
                servicio.confirmar(despachoId, rq, idempotencyKey)));
    }

    @Override
    @PreAuthorize("hasAnyRole('DESPACHO_MEDICO', 'ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<DespachoFormulaRs>> liberar(Long despachoId) {
        return ResponseEntity.ok(RespuestaRs.ok("Preparación liberada.",
                servicio.liberar(despachoId)));
    }

    @Override
    @PreAuthorize("hasAnyRole('DESPACHO_MEDICO', 'ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<DespachoFormulaRs>> cancelar(Long formulaId, MotivoRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Despacho cancelado.",
                servicio.cancelar(formulaId, rq.motivo())));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<DespachoRs>> revertir(Long despachoId, MotivoRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Despacho revertido.",
                servicio.revertir(despachoId, rq.motivo())));
    }

    @Override
    @PreAuthorize("hasAnyRole('DESPACHO_MEDICO', 'ADMINISTRADOR')")
    public ResponseEntity<byte[]> comprobante(Long despachoId) {
        return servicio.comprobante(despachoId).respuesta();
    }

    @Override
    @PreAuthorize("hasAnyRole('DESPACHO_MEDICO', 'ADMINISTRADOR')")
    public ResponseEntity<List<DespachoRs>> historial(LocalDate desde, LocalDate hasta, String texto, String estado) {
        return ResponseEntity.ok(servicio.historial(desde, hasta, texto, estado));
    }

    @Override
    @PreAuthorize("hasAnyRole('DESPACHO_MEDICO', 'ADMINISTRADOR')")
    public ResponseEntity<List<DespachoMedicamentoRs>> historialMedicamento(Long medicamentoId) {
        return ResponseEntity.ok(servicio.historialMedicamento(medicamentoId));
    }

    @Override
    @PreAuthorize("hasAnyRole('DESPACHO_MEDICO', 'ADMINISTRADOR')")
    public ResponseEntity<List<FormulaHistorialRs>> historialEstados(Long formulaId) {
        return ResponseEntity.ok(servicio.historialEstados(formulaId));
    }
}
