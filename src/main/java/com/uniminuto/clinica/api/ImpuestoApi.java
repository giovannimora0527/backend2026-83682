package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.uniminuto.clinica.models.ImpuestoRq;
import com.uniminuto.clinica.models.ImpuestoRs;
import com.uniminuto.clinica.models.RespuestaRs;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Tag(name = "Impuestos")
@RequestMapping("/impuesto")
public interface ImpuestoApi {

    @Operation(summary = "Listar impuestos")
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<ImpuestoRs>> listar();

    @Operation(summary = "Crear un impuesto")
    @PostMapping(value = "/crear", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ImpuestoRs>> crear(@Valid @RequestBody ImpuestoRq rq);

    @Operation(summary = "Actualizar un impuesto")
    @PutMapping(value = "/{id}", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ImpuestoRs>> actualizar(@PathVariable("id") Long id, @Valid @RequestBody ImpuestoRq rq);
}
