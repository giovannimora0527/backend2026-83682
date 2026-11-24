package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.uniminuto.clinica.models.EstadoRq;
import com.uniminuto.clinica.models.RespuestaRs;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuarios y perfiles")
@RequestMapping("/usuario")
public interface UsuarioApi {

    /**
     * Metodo que lista los usuariosRs organizados alfabeticamente.
     *
     * @return Lista<UsuarioRs> </>.
     * @throws BadRequestException excepcion.
     */
    @Operation(summary = "Listar usuarios")
    @GetMapping(value = "/listar-ordenado",
            produces = {"application/json"})
    ResponseEntity<List<UsuarioRs>> getUsuarios()
            throws BadRequestException;


    /**
     * Metodo que guarda un nuevo usuario.
     *
     * @param usuarioRq Datos del usuario a guardar.
     * @return Lista<UsuarioRs> </>.
     * @throws BadRequestException excepcion.
     */
    @Operation(summary = "Crear un usuario")
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardar(
            @RequestBody UsuarioRq usuarioRq
    )
            throws BadRequestException;

    /**
     * Metodo que actualiza un usuario existente.
     *
     * @param usuarioRq Datos del usuario a actualizar.
     * @return Lista<UsuarioRs> </>.
     * @throws BadRequestException excepcion.
     */
    @Operation(summary = "Actualizar un usuario")
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizar(
            @RequestBody UsuarioRq usuarioRq
    )
            throws BadRequestException;

    @Operation(summary = "Activar o inactivar un usuario")
    @PatchMapping(value = "/{id}/estado", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<UsuarioRs>> cambiarEstado(@PathVariable("id") Long id, @Valid @RequestBody EstadoRq rq);
}
