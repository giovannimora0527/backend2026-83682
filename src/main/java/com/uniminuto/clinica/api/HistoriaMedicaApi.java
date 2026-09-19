package com.uniminuto.clinica.api;


import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import org.apache.coyote.BadRequestException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/historia-medica")
public interface HistoriaMedicaApi {

    // Lista historias médicas entre dos fechas
    // ordenadas desde la más reciente hasta la más antigua
    @GetMapping(
            value = "/listar-historias",
            produces = {"application/json"}
    )
    ResponseEntity<List<HistoriaMedica>> listarHistorias(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaInicial,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaFinal
    ) throws BadRequestException;


    // Guarda una nueva historia médica
    @PostMapping(
            value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<MiRespuestaRS> guardarHistoriaMedica(
            @RequestBody HistoriaMedicaRq historiaMedicaRq
    ) throws BadRequestException;


    // Actualiza una historia médica existente
    @PostMapping(
            value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<MiRespuestaRS> actualizarHistoriaMedica(
            @RequestBody HistoriaMedicaRq historiaMedicaRq
    ) throws BadRequestException;
}