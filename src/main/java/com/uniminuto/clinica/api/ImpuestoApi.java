package com.uniminuto.clinica.api;

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

@RequestMapping("/impuesto")
public interface ImpuestoApi {

    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<ImpuestoRs>> listar();

    @PostMapping(value = "/crear", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ImpuestoRs>> crear(@Valid @RequestBody ImpuestoRq rq);

    @PutMapping(value = "/{id}", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ImpuestoRs>> actualizar(@PathVariable("id") Long id, @Valid @RequestBody ImpuestoRq rq);
}
