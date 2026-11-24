package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Atención médica")
@RequestMapping("/atencion")
public interface AtencionApi {

    @Operation(summary = "Iniciar la atención de una cita", description = "Pasa la cita a EN_ATENCION. Idempotente.")
    @PostMapping(value = "/iniciar/{citaId}", produces = {"application/json"})
    ResponseEntity<RespuestaRs<AtencionRs>> iniciar(@PathVariable("citaId") Long citaId);

    @Operation(summary = "Consultar atención, mascota, propietario y antecedentes")
    @GetMapping(value = "/cita/{citaId}", produces = {"application/json"})
    ResponseEntity<AtencionCitaRs> consultar(@PathVariable("citaId") Long citaId);

    @Operation(summary = "Guardar el borrador de la atención")
    @PutMapping(value = "/{id}", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<AtencionRs>> guardar(@PathVariable("id") Long id, @Valid @RequestBody AtencionRq rq);

    @Operation(summary = "Finalizar la atención", description = "Pasa la cita a ATENDIDA y registra la anotación en la historia.")
    @PostMapping(value = "/{id}/finalizar", produces = {"application/json"})
    ResponseEntity<RespuestaRs<AtencionRs>> finalizar(@PathVariable("id") Long id);

    @Operation(summary = "Descargar la prefactura en PDF")
    @GetMapping(value = "/{id}/prefactura/pdf")
    ResponseEntity<byte[]> prefactura(@PathVariable("id") Long id);
}
