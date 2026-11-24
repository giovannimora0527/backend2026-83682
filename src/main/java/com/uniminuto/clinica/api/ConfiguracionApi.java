package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.ConfiguracionRq;
import com.uniminuto.clinica.models.ConfiguracionRs;
import com.uniminuto.clinica.models.RespuestaRs;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/configuracion-clinica")
public interface ConfiguracionApi {

    @GetMapping(produces = {"application/json"})
    ResponseEntity<ConfiguracionRs> obtener();

    @PutMapping(produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ConfiguracionRs>> actualizar(@Valid @RequestBody ConfiguracionRq rq);
}
