package com.uniminuto.clinica.api;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/usuario")
public interface UsuarioApi {

    /**
     * Metodo test del servicio de usuarios (lo invoca el servicio Angular).
     *
     * @return mensaje de servicio activo.
     */
    @GetMapping(value = "/test", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> testService();

    /**
     * Metodo que lista los usuarios (ruta usada por Angular).
     *
     * @return Lista<UsuarioRs> </>.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<UsuarioRs>> listarUsuarios()
            throws BadRequestException;

    /**
     * Metodo que lista los usuariosRs organizados alfabeticamente (ruta del parcial).
     *
     * @return Lista<UsuarioRs> </>.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar-ordenado", produces = {"application/json"})
    ResponseEntity<List<UsuarioRs>> getUsuarios()
            throws BadRequestException;

    /**
     * Metodo que guarda un nuevo usuario.
     *
     * @param usuarioRq Datos del usuario a guardar.
     * @return respuesta de la operación.
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
     * @return respuesta de la operación.
     * @throws BadRequestException excepcion.
     */
    @RequestMapping(value = "/actualizar",
            method = {RequestMethod.POST, RequestMethod.PUT},
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizar(
            @RequestBody UsuarioRq usuarioRq
    )
            throws BadRequestException;
}
