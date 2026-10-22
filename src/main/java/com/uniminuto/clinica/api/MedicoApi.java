package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
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
@RequestMapping("/medico")
public interface MedicoApi {

    @GetMapping(value = "/listar-disponibles",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Medico>> listarMedicosDisponibles(
            @RequestParam LocalDateTime fechaInicio,
            @RequestParam LocalDateTime fechaFin)
            throws BadRequestException;


    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Medico>> listarTodos()
            throws BadRequestException;

    @PostMapping(value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearMedico(
            @RequestBody MedicoRq medicoRq
    )
            throws BadRequestException;

    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarMedico(
            @RequestBody MedicoRq medicoRq
    )
            throws BadRequestException;
}
