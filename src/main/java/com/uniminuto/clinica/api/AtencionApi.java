package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.AtencionCitaRs;
import com.uniminuto.clinica.models.AtencionRq;
import com.uniminuto.clinica.models.AtencionRs;
import com.uniminuto.clinica.models.RespuestaRs;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/atencion")
public interface AtencionApi {

    @PostMapping(value = "/iniciar/{citaId}", produces = {"application/json"})
    ResponseEntity<RespuestaRs<AtencionRs>> iniciar(@PathVariable("citaId") Long citaId);

    @GetMapping(value = "/cita/{citaId}", produces = {"application/json"})
    ResponseEntity<AtencionCitaRs> consultar(@PathVariable("citaId") Long citaId);

    @PutMapping(value = "/{id}", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<AtencionRs>> guardar(@PathVariable("id") Long id, @Valid @RequestBody AtencionRq rq);

    @PostMapping(value = "/{id}/finalizar", produces = {"application/json"})
    ResponseEntity<RespuestaRs<AtencionRs>> finalizar(@PathVariable("id") Long id);

    @GetMapping(value = "/{id}/prefactura/pdf")
    ResponseEntity<byte[]> prefactura(@PathVariable("id") Long id);
}
