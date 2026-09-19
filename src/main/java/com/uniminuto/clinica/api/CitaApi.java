package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import org.apache.coyote.BadRequestException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
public interface CitaApi {

    // Lista las citas entre dos fechas
    @GetMapping(
            value = "/listar-citas",
            produces = {"application/json"}
    )
    ResponseEntity<List<Cita>> listarCitas(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaInicial,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaFinal
    ) throws BadRequestException;


    // Guarda una nueva cita
    @PostMapping(
            value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<MiRespuestaRS> guardarCita(
            @RequestBody CitaRq citaRq
    ) throws BadRequestException;


    // Actualiza una cita existente
    @PostMapping(
            value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<MiRespuestaRS> actualizarCita(
            @RequestBody CitaRq citaRq
    ) throws BadRequestException;
}