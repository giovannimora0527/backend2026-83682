package com.uniminuto.clinica.api;

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

@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<FormulaRs>> listar(@RequestParam(required = false) String texto, @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta, @RequestParam(required = false) EstadoFormula estado);

    @GetMapping(value = "/cita/{citaId}", produces = {"application/json"})
    ResponseEntity<List<FormulaRs>> listarPorCita(@PathVariable("citaId") Long citaId);

    @GetMapping(value = "/{id}", produces = {"application/json"})
    ResponseEntity<FormulaRs> obtener(@PathVariable("id") Long id);

    @GetMapping(value = "/{id}/pdf")
    ResponseEntity<byte[]> pdf(@PathVariable("id") Long id);

    @PostMapping(value = "/crear", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<FormulaRs>> crear(@Valid @RequestBody FormulaRq rq);

    @PutMapping(value = "/{id}", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<FormulaRs>> actualizar(@PathVariable("id") Long id, @Valid @RequestBody FormulaRq rq);

    @PostMapping(value = "/{id}/confirmar", produces = {"application/json"})
    ResponseEntity<RespuestaRs<FormulaRs>> confirmar(@PathVariable("id") Long id);

    @PostMapping(value = "/{id}/anular", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<FormulaRs>> anular(@PathVariable("id") Long id, @Valid @RequestBody MotivoRq rq);

    @PostMapping(value = "/guardar", produces = {"application/json"})
    ResponseEntity<Void> guardarAntiguo();

    @PostMapping(value = "/actualizar", produces = {"application/json"})
    ResponseEntity<Void> actualizarAntiguo();
}
