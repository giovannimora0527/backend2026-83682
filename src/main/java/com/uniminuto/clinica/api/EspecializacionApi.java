package com.uniminuto.clinica.api;

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

@RequestMapping("/especializacion")
public interface EspecializacionApi {

    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Especializacion>> listarEspecializaciones(
            @RequestParam(defaultValue = "false") boolean soloActivas)
            throws BadRequestException;


    @PostMapping(value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearEspecializacion(
            @RequestBody Especializacion especializacion
    )
            throws BadRequestException;


    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarEspecializacion(
            @RequestBody Especializacion especializacion
    )
            throws BadRequestException;

    @PatchMapping(value = "/{id}/estado", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<Especializacion>> cambiarEstado(@PathVariable("id") Long id, @Valid @RequestBody EstadoRq rq);
}
