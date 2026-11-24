package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.uniminuto.clinica.models.ConfiguracionRq;
import com.uniminuto.clinica.models.ConfiguracionRs;
import com.uniminuto.clinica.models.RespuestaRs;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Configuración de la clínica")
@RequestMapping("/configuracion-clinica")
public interface ConfiguracionApi {

    @Operation(summary = "Consultar la configuración")
    @GetMapping(produces = {"application/json"})
    ResponseEntity<ConfiguracionRs> obtener();

    @Operation(summary = "Actualizar la configuración")
    @PutMapping(produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ConfiguracionRs>> actualizar(@Valid @RequestBody ConfiguracionRq rq);
}
