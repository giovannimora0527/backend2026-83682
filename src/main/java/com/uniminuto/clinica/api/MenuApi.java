
package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Menu;
import com.uniminuto.clinica.service.IdentidadService;
import com.uniminuto.clinica.service.IdentidadService.Identidad;
import org.springframework.web.bind.annotation.RequestAttribute;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CLÍNICA VETERINARIA - UNIMINUTO
 *
 * API para consultar los menús registrados
 * y los menús asociados al perfil de un usuario.
 *
 */
@RequestMapping("/menu")
public interface MenuApi {
    @GetMapping("/propio")
    ResponseEntity<List<Menu>> obtenerMenusPropios(
            @RequestAttribute(IdentidadService.ATRIBUTO) Identidad identidad);


    /**
     * Consultar todos los menús activos.
     *
     * GET /menu/listar-activos
     */
    @GetMapping(
            value = "/listar-activos",
            produces = "application/json"
    )
    ResponseEntity<List<Menu>> listarMenusActivos();

    /**
     * Consultar los menús asociados al perfil
     * de un usuario existente.
     *
     * GET /menu/usuario/29
     */
    @GetMapping(
            value = "/usuario/{usuarioId}",
            produces = "application/json"
    )
    ResponseEntity<List<Menu>> obtenerMenusPorUsuario(
            @PathVariable("usuarioId") Long usuarioId,
            @RequestAttribute(IdentidadService.ATRIBUTO) Identidad identidad
    );
}
