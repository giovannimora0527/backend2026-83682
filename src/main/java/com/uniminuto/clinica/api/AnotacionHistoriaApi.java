package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/anotacion-historia")
public interface AnotacionHistoriaApi {

    // Lista todas las anotaciones
    @GetMapping(
            value = "/listar-anotaciones",
            produces = {"application/json"}
    )
    ResponseEntity<List<AnotacionHistoria>> listarAnotaciones()
            throws BadRequestException;


    // Guarda una nueva anotación
    @PostMapping(
            value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<MiRespuestaRS> guardarAnotacion(
            @RequestBody AnotacionHistoriaRq anotacionHistoriaRq
    ) throws BadRequestException;


    // Actualiza una anotación existente
    @PostMapping(
            value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<MiRespuestaRS> actualizarAnotacion(
            @RequestBody AnotacionHistoriaRq anotacionHistoriaRq
    ) throws BadRequestException;
}