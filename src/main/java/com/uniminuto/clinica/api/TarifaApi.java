package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.RespuestaRs;
import com.uniminuto.clinica.models.TarifaActualizarRq;
import com.uniminuto.clinica.models.TarifaCrearRq;
import com.uniminuto.clinica.models.TarifaHistorialRs;
import com.uniminuto.clinica.models.TarifaRs;
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

@RequestMapping("/tarifa")
public interface TarifaApi {

    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<TarifaRs>> listar(@RequestParam(required = false) Long servicioId, @RequestParam(defaultValue = "false") boolean soloVigentes);

    @PostMapping(value = "/crear", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<TarifaRs>> crear(@Valid @RequestBody TarifaCrearRq rq);

    @PutMapping(value = "/{id}", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<TarifaRs>> actualizar(@PathVariable("id") Long id, @Valid @RequestBody TarifaActualizarRq rq);

    @GetMapping(value = "/{id}/historial", produces = {"application/json"})
    ResponseEntity<List<TarifaHistorialRs>> historialTarifa(@PathVariable("id") Long id);

    @GetMapping(value = "/historial", produces = {"application/json"})
    ResponseEntity<List<TarifaHistorialRs>> historial(@RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta);
}
