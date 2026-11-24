package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.uniminuto.clinica.models.HorarioRq;
import com.uniminuto.clinica.models.HorarioRs;
import com.uniminuto.clinica.models.RespuestaRs;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Médicos y horarios")
@RequestMapping("/medico")
public interface MedicoApi {

    @Operation(summary = "Médicos disponibles en un rango")
    @GetMapping(value = "/listar-disponibles",
            produces = {"application/json"})
    ResponseEntity<List<Medico>> listarMedicosDisponibles(
            @RequestParam(required = false) LocalDateTime inicio,
            @RequestParam(required = false) LocalDateTime fin,
            @RequestParam(required = false) LocalDateTime fechaInicio,
            @RequestParam(required = false) LocalDateTime fechaFin)
            throws BadRequestException;


    @Operation(summary = "Listar médicos")
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Medico>> listarTodos()
            throws BadRequestException;

    @Operation(summary = "Crear un médico")
    @PostMapping(value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearMedico(
            @RequestBody MedicoRq medicoRq
    )
            throws BadRequestException;

    @Operation(summary = "Actualizar un médico")
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarMedico(
            @RequestBody MedicoRq medicoRq
    )
            throws BadRequestException;

    @Operation(summary = "Horarios de un médico")
    @GetMapping(value = "/{id}/horarios", produces = {"application/json"})
    ResponseEntity<List<HorarioRs>> listarHorarios(@PathVariable("id") Long id);

    @Operation(summary = "Reemplazar los horarios de un médico")
    @PutMapping(value = "/{id}/horarios", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<List<HorarioRs>>> guardarHorarios(@PathVariable("id") Long id,
                                                                 @RequestBody List<HorarioRq> horarios);

    @Operation(summary = "Horarios del médico autenticado")
    @GetMapping(value = "/mis-horarios", produces = {"application/json"})
    ResponseEntity<List<HorarioRs>> misHorarios();
}
