package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;
import com.uniminuto.clinica.models.EstadoRq;
import com.uniminuto.clinica.models.RespuestaRs;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "Especialidades")
@RequestMapping("/especializacion")
public interface EspecializacionApi {

    @Operation(summary = "Listar especialidades")
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Especializacion>> listarEspecializaciones(
            @RequestParam(defaultValue = "false") boolean soloActivas)
            throws BadRequestException;


    @Operation(summary = "Crear una especialidad")
    @PostMapping(value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearEspecializacion(
            @RequestBody Especializacion especializacion
    )
            throws BadRequestException;


    @Operation(summary = "Actualizar una especialidad")
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarEspecializacion(
            @RequestBody Especializacion especializacion
    )
            throws BadRequestException;

    @Operation(summary = "Activar o inactivar una especialidad")
    @PatchMapping(value = "/{id}/estado", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<Especializacion>> cambiarEstado(@PathVariable("id") Long id, @Valid @RequestBody EstadoRq rq);
}
