package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/usuario")
public interface UsuarioApi {

    /**
     * Metodo que lista los usuariosRs organizados alfabeticamente.
     *
     * @return Lista<UsuarioRs> </>.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar-ordenado",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<UsuarioRs>> getUsuarios()
            throws BadRequestException;


    /**
     * Metodo que guarda un nuevo usuario.
     *
     * @param usuarioRq Datos del usuario a guardar.
     * @return Lista<UsuarioRs> </>.
     * @throws BadRequestException excepcion.
     */
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
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizar(
            @RequestBody UsuarioRq usuarioRq
    )
            throws BadRequestException;
}
