package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Despacho")
@RequestMapping("/despacho")
public interface DespachoApi {

    @Operation(summary = "Bandeja de fórmulas pendientes")
    @GetMapping(value = "/formulas-pendientes", produces = {"application/json"})
    ResponseEntity<List<FormulaPendienteRs>> formulasPendientes(@RequestParam(required = false) String texto, @RequestParam(required = false) String numero, @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta, @RequestParam(required = false) EstadoDespacho estado);

    @Operation(summary = "Consultar una fórmula con lotes sugeridos (FEFO)")
    @GetMapping(value = "/formula/{formulaId}", produces = {"application/json"})
    ResponseEntity<DespachoFormulaRs> consultar(@PathVariable("formulaId") Long formulaId);

    @Operation(summary = "Iniciar la preparación")
    @PostMapping(value = "/formula/{formulaId}/preparar", produces = {"application/json"})
    ResponseEntity<RespuestaRs<DespachoFormulaRs>> preparar(@PathVariable("formulaId") Long formulaId);

    @Operation(summary = "Confirmar el despacho", description = "Todo o nada. Admite el encabezado Idempotency-Key.")
    @PostMapping(value = "/{despachoId}/confirmar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<DespachoRs>> confirmar(@PathVariable("despachoId") Long despachoId, @Valid @RequestBody DespachoConfirmarRq rq, @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey);

    @Operation(summary = "Liberar una preparación sin entregar")
    @PostMapping(value = "/{despachoId}/liberar", produces = {"application/json"})
    ResponseEntity<RespuestaRs<DespachoFormulaRs>> liberar(@PathVariable("despachoId") Long despachoId);

    @Operation(summary = "Cancelar el despacho de una fórmula")
    @PostMapping(value = "/formula/{formulaId}/cancelar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<DespachoFormulaRs>> cancelar(@PathVariable("formulaId") Long formulaId, @Valid @RequestBody MotivoRq rq);

    @Operation(summary = "Revertir un despacho confirmado")
    @PostMapping(value = "/{despachoId}/revertir", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<DespachoRs>> revertir(@PathVariable("despachoId") Long despachoId, @Valid @RequestBody MotivoRq rq);

    @Operation(summary = "Descargar el comprobante en PDF")
    @GetMapping(value = "/{despachoId}/comprobante/pdf")
    ResponseEntity<byte[]> comprobante(@PathVariable("despachoId") Long despachoId);

    @Operation(summary = "Historial de despachos")
    @GetMapping(value = "/historial", produces = {"application/json"})
    ResponseEntity<List<DespachoRs>> historial(@RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta, @RequestParam(required = false) String texto, @RequestParam(required = false) String estado);

    @Operation(summary = "Historial de dispensaciones de un medicamento")
    @GetMapping(value = "/medicamento/{medicamentoId}/historial", produces = {"application/json"})
    ResponseEntity<List<DespachoMedicamentoRs>> historialMedicamento(@PathVariable("medicamentoId") Long medicamentoId);

    @Operation(summary = "Historial de estados del despacho de una fórmula")
    @GetMapping(value = "/formula/{formulaId}/historial-estados", produces = {"application/json"})
    ResponseEntity<List<FormulaHistorialRs>> historialEstados(@PathVariable("formulaId") Long formulaId);
}
