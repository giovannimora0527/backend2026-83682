package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.uniminuto.clinica.entity.enums.EstadoFormula;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.FormulaRs;
import com.uniminuto.clinica.models.MotivoRq;
import com.uniminuto.clinica.models.RespuestaRs;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Fórmulas médicas")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    @Operation(summary = "Listar fórmulas")
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<FormulaRs>> listar(@RequestParam(required = false) String texto, @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta, @RequestParam(required = false) EstadoFormula estado);

    @Operation(summary = "Fórmulas de una cita")
    @GetMapping(value = "/cita/{citaId}", produces = {"application/json"})
    ResponseEntity<List<FormulaRs>> listarPorCita(@PathVariable("citaId") Long citaId);

    @Operation(summary = "Consultar una fórmula")
    @GetMapping(value = "/{id}", produces = {"application/json"})
    ResponseEntity<FormulaRs> obtener(@PathVariable("id") Long id);

    @Operation(summary = "Descargar la fórmula en PDF")
    @GetMapping(value = "/{id}/pdf")
    ResponseEntity<byte[]> pdf(@PathVariable("id") Long id);

    @Operation(summary = "Crear una fórmula en borrador")
    @PostMapping(value = "/crear", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<FormulaRs>> crear(@Valid @RequestBody FormulaRq rq);

    @Operation(summary = "Editar una fórmula en borrador")
    @PutMapping(value = "/{id}", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<FormulaRs>> actualizar(@PathVariable("id") Long id, @Valid @RequestBody FormulaRq rq);

    @Operation(summary = "Confirmar la fórmula", description = "Queda pendiente de despacho y ya no se edita.")
    @PostMapping(value = "/{id}/confirmar", produces = {"application/json"})
    ResponseEntity<RespuestaRs<FormulaRs>> confirmar(@PathVariable("id") Long id);

    @Operation(summary = "Anular la fórmula")
    @PostMapping(value = "/{id}/anular", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<FormulaRs>> anular(@PathVariable("id") Long id, @Valid @RequestBody MotivoRq rq);

    @Operation(summary = "Retirado: responde 410")
    @PostMapping(value = "/guardar", produces = {"application/json"})
    ResponseEntity<Void> guardarAntiguo();

    @Operation(summary = "Retirado: responde 410")
    @PostMapping(value = "/actualizar", produces = {"application/json"})
    ResponseEntity<Void> actualizarAntiguo();
}
