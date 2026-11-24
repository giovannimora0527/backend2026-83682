package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.HistoriaMedicaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "Historias clínicas")
@RequestMapping("/historia-clinica")
public interface HistoriaApi {

    @Operation(summary = "Listar historias con anotaciones y fórmulas")
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<HistoriaMedicaRs>> listarAllHIstorias()
            throws BadRequestException;

    @Operation(summary = "Crear una historia clínica")
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarHistoria(
            @RequestBody HistoriaMedicaRq historiaMedicaRq
    )
            throws BadRequestException;

    @Operation(summary = "Actualizar una historia clínica")
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarHistoria(
            @RequestBody HistoriaMedicaRq historiaMedicaRq
    )
            throws BadRequestException;
}
