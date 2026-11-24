package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.enums.EstadoDespacho;
import com.uniminuto.clinica.models.DespachoConfirmarRq;
import com.uniminuto.clinica.models.DespachoFormulaRs;
import com.uniminuto.clinica.models.DespachoMedicamentoRs;
import com.uniminuto.clinica.models.DespachoRs;
import com.uniminuto.clinica.models.FormulaHistorialRs;
import com.uniminuto.clinica.models.FormulaPendienteRs;
import com.uniminuto.clinica.models.MotivoRq;
import com.uniminuto.clinica.models.RespuestaRs;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/despacho")
public interface DespachoApi {

    @GetMapping(value = "/formulas-pendientes", produces = {"application/json"})
    ResponseEntity<List<FormulaPendienteRs>> formulasPendientes(@RequestParam(required = false) String texto, @RequestParam(required = false) String numero, @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta, @RequestParam(required = false) EstadoDespacho estado);

    @GetMapping(value = "/formula/{formulaId}", produces = {"application/json"})
    ResponseEntity<DespachoFormulaRs> consultar(@PathVariable("formulaId") Long formulaId);

    @PostMapping(value = "/formula/{formulaId}/preparar", produces = {"application/json"})
    ResponseEntity<RespuestaRs<DespachoFormulaRs>> preparar(@PathVariable("formulaId") Long formulaId);

    @PostMapping(value = "/{despachoId}/confirmar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<DespachoRs>> confirmar(@PathVariable("despachoId") Long despachoId, @Valid @RequestBody DespachoConfirmarRq rq, @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey);

    @PostMapping(value = "/{despachoId}/liberar", produces = {"application/json"})
    ResponseEntity<RespuestaRs<DespachoFormulaRs>> liberar(@PathVariable("despachoId") Long despachoId);

    @PostMapping(value = "/formula/{formulaId}/cancelar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<DespachoFormulaRs>> cancelar(@PathVariable("formulaId") Long formulaId, @Valid @RequestBody MotivoRq rq);

    @PostMapping(value = "/{despachoId}/revertir", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<DespachoRs>> revertir(@PathVariable("despachoId") Long despachoId, @Valid @RequestBody MotivoRq rq);

    @GetMapping(value = "/{despachoId}/comprobante/pdf")
    ResponseEntity<byte[]> comprobante(@PathVariable("despachoId") Long despachoId);

    @GetMapping(value = "/historial", produces = {"application/json"})
    ResponseEntity<List<DespachoRs>> historial(@RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta, @RequestParam(required = false) String texto, @RequestParam(required = false) String estado);

    @GetMapping(value = "/medicamento/{medicamentoId}/historial", produces = {"application/json"})
    ResponseEntity<List<DespachoMedicamentoRs>> historialMedicamento(@PathVariable("medicamentoId") Long medicamentoId);

    @GetMapping(value = "/formula/{formulaId}/historial-estados", produces = {"application/json"})
    ResponseEntity<List<FormulaHistorialRs>> historialEstados(@PathVariable("formulaId") Long formulaId);
}
